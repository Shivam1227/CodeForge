package com.judge.worker.judge;

import lombok.Getter;

@Getter
public class LanguageConfig {

    private final String dockerImage;
    private final String fileName;
    private final String compileCommand; // null for interpreted languages
    private final String runCommand;

    private LanguageConfig(String dockerImage, String fileName, String compileCommand, String runCommand) {
        this.dockerImage = dockerImage;
        this.fileName = fileName;
        this.compileCommand = compileCommand;
        this.runCommand = runCommand;
    }

    public static LanguageConfig forLanguage(String language) {
        return switch (language.toLowerCase()) {
            case "java" -> new LanguageConfig(
                    "eclipse-temurin:17-jdk", "Main.java", "javac Main.java", "java Main");
            case "python", "python3" -> new LanguageConfig(
                    "python:3.11-slim", "Main.py", null, "python3 Main.py");
            case "cpp", "c++" -> new LanguageConfig(
                    "gcc:13", "Main.cpp", "g++ Main.cpp -o Main", "./Main");
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }
}