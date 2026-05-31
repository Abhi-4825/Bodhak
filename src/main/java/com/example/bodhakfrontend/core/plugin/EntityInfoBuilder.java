package com.example.bodhakfrontend.core.plugin;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;

import java.nio.file.Path;
import java.util.List;


public interface EntityInfoBuilder {

    List<EntityInfo> build(Path filePath);
    void invalidate(Path filePath);
}
