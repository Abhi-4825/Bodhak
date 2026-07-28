package com.example.anuviya.analyzer.runtime.planner;

import com.example.anuviya.model.endpoint.ServiceEndpoint;
import java.util.Map;

/**
 * Extensible interface for composing request templates, headers, parameters, and payloads.
 */
public interface RequestGenerator {
    Map<String, String> generateHeaders(ServiceEndpoint endpoint);
    Map<String, String> generatePathParams(ServiceEndpoint endpoint);
    Map<String, String> generateQueryParams(ServiceEndpoint endpoint);
    String generateBody(ServiceEndpoint endpoint);
}
