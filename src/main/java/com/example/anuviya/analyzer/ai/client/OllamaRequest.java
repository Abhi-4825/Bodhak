package com.example.anuviya.analyzer.ai.client;

import java.util.Map;

public record OllamaRequest(
    String model,
    String prompt,
    boolean stream,
    String system,
    String format,
    Map<String, Object> options
) {
    public OllamaRequest(String model, String prompt, boolean stream) {
        this(model, prompt, stream, null, null, null);
    }
}
