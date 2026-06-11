package com.example.bodhakfrontend.core.model.runtime;

public record ServiceEndpoint(

        HttpMethod method,

        String path,

        String sourceEntity,

        EndpointSource source,

        boolean secured

) {
}
