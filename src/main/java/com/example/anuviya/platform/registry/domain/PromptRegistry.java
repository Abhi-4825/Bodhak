package com.example.anuviya.platform.registry.domain;

import java.util.*;

public class PromptRegistry {
    private static final PromptRegistry INSTANCE = new PromptRegistry();
    private final Map<String, String> prompts = new HashMap<>();

    private PromptRegistry() {}

    public static PromptRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void register(String id, String content) {
        prompts.put(id, content);
    }

    public synchronized void clear() {
        prompts.clear();
    }

    public synchronized Optional<String> get(String id) {
        return Optional.ofNullable(prompts.get(id));
    }
}
