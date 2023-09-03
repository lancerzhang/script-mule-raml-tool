package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;

@SpringBootApplication
public class ScriptMuleRamlToolApplication implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(ScriptMuleRamlToolApplication.class, args);
    }

    @Override
    public void run(String... args) throws IOException {
        RamlCompletion ramlCompletion = new RamlCompletion();
        ramlCompletion.processRaml("config.json");
    }
}
