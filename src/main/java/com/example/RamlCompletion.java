package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.methods.Method;
import org.raml.v2.api.model.v08.resources.Resource;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class RamlCompletion {

    public void processRaml(String configPath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ProjectConfig config = mapper.readValue(new File(configPath), ProjectConfig.class);

        String apiDirPath = config.getProjectPath() + "/src/main/api";
        File apiDir = new File(apiDirPath);

        File[] ramlFiles = apiDir.listFiles((dir, name) -> name.endsWith(".raml"));
        if (ramlFiles != null && ramlFiles.length == 1) {
            String ramlFilePath = ramlFiles[0].getAbsolutePath();
            RamlModelResult ramlModelResult = new RamlModelBuilder().buildApi(ramlFilePath);

            if (ramlModelResult.hasErrors()) {
                System.out.println("Errors in the RAML file");
                return;
            }

            Api api = ramlModelResult.getApiV08();
            List<Resource> resources = api.resources();
            printEndpointSchemas(resources, "");
        }
    }

    private void printEndpointSchemas(List<Resource> resources, String parentUrl) {
        for (Resource resource : resources) {
            String url = parentUrl + resource.relativeUri().value();
            for (Method method : resource.methods()) {
                boolean hasRequestBodySchema = false;
                boolean hasResponseBodySchema = false;

                if ("post".equalsIgnoreCase(method.method())) {
                    hasRequestBodySchema = method.body() != null && !method.body().isEmpty();
                }

                hasResponseBodySchema = method.responses() != null && !method.responses().isEmpty();

                if ("get".equalsIgnoreCase(method.method())) {
                    System.out.println("Endpoint: " + url + ", Method: GET, Has Response Schema: " + hasResponseBodySchema);
                } else if ("post".equalsIgnoreCase(method.method())) {
                    System.out.println("Endpoint: " + url + ", Method: POST, Has Request Schema: " + hasRequestBodySchema + ", Has Response Schema: " + hasResponseBodySchema);
                }
            }

            printEndpointSchemas(resource.resources(), url);
        }
    }
}
