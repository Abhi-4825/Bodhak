package com.example.bodhakfrontend.core.plugin;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.model.runtime.ServiceEndpoint;

import java.nio.file.Path;
import java.util.List;

public interface EndpointDiscoveryProvider {
    List<ServiceEndpoint> discover(Path filePath);
}
