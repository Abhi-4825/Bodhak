package com.example.bodhakfrontend.Backend.Factory;

import com.example.bodhakfrontend.Backend.interfaces.ClassDependenciesBuilder;
import com.example.bodhakfrontend.Backend.interfaces.Parser;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.utils.JavaClassDependsOn;

import java.util.HashMap;
import java.util.Map;

public class ClassDependsOnFactory {

    private final Map<String, ClassDependenciesBuilder> builders = new HashMap<>();

    public ClassDependsOnFactory(ParserFactory parserFactory) {


        builders.put("java", new JavaClassDependsOn(parserFactory));
        // future:
        // builders.put("python", new PythonClassDependsOn(parser));
    }

    public ClassDependenciesBuilder getBuilder(String language) {
        ClassDependenciesBuilder builder = builders.get(language.toLowerCase());

        if (builder == null) {
            throw new IllegalArgumentException("Language not supported " + language);
        }

        return builder;
    }
}
