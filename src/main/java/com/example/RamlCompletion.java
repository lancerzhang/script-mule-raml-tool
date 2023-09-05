package com.example;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RamlCompletion {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final String specPath = "/src/main/api/";

    private String projectPath;

    public void setProjectPath(String projectPath) {
        this.projectPath = projectPath;
    }

    public void processRaml(String configPath) throws Exception {
        Instant startTime = Instant.now();

        ObjectMapper mapper = new ObjectMapper();
        ProjectConfig config = mapper.readValue(new File(configPath), ProjectConfig.class);
        setProjectPath(config.getProjectPath());

        CommandUtils.runMvnCompile(projectPath);
        Map<Object, Object> filteredData = YamlUtil.filterRaml(projectPath);

        completeSpec(filteredData);

        Yaml yaml = new Yaml();
        try (FileWriter writer = new FileWriter(FileUtil.getPath(projectPath + specPath + "filtered_api.yaml").toFile())) {
            yaml.dump(filteredData, writer);
        } catch (IOException e) {
            throw new RuntimeException("Error writing to filtered_api.raml", e);
        }
        Instant endTime = Instant.now();
        logger.info("Job duration (seconds): " + Duration.between(startTime, endTime).getSeconds());
    }

    protected void completeSpec(Map<Object, Object> filteredData) throws Exception {
        completeSpecHelper(filteredData, "");
    }

    protected void completeSpecHelper(Map<Object, Object> currentData, String currentPath) throws Exception {
        for (Object objKey : currentData.keySet()) {
            if (!(objKey instanceof String)) {
                continue; // skip if key isn't a String
            }

            String key = (String) objKey;
            Object value = currentData.get(key);

            String apiPath = currentPath + key;  // Construct the new path for this level
            logger.debug("processing path: " + apiPath);

            if (value instanceof Map) {
                Map<Object, Object> valueNode = (Map<Object, Object>) value;
                // This recursive call ensures that all nested maps are processed.
                completeSpecHelper(valueNode, apiPath);

                if (valueNode.containsKey("get")) {
                    generateSchema("get", apiPath, valueNode);
                }

                if (valueNode.containsKey("post")) {
                    generateSchema("post", apiPath, valueNode);
                }
            }
        }
    }

    protected void generateSchema(String httpMethod, String apiPath, Map<Object, Object> valueNode) throws Exception {
        Map<Object, Object> responseBodyMap = YamlUtil.getPathNode(valueNode, httpMethod, "responses", 200, "body");
        String flowName = httpMethod + ":" + apiPath + ":mobile_api-config";
        logger.debug("flowName: " + flowName);

        boolean hasResponseBodySchema = responseBodyMap.get("application/json") != null;

        String subFolder = FileUtil.formatFolderName(httpMethod, apiPath);
        String javaClassStr = FileUtil.readFileAsString("./combined_flow_xml/" + subFolder + "/java_classes.txt");

        if (!hasResponseBodySchema) {
            generateSchemaByJava(httpMethod, "Response", apiPath, javaClassStr, responseBodyMap);
        }
        if ("post".equals(httpMethod)) {
            Map<Object, Object> requestBodyMap = YamlUtil.getPathNode(valueNode, httpMethod, "body");
            boolean hasRequestBodySchema = requestBodyMap.get("application/json") != null;
            if (!hasRequestBodySchema) {
                generateSchemaByJava(httpMethod, "Request", apiPath, javaClassStr, requestBodyMap);
            }
        }
    }

    protected void generateSchemaByJava(String httpMethod, String type, String apiPath, String reqJavaClassStr, Map<Object, Object> requestBodyMap) throws Exception {
        if (reqJavaClassStr.isEmpty()) {
            return;
        }
        logger.info("start to use java to generate schema for " + httpMethod + ":" + apiPath);
        List<String> javaClasses = Utils.getContentItems(reqJavaClassStr);
        List<JsonNode> schemas = new ArrayList<>();
        for (String javaClass : javaClasses) {
            if (javaClass.contains(type)) {
                Class<?> clazz = JavaUtil.loadClassFromFile(javaClass, projectPath + "/target/classes/");
                JsonNode schema = JsonSchemaUtil.generateJsonSchemaNode(clazz);
                schemas.add(schema);
            }
        }

        if (!schemas.isEmpty()) {
            JsonNode schemaNode = JsonSchemaUtil.mergeAll(schemas);
            String newClassName = JavaUtil.convertToCamelCase(httpMethod + apiPath + "/" + type + "Body");
            ObjectMapper mapper = new ObjectMapper();
            String schemaStr = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(schemaNode);
            String schemaFileName = JsonSchemaUtil.writeSchema(projectPath, newClassName, schemaStr);

            Map<String, String> innerMap = new HashMap<>();
            innerMap.put("schema", "!include schema/" + schemaFileName);
            requestBodyMap.put("application/json", innerMap);
        }
    }

}
