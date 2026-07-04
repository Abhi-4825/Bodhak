package com.example.bodhak.ui.dashboard.architecture.model;
import com.example.bodhak.model.namespace.NamespaceInfo;

import javafx.geometry.Point2D;
import java.util.HashMap;
import java.util.Map;

/**
 * Caches the graph layout, zoom, and pan state so that navigating between tabs
 * doesn't cause the graph to reset or rebuild from scratch.
 */
public class ArchitectureGraphState {
    
    // Zoom and Pan
    private double scale = 1.0;
    private double translateX = 0.0;
    private double translateY = 0.0;
    
    // Cache for Node positions per View Mode
    private Map<String, Point2D> dependencyPositions = new HashMap<>();
    private Map<String, Point2D> cyclePositions = new HashMap<>();
    private Map<String, Point2D> hotspotPositions = new HashMap<>();
    private Map<String, Point2D> architecturePositions = new HashMap<>();
    
    // Store namespace data for fast O(1) inspector updates
    private Map<String, NamespaceInfo> namespaceMap = new HashMap<>();
    
    // Currently selected node/namespace
    private String selectedNode = null;

    public double getScale() { return scale; }
    public void setScale(double scale) { this.scale = scale; }

    public Map<String, NamespaceInfo> getNamespaceMap() { return namespaceMap; }
    public void setNamespaceMap(Map<String, NamespaceInfo> namespaceMap) { this.namespaceMap = namespaceMap; }

    public double getTranslateX() { return translateX; }
    public void setTranslateX(double translateX) { this.translateX = translateX; }

    public double getTranslateY() { return translateY; }
    public void setTranslateY(double translateY) { this.translateY = translateY; }

    public String getSelectedNode() { return selectedNode; }
    public void setSelectedNode(String selectedNode) { this.selectedNode = selectedNode; }

    public Map<String, Point2D> getDependencyPositions() { return dependencyPositions; }
    public void setDependencyPositions(Map<String, Point2D> dependencyPositions) { this.dependencyPositions = dependencyPositions; }

    public Map<String, Point2D> getCyclePositions() { return cyclePositions; }
    public void setCyclePositions(Map<String, Point2D> cyclePositions) { this.cyclePositions = cyclePositions; }

    public Map<String, Point2D> getHotspotPositions() { return hotspotPositions; }
    public void setHotspotPositions(Map<String, Point2D> hotspotPositions) { this.hotspotPositions = hotspotPositions; }

    public Map<String, Point2D> getArchitecturePositions() { return architecturePositions; }
    public void setArchitecturePositions(Map<String, Point2D> architecturePositions) { this.architecturePositions = architecturePositions; }

    /**
     * Gets the cached positions for a specific view mode.
     */
    public Map<String, Point2D> getLayout(String mode) {
        return switch (mode) {
            case "ARCHITECTURE" -> architecturePositions;
            case "DEPENDENCY" -> dependencyPositions;
            case "CYCLES" -> cyclePositions;
            case "HOTSPOTS" -> hotspotPositions;
            default -> null;
        };
    }

    /**
     * Caches the positions for a specific view mode.
     */
    public void cacheLayout(String mode, Map<String, Point2D> positions) {
        switch (mode) {
            case "ARCHITECTURE" -> this.architecturePositions = new HashMap<>(positions);
            case "DEPENDENCY" -> this.dependencyPositions = new HashMap<>(positions);
            case "CYCLES" -> this.cyclePositions = new HashMap<>(positions);
            case "HOTSPOTS" -> this.hotspotPositions = new HashMap<>(positions);
        }
    }
    
    public boolean hasLayout(String mode) {
        Map<String, Point2D> layout = getLayout(mode);
        return layout != null && !layout.isEmpty();
    }

    public void clear() {
        scale = 1.0;
        translateX = 0.0;
        translateY = 0.0;
        selectedNode = null;
        architecturePositions.clear();
        dependencyPositions.clear();
        cyclePositions.clear();
        hotspotPositions.clear();
        namespaceMap=new HashMap<>();
    }
}
