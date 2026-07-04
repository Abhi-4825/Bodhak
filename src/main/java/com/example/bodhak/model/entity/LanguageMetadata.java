package com.example.bodhak.model.entity;

import java.util.Map;

/**
 * Arbitrary key-value metadata container for language-specific metadata.
 */
public record LanguageMetadata(
        Map<String, Object> properties
) {

    public String languageId() {
        return (String) properties.getOrDefault("languageId", "unknown");
    }

    public String languageName() {
        return (String) properties.getOrDefault("languageName", languageId());
    }

    public String parser() {
        return (String) properties.getOrDefault("parser", "unknown");
    }

    public String version() {
        return (String) properties.getOrDefault("version", "");
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        Object value = properties.get(key);
        if (type.isInstance(value)) {
            return (T) value;
        }
        return null;
    }

    public Object get(String key) {
        return properties.get(key);
    }
}