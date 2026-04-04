package com.example.bodhakfrontend.Backend.Factory;

import com.example.bodhakfrontend.Backend.interfaces.Parser;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Parser.javaParseCache;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class ParserFactory {

    private final Map<String, Parser> parsers = new HashMap<>();

    public ParserFactory(List<Path> sourcePaths) {

        parsers.put("java", new javaParseCache(sourcePaths));

        // future:
        // parsers.put("python", new PythonParser(...));
    }

    public Parser getParser(String language) {
        Parser parser = parsers.get(language.toLowerCase());

        if (parser == null) {
            throw new IllegalArgumentException("Unsupported language: " + language);
        }

        return parser;
    }
}
