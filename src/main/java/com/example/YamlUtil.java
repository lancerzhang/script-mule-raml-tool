package com.example;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


public class YamlUtil {

    private static final Logger logger = LoggerFactory.getLogger(YamlUtil.class);
    private static final String specPath = "/src/main/api/";
    private static final String filterFileName = "endPointFilter.txt";

    public static Map<Object, Object> filterRaml(String projectPath) {
        File apiSpecDirectory = FileUtil.getPath(projectPath + specPath).toFile();
        File[] ramlFiles = apiSpecDirectory.listFiles((dir, name) -> name.toLowerCase().endsWith(".raml"));
        if (ramlFiles == null || ramlFiles.length == 0) {
            throw new RuntimeException("No RAML file found in the specified directory.");
        }
        if (ramlFiles.length > 1) {
            throw new RuntimeException("More than one RAML file found. Please ensure only one RAML file is present.");
        }
        File apiYaml = ramlFiles[0];
        logger.info("Found API raml:" + apiYaml);
        File filterYaml = new File(apiSpecDirectory, filterFileName);

        return filterYamlByAnother(apiYaml, filterYaml);
    }

    public static Map<Object, Object> filterYamlByAnother(File dataFile, File filterFile) {
        Yaml yaml = new Yaml();
        Map<Object, Object> dataMap;
        Map<Object, Object> filterMap;
        try {
            dataMap = yaml.load(new FileInputStream(dataFile));
            if (!filterFile.exists()) {
                logger.info("There is no " + filterFileName);
                return dataMap;
            }
            logger.info("Found " + filterFileName);
            filterMap = convertTxtToMap(filterFile);
            filterData(dataMap, filterMap);
            return dataMap;
        } catch (IOException e) {
            throw new RuntimeException("Error reading the files", e);
        }
    }

    protected static Map<Object, Object> convertTxtToMap(File filterFile) throws IOException {
        List<String> lines = Files.readAllLines(filterFile.toPath());
        Map<Object, Object> yamlStructure = new LinkedHashMap<>();

        for (String line : lines) {
            String[] parts = line.split("/");
            Map<Object, Object> currentMap = yamlStructure;
            for (int i = 1; i < parts.length; i++) {
                String key = "/" + parts[i];
                currentMap = (Map<Object, Object>) currentMap.computeIfAbsent(key, k -> new LinkedHashMap<>());
            }
        }
        return yamlStructure;
    }

    protected static void filterData(Map<Object, Object> data, Map<Object, Object> filter) {
        data.keySet().removeIf(key -> ((String) key).startsWith("/") && !filter.containsKey(key));
        for (Object key : data.keySet()) {
            Object dataValue = data.get(key);
            Object filterValue = filter.get(key);

            if (dataValue instanceof Map && filterValue instanceof Map) {
                filterData((Map<Object, Object>) dataValue, (Map<Object, Object>) filterValue);
            }
        }
    }

    public static <T> Map<Object, Object> getPathNode(Map<Object, Object> map, T... keys) {
        for (int i = 0; i < keys.length - 1; i++) {
            map.putIfAbsent(keys[i], new HashMap<>());
            map = (Map<Object, Object>) map.get(keys[i]);
        }

        if (!map.containsKey(keys[keys.length - 1])) {
            map.put(keys[keys.length - 1], new HashMap<>());
        }

        return (Map<Object, Object>) map.get(keys[keys.length - 1]);
    }

}