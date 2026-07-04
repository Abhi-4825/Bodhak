package com.example.bodhak.ui.dependencyExplorer.components;

import java.util.List;

public class EntityPath {
    private final List<String> entities;
    private final List<PathConnection> connections;

    public EntityPath(List<String> entities, List<PathConnection> connections) {
        this.entities = entities;
        this.connections = connections;
    }

    public List<String> getEntities() {
        return entities;
    }

    public List<PathConnection> getConnections() {
        return connections;
    }

    public int getHops() {
        return entities.size() - 1;
    }
}
