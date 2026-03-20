package com.example.bodhakfrontend.Backend.interfaces;

import java.nio.file.Path;
import java.util.Set;

public interface ClassNameExtractor {
    Set<String> getClassNames(Path filePath);
}
