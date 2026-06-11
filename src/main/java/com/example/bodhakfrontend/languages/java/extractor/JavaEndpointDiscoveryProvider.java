package com.example.bodhakfrontend.languages.java.extractor;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.runtime.ServiceEndpoint;
import com.example.bodhakfrontend.core.plugin.EndpointDiscoveryProvider;

import java.nio.file.Path;
import java.util.List;

public class JavaEndpointDiscoveryProvider implements EndpointDiscoveryProvider {
    @Override
    public List<ServiceEndpoint> discover(Path filePath) {
        return List.of();
    }
}
