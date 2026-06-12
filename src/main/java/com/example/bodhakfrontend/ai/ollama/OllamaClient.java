package com.example.bodhakfrontend.ai.ollama;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class OllamaClient {

    private static final String URL =
            "http://localhost:11434/api/generate";
 private  final HttpClient httpClient=HttpClient.newHttpClient();
 private final ObjectMapper mapper=new ObjectMapper();

    public String generate(
            String model,
            String prompt
    ) throws IOException, InterruptedException {

        OllamaRequest requestBody =
                new OllamaRequest(
                        model,
                        prompt,
                        false
                );

        String json =
                mapper.writeValueAsString(
                        requestBody
                );

        HttpRequest request =
                HttpRequest.newBuilder()
                        .uri(URI.create(URL))
                        .header(
                                "Content-Type",
                                "application/json"
                        )
                        .POST(
                                HttpRequest.BodyPublishers
                                        .ofString(json)
                        )
                        .build();

        HttpResponse<String> response =
                httpClient.send(
                        request,
                        HttpResponse.BodyHandlers.ofString()
                );

        OllamaResponse ollamaResponse =
                mapper.readValue(
                        response.body(),
                        OllamaResponse.class
                );

        return ollamaResponse.response();
    }

}
