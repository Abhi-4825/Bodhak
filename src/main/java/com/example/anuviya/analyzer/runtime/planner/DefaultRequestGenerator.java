package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.endpoint.ServiceEndpoint;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Standard default request generator fallback implementation.
 */
public class DefaultRequestGenerator implements RequestGenerator {
    @Override
    public Map<String, String> generateHeaders(ServiceEndpoint endpoint) {
        Map<String, String> headers = new HashMap<>();
        headers.put("Content-Type", "application/json");
        headers.put("Accept", "application/json");
        return headers;
    }

    @Override
    public Map<String, String> generatePathParams(ServiceEndpoint endpoint) {
        return Collections.emptyMap();
    }

    @Override
    public Map<String, String> generateQueryParams(ServiceEndpoint endpoint) {
        return Collections.emptyMap();
    }

    @Override
    public String generateBody(ServiceEndpoint endpoint) {
        if ("POST".equalsIgnoreCase(endpoint.httpMethod()) || "PUT".equalsIgnoreCase(endpoint.httpMethod())) {
            return "{}";
        }
        return null;
    }
}
