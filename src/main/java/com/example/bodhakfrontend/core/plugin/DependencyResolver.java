package com.example.bodhakfrontend.core.plugin;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;


public interface DependencyResolver {
    Map<String, Set<String>> resolve(Path filePath, Set<String> knownNames);
}
