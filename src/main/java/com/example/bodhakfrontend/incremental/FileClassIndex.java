package com.example.bodhakfrontend.incremental;


import java.nio.file.Path;
import java.util.*;

public class FileClassIndex {

    private final Map<Path, Set<String>> fileToClasses = new HashMap<>();

    public void update(Path file, Set<String> classes) {
        fileToClasses.put(
                normalize(file),
                new HashSet<>(classes)
        );
    }

    public Set<String> remove(Path file) {
        return fileToClasses.remove(
                normalize(file)
        );
    }

    private Path normalize(Path p) {
        return p.toAbsolutePath().normalize();
    }
}

