package com.example.anuviya.compiler.symbol;

import java.util.List;
import java.util.Arrays;

/**
 * Represents a structured qualified name (e.g. java.util.List).
 */
public record QualifiedName(List<String> segments) {
    public static QualifiedName of(String... segments) {
        return new QualifiedName(Arrays.asList(segments));
    }

    public static QualifiedName parse(String name) {
        if (name == null || name.isEmpty()) {
            return new QualifiedName(List.of());
        }
        return new QualifiedName(Arrays.asList(name.split("\\.")));
    }

    @Override
    public String toString() {
        return String.join(".", segments);
    }
}
