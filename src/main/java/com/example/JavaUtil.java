package com.example;


import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;

public class JavaUtil {
    private static final String javaPath = "/src/main/java/";

    public static String convertToCamelCase(String input) {
        String[] words = input.split("/");
        StringBuilder camelCaseBuilder = new StringBuilder();

        for (String word : words) {
            camelCaseBuilder.append(word.substring(0, 1).toUpperCase()).append(word.substring(1));
        }

        return camelCaseBuilder.toString();
    }

    public static Class<?> loadClassFromFile(String className, String pathToJarOrClassFiles) throws Exception {
        URL url = new File(pathToJarOrClassFiles).toURI().toURL();
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        return classLoader.loadClass(className);
    }

}
