package com.example.bodhakfrontend.core.model.entity;

/**
 * A single parameter of a member (method, function, constructor).
 */
public class ParameterInfo {
    private final String name;
    /** Type as a raw string. Empty string if the language has no type annotations. */
    private final String type;

    public ParameterInfo(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() { return name; }
    public String getType() { return type; }

    @Override
    public String toString() {
        return type.isBlank() ? name : type + " " + name;
    }
}
