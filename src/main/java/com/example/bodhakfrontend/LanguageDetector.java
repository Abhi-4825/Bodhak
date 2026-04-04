package com.example.bodhakfrontend;

import java.io.File;

public class LanguageDetector {

    public String detectFileType(File file) {
        String name = file.getName().toLowerCase();

        if (name.endsWith(".java")) return "Java";
        if(name.endsWith(".kt"))return "Kotlin";
        if (name.endsWith(".py")) return "Python";
        if (name.endsWith(".js")) return "JavaScript";
        if (name.endsWith(".ts")) return "TypeScript";
        if (name.endsWith(".html") || name.endsWith(".htm")) return "HTML";
        if (name.endsWith(".css")) return "CSS";
        if (name.endsWith(".xml")) return "XML";
        if (name.endsWith(".json")) return "JSON";
        if (name.endsWith(".md")) return "Markdown";
        if (name.endsWith(".txt")) return "Text";

        return "Unknown";
    }


}
