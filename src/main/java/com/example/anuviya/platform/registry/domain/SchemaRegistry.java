package com.example.anuviya.platform.registry.domain;

import java.util.*;

public class SchemaRegistry {
    private static final SchemaRegistry INSTANCE = new SchemaRegistry();
    private final Map<String, String> schemas = new HashMap<>();

    private SchemaRegistry() {}

    public static SchemaRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void register(String id, String content) {
        schemas.put(id, content);
    }

    public synchronized void clear() {
        schemas.clear();
    }

    public synchronized Optional<String> get(String id) {
        return Optional.ofNullable(schemas.get(id));
    }
}
