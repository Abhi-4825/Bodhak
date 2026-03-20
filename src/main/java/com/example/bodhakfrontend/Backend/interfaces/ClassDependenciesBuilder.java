package com.example.bodhakfrontend.Backend.interfaces;

import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

public interface ClassDependenciesBuilder {

  Map<String, Set<String>> getDependencies(Path filePath,Set<String> sourceClasses);


}
