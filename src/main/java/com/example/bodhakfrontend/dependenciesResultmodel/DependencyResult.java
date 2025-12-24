package com.example.bodhakfrontend.dependenciesResultmodel;

import java.util.List;

public class DependencyResult {
    private List<String> external;
    private List<String> internal;
    private List<String> javaLibrariesDependencies;

    public DependencyResult(List<String> external, List<String> internal, List<String> javaLibrariesDependencies) {
        this.external = external;
        this.internal = internal;
        this.javaLibrariesDependencies = javaLibrariesDependencies;
    }

    public List<String> getExternal() { return external; }
    public List<String> getInternal() { return internal; }
    public List<String> getJavaLibrariesDependencies() { return javaLibrariesDependencies; }
}
