package com.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.raml.v2.api.RamlModelBuilder;
import org.raml.v2.api.RamlModelResult;
import org.raml.v2.api.model.v08.api.Api;
import org.raml.v2.api.model.v08.methods.Method;
import org.raml.v2.api.model.v08.resources.Resource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;
import java.io.IOException;
import java.util.List;

@SpringBootApplication
public class ScriptMuleRamlToolApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ScriptMuleRamlToolApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        ProjectConfig config = mapper.readValue(new File("config.json"), ProjectConfig.class);

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
            printEndpoints(resources, "");
        }
    }

    private void printEndpoints(List<Resource> resources, String parentUrl) {
        for (Resource resource : resources) {
            String url = parentUrl + resource.relativeUri().value();
            for (Method method : resource.methods()) {
                System.out.println("Endpoint: " + url + ", Method: " + method.method());
            }
            printEndpoints(resource.resources(), url);
        }
    }
}
