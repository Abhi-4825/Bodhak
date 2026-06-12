package com.example.bodhakfrontend.ai.ollama;

public record OllamaRequest(String model,String prompt,boolean stream) {
}
