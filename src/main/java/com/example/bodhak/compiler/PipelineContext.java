package com.example.bodhak.compiler;

import com.example.bodhak.compiler.CompilationUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores transient states, compiler options, and the set of CompilationUnits being processed in the pipeline.
 */
public final class PipelineContext {
    private final List<CompilationUnit> compilationUnits = new ArrayList<>();
    private final Map<String, Object> attributes = new ConcurrentHashMap<>();

    public List<CompilationUnit> getCompilationUnits() {
        return compilationUnits;
    }

    public void addCompilationUnit(CompilationUnit cu) {
        compilationUnits.add(cu);
    }

    public Object getAttribute(String key) {
        return attributes.get(key);
    }

    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }

    public void removeAttribute(String key) {
        attributes.remove(key);
    }
}
