package com.example.anuviya.platform.onboarding;

import java.util.HashMap;
import java.util.Map;

public class OnboardingContext {
    private final Map<String, Object> data = new HashMap<>();

    public void put(String key, Object val) {
        data.put(key, val);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) data.get(key);
    }
}
