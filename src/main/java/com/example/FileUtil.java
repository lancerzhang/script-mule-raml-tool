package com.example;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    public static String changeToSystemFileSeparator(String input) {
        return input.replace("/", File.separator);
    }

    public static Path getPath(String linuxPath) {
        return Paths.get(changeToSystemFileSeparator(linuxPath));
    }

    public static String formatFolderName(String httpMethod, String url) {
        return httpMethod.toLowerCase() + "__" + url.substring(1).replace('/', '_').replace(" ", "_");
    }

    public static String readFileAsString(String fileName) {
        String data = "";
        try {
            data = new String(Files.readAllBytes(Paths.get(fileName)));
        } catch (IOException e) {
            System.out.println(fileName + " not found");
        }
        return data;
    }

}
