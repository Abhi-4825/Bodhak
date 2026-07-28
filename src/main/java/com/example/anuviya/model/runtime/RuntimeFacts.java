package com.example.anuviya.model.runtime;

import com.example.anuviya.model.endpoint.ServiceEndpoint;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Projects an immutable view of compiler facts relevant to runtime execution.
 */
public record RuntimeFacts(
        List<ServiceEndpoint> endpoints,
        Set<String> detectedTechnologies,
        Set<String> capabilities,
        String host,
        int port,
        List<String> protocols,
        Map<String, String> rawEvidence
) {
}
