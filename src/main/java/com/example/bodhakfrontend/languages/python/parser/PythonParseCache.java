package com.example.bodhakfrontend.languages.python.parser;

import com.example.bodhakfrontend.core.plugin.Parser;
import org.treesitter.TSParser;
import org.treesitter.TSTree;
import org.treesitter.TreeSitterPython;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PythonParseCache implements Parser<TSTree> {

    private final TSParser tsParser;
    private final Map<Path, TSTree> cache = new ConcurrentHashMap<>();

    public PythonParseCache() {
        this.tsParser = new TSParser();
        this.tsParser.setLanguage(new TreeSitterPython());
    }

    @Override
    public TSTree parse(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        return cache.computeIfAbsent(normalized, p -> {
            try {
                byte[] bytes = Files.readAllBytes(p);
                String sourceCode = new String(bytes, StandardCharsets.UTF_8);
                return tsParser.parseString(null, sourceCode);
            } catch (IOException e) {
                System.err.println("PythonParseCache: failed to parse " + p + " -> " + e.getMessage());
                return null;
            }
        });
    }

    @Override
    public void invalidate(Path path) {
        Path normalized = path.toAbsolutePath().normalize();
        cache.remove(normalized);
    }
}
