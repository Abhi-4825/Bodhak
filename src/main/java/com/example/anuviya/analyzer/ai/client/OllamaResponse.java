package com.example.anuviya.analyzer.ai.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record OllamaResponse(String model,String response,boolean done) {
}
