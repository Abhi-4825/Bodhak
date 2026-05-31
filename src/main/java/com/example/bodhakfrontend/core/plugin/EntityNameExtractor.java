package com.example.bodhakfrontend.core.plugin;

import java.nio.file.Path;
import java.util.Set;


public interface EntityNameExtractor {
    Set<String> extractNames(Path filePath);
}
