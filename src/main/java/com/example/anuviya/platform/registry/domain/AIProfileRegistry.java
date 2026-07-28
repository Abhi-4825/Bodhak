package com.example.anuviya.platform.registry.domain;

import com.example.anuviya.analyzer.ai.platform.model.AIProfileDef;

import java.util.*;

public class AIProfileRegistry {
    private static final AIProfileRegistry INSTANCE = new AIProfileRegistry();
    private final Map<String, AIProfileDef> profiles = new LinkedHashMap<>();

    private AIProfileRegistry() {}

    public static AIProfileRegistry getInstance() {
        return INSTANCE;
    }

    public synchronized void register(AIProfileDef profile) {
        profiles.put(profile.id(), profile);
    }

    public synchronized void clear() {
        profiles.clear();
    }

    public synchronized Optional<AIProfileDef> get(String id) {
        return Optional.ofNullable(profiles.get(id));
    }

    public synchronized List<AIProfileDef> all() {
        return new ArrayList<>(profiles.values());
    }
}
