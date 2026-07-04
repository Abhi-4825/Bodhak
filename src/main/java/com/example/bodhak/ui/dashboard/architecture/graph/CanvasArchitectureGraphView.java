package com.example.bodhak.ui.dashboard.architecture.graph;

import com.example.bodhak.model.namespace.NamespaceInfo;
import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.context.GraphSnapshot;
import com.example.bodhak.ui.dashboard.architecture.model.ArchitectureGraphState;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CanvasArchitectureGraphView extends Pane {
    private final GraphSnapshot snapshot;
    private final ProjectInfo projectInfo;
    private final ArchitectureGraphState state;
    private final GraphViewMode currentMode;
    private Consumer<String> nodeSelected;

    public void setNodeSelected(Consumer<String> nodeSelected) {
        this.nodeSelected = nodeSelected;
    }

    private final Canvas canvas;
    private GraphicsContext gc;

    // The graph data used for rendering depending on the mode
    private Map<String, Set<String>> displayGraph = new HashMap<>();
    private Map<String, Set<String>> displayReverseGraph = new HashMap<>();
    private Map<String, String> displayNodeNames = new HashMap<>();

    private Map<String, Point2D> positions = new HashMap<>();

    // Interaction state
    private double lastMouseX;
    private double lastMouseY;
    private String hoveredNode = null;

    public CanvasArchitectureGraphView(GraphSnapshot snapshot, ProjectInfo projectInfo, ArchitectureGraphState state, GraphViewMode mode, Consumer<String> nodeSelected) {
        this.snapshot = snapshot;
        this.projectInfo = projectInfo;
        this.state = state;
        this.currentMode = mode;
        this.nodeSelected = nodeSelected;

        this.canvas = new Canvas(800, 600);
        this.gc = canvas.getGraphicsContext2D();
        getChildren().add(canvas);

        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        canvas.widthProperty().addListener((obs, oldV, newV) -> render());
        canvas.heightProperty().addListener((obs, oldV, newV) -> render());

        buildGraphForMode();
        setupInteraction();

        // Load cached layout or compute new one
        if (state.hasLayout(mode.name())) {
            positions = state.getLayout(mode.name());
        } else {
            computeLayout();
            state.cacheLayout(mode.name(), positions);
        }

        Platform.runLater(this::render);
    }

    private void buildGraphForMode() {
        displayGraph.clear();
        displayReverseGraph.clear();
        displayNodeNames.clear();

        Map<String, NamespaceInfo> namespaces = state.getNamespaceMap();

        if (currentMode == GraphViewMode.ARCHITECTURE) {
            // Group by top-level domain
            String commonPrefix = findLongestCommonPrefix(namespaces.keySet());
            
            // Map raw namespace -> abstract domain
            Map<String, String> nsToDomain = new HashMap<>();
            Set<String> domains = new HashSet<>();
            
            for (String ns : namespaces.keySet()) {
                String domain = extractDomain(ns, commonPrefix);
                nsToDomain.put(ns, domain);
                domains.add(domain);
                displayNodeNames.put(domain, domain);
            }

            for (String domain : domains) {
                displayGraph.put(domain, new HashSet<>());
                displayReverseGraph.put(domain, new HashSet<>());
            }

            // Collapse dependencies into domains
            for (NamespaceInfo info : namespaces.values()) {
                String fromDomain = nsToDomain.get(info.getNamespaceName());
                for (String depNs : info.getDependsOn()) {
                    String toDomain = nsToDomain.get(depNs);
                    if (toDomain != null && !fromDomain.equals(toDomain)) {
                        displayGraph.get(fromDomain).add(toDomain);
                        displayReverseGraph.get(toDomain).add(fromDomain);
                    }
                }
            }

        } else if (currentMode == GraphViewMode.HOTSPOTS) {
            // Sort by risk, take top 15
            List<NamespaceInfo> sorted = namespaces.values().stream()
                    .sorted(Comparator.comparingDouble(NamespaceInfo::getRiskScore).reversed())
                    .limit(15)
                    .collect(Collectors.toList());

            Set<String> hotspotNames = sorted.stream().map(NamespaceInfo::getNamespaceName).collect(Collectors.toSet());

            for (NamespaceInfo info : sorted) {
                String ns = info.getNamespaceName();
                displayNodeNames.put(ns, shorten(ns));
                displayGraph.put(ns, new HashSet<>());
                displayReverseGraph.put(ns, new HashSet<>());
                
                // Only link hotspots to other hotspots
                for (String dep : info.getDependsOn()) {
                    if (hotspotNames.contains(dep)) {
                        displayGraph.get(ns).add(dep);
                    }
                }
                for (String user : info.getUsedBy()) {
                    if (hotspotNames.contains(user)) {
                        displayReverseGraph.get(ns).add(user);
                    }
                }
            }
        } else {
            // DEPENDENCY and CYCLES mode
            for (NamespaceInfo info : namespaces.values()) {
                if (currentMode == GraphViewMode.CYCLES && !info.isPartOfCycle()) {
                    continue; // Skip non-cycle nodes
                }
                
                String ns = info.getNamespaceName();
                displayNodeNames.put(ns, shorten(ns));
                displayGraph.put(ns, new HashSet<>());
                displayReverseGraph.put(ns, new HashSet<>());
            }

            for (String ns : displayNodeNames.keySet()) {
                NamespaceInfo info = namespaces.get(ns);
                if (info != null) {
                    for (String dep : info.getDependsOn()) {
                        if (displayNodeNames.containsKey(dep)) {
                            displayGraph.get(ns).add(dep);
                            displayReverseGraph.get(dep).add(ns);
                        }
                    }
                }
            }
        }
    }

    private String findLongestCommonPrefix(Set<String> strings) {
        if (strings.isEmpty()) return "";
        String prefix = strings.iterator().next();
        for (String s : strings) {
            while (!s.startsWith(prefix)) {
                prefix = prefix.substring(0, prefix.length() - 1);
                if (prefix.isEmpty()) return "";
            }
        }
        // Only split on package boundaries
        int lastDot = prefix.lastIndexOf('.');
        if (lastDot >= 0) {
            return prefix.substring(0, lastDot + 1);
        }
        return prefix;
    }

    private String extractDomain(String namespace, String commonPrefix) {
        String remaining = namespace;
        if (namespace.startsWith(commonPrefix)) {
            remaining = namespace.substring(commonPrefix.length());
        }
        int firstDot = remaining.indexOf('.');
        if (firstDot > 0) {
            return remaining.substring(0, firstDot);
        }
        return remaining.isEmpty() ? "root" : remaining;
    }

    private String shorten(String fullName) {
        int idx = fullName.lastIndexOf('.');
        if (idx < 0) return fullName;
        return fullName.substring(idx + 1);
    }

    private void computeLayout() {
        positions.clear();
        if (displayGraph.isEmpty()) return;

        List<String> nodes = new ArrayList<>(displayGraph.keySet());
        
        if (currentMode == GraphViewMode.ARCHITECTURE) {
            // Left to right DAG layout based on topological depth
            Map<String, Integer> depthMap = new HashMap<>();
            for (String node : nodes) {
                depthMap.put(node, calculateDepth(node, new HashSet<>()));
            }
            
            Map<Integer, List<String>> layers = new HashMap<>();
            for (String node : nodes) {
                int d = depthMap.get(node);
                layers.putIfAbsent(d, new ArrayList<>());
                layers.get(d).add(node);
            }
            
            double startX = 150;
            double layerWidth = 250;
            for (Map.Entry<Integer, List<String>> entry : layers.entrySet()) {
                int layer = entry.getKey();
                List<String> layerNodes = entry.getValue();
                double x = startX + (layer * layerWidth);
                double startY = 300 - (layerNodes.size() * 100) / 2.0;
                
                for (int i = 0; i < layerNodes.size(); i++) {
                    positions.put(layerNodes.get(i), new Point2D(x, startY + (i * 100)));
                }
            }
        } else {
            // Radial or Spring Layout
            double centerX = 400;
            double centerY = 300;
            double radius = 250;
            int total = nodes.size();
            for (int i = 0; i < total; i++) {
                double angle = (2 * Math.PI * i) / total;
                positions.put(nodes.get(i), new Point2D(centerX + radius * Math.cos(angle), centerY + radius * Math.sin(angle)));
            }
        }
    }

    private int calculateDepth(String node, Set<String> visited) {
        if (visited.contains(node)) return 0; // Break cycles
        visited.add(node);
        int maxDepth = 0;
        for (String inDep : displayReverseGraph.getOrDefault(node, Set.of())) {
            maxDepth = Math.max(maxDepth, 1 + calculateDepth(inDep, new HashSet<>(visited)));
        }
        return maxDepth;
    }

    private void setupInteraction() {
        canvas.setOnMousePressed(e -> {
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            
            String clickedNode = getNodeAt(e.getX(), e.getY());
            if (clickedNode != null) {
                state.setSelectedNode(clickedNode);
                
                // If in Architecture View, clickedNode is a "domain", so we clear the namespace inspector
                // because domains don't map to a single NamespaceInfo. 
                // Or we can just pass null for domains.
                if (currentMode == GraphViewMode.ARCHITECTURE) {
                    nodeSelected.accept(null); // Clear inspector for domains
                } else {
                    nodeSelected.accept(clickedNode); // Pass namespace name
                }
                render();
            }
        });

        canvas.setOnMouseDragged(e -> {
            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;
            state.setTranslateX(state.getTranslateX() + dx);
            state.setTranslateY(state.getTranslateY() + dy);
            lastMouseX = e.getX();
            lastMouseY = e.getY();
            render();
        });

        canvas.setOnScroll(e -> {
            double zoomFactor = 1.05;
            if (e.getDeltaY() < 0) zoomFactor = 1 / zoomFactor;
            state.setScale(Math.max(0.1, Math.min(state.getScale() * zoomFactor, 5.0)));
            render();
        });
        
        canvas.setOnMouseMoved(e -> {
            String node = getNodeAt(e.getX(), e.getY());
            if (!Objects.equals(node, hoveredNode)) {
                hoveredNode = node;
                render();
            }
        });
    }
    
    private String getNodeAt(double x, double y) {
        double tx = (x - state.getTranslateX() - canvas.getWidth()/2) / state.getScale() + canvas.getWidth()/2;
        double ty = (y - state.getTranslateY() - canvas.getHeight()/2) / state.getScale() + canvas.getHeight()/2;
        
        for (Map.Entry<String, Point2D> entry : positions.entrySet()) {
            Point2D p = entry.getValue();
            double radius = calculateRadius(entry.getKey());
            if (Math.hypot(p.getX() - tx, p.getY() - ty) < radius + 5) {
                return entry.getKey();
            }
        }
        return null;
    }

    private double calculateRadius(String node) {
        if (currentMode == GraphViewMode.HOTSPOTS) {
            NamespaceInfo info = state.getNamespaceMap().get(node);
            if (info != null) {
                return 16 + Math.min((info.getFanIn() + info.getFanOut()) * 2, 40);
            }
        } else if (currentMode == GraphViewMode.ARCHITECTURE) {
            return 30; // Larger nodes for domains
        }
        return 24;
    }

    private void render() {
        double w = canvas.getWidth();
        double h = canvas.getHeight();
        if (w <= 0 || h <= 0) return;

        gc.clearRect(0, 0, w, h);
        gc.save();
        
        // Apply camera transforms
        gc.translate(state.getTranslateX() + w/2, state.getTranslateY() + h/2);
        gc.scale(state.getScale(), state.getScale());
        gc.translate(-w/2, -h/2);

        // Draw edges
        gc.setLineWidth(1.5);
        for (String source : positions.keySet()) {
            Point2D p1 = positions.get(source);
            for (String target : displayGraph.getOrDefault(source, Set.of())) {
                Point2D p2 = positions.get(target);
                if (p2 == null) continue;
                
                // Highlight edges connected to selected/hovered node
                if (source.equals(state.getSelectedNode()) || target.equals(state.getSelectedNode()) || 
                    source.equals(hoveredNode) || target.equals(hoveredNode)) {
                    gc.setStroke(Color.web("#8bfd91"));
                    gc.setGlobalAlpha(0.8);
                } else {
                    gc.setStroke(Color.web("#849494"));
                    gc.setGlobalAlpha(0.2);
                }
                
                gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
            }
        }

        // Draw nodes
        gc.setGlobalAlpha(1.0);
        for (Map.Entry<String, Point2D> entry : positions.entrySet()) {
            String node = entry.getKey();
            Point2D p = entry.getValue();
            
            double radius = calculateRadius(node);
            boolean isSelected = node.equals(state.getSelectedNode());
            boolean isHovered = node.equals(hoveredNode);
            
            String color = getColorForNode(node);
            
            gc.setFill(Color.web("#0C151B"));
            gc.fillOval(p.getX() - radius, p.getY() - radius, radius * 2, radius * 2);
            
            gc.setStroke(Color.web(color));
            gc.setLineWidth(isSelected || isHovered ? 4 : 2);
            gc.strokeOval(p.getX() - radius, p.getY() - radius, radius * 2, radius * 2);
            
            gc.setFill(isSelected ? Color.web("#dde4e5") : Color.web(color));
            gc.setFont(Font.font("JetBrains Mono", FontWeight.BOLD, 11));
            
            String displayName = displayNodeNames.getOrDefault(node, node);
            gc.fillText(displayName, p.getX() - (displayName.length() * 3.5), p.getY() + radius + 15);
        }

        gc.restore();
    }

    private String getColorForNode(String node) {
        if (currentMode == GraphViewMode.ARCHITECTURE) {
            return "#dde4e5"; // White/Grey for domains
        }
        NamespaceInfo info = state.getNamespaceMap().get(node);
        if (info != null) {
            if (info.isPartOfCycle()) return "#ff8a80"; // Red
            if (info.getRiskScore() > 50) return "#ffd54f"; // Yellow
        }
        return "#4bf6ff"; // Cyan
    }
}
