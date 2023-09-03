package com.example;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.FileVisitOption;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.EnumSet;

@SpringBootApplication
public class ScriptMuleRamlToolApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(ScriptMuleRamlToolApplication.class, args);
	}

	@Override
	public void run(String... args) {
		Path startingDir = Paths.get("combined_flow_xml");
		try {
			Files.walkFileTree(startingDir, EnumSet.noneOf(FileVisitOption.class), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
					if (file.getFileName().toString().equals("java_classes.txt")) {
						readAndPrintFile(file.toFile());
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void readAndPrintFile(File file) {
		try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
			String line;
			while ((line = reader.readLine()) != null) {
				System.out.println("FQCN: " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
