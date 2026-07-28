package com.example.anuviya.ui.dependencyExplorer.components;

import com.example.anuviya.ui.dependencyExplorer.state.DependencyExplorerState;
import com.example.anuviya.ui.dependencyExplorer.state.DependencyGraphState;
import com.fxgraph.cells.AbstractCell;
import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.graph.Model;
import com.fxgraph.graph.NodeGestures;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.*;

public class DependencyGraphRenderer extends StackPane {

    private final DependencyExplorerState state;
    private final DependencyGraphState graphState;
    private final Graph graph = new Graph();
    private final CustomViewportGestures viewportGestures;

    private final DependencyGraphLayoutEngine layoutEngine = new DependencyGraphLayoutEngine();
    private final Map<String, Point2D> lastNodePositions = new HashMap<>();

    public DependencyGraphRenderer(DependencyExplorerState state) {
        this.state = state;
        this.graphState = state.getDependencyGraphState();

        setStyle("-fx-background-color: #0d141a; -fx-background-radius: 8;");

        // Bind canvas dimensions
        graph.getCanvas().prefWidthProperty().bind(widthProperty());
        graph.getCanvas().prefHeightProperty().bind(heightProperty());

        // Apply a layout clip to prevent nodes from spilling outside the box
        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(widthProperty());
        clip.heightProperty().bind(heightProperty());
        setClip(clip);

        // Bind custom gestures
        viewportGestures = new CustomViewportGestures(graph.getCanvas());
        graph.getCanvas().addEventHandler(javafx.scene.input.MouseEvent.MOUSE_PRESSED, viewportGestures.getOnMousePressedEventHandler());
        graph.getCanvas().addEventHandler(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, viewportGestures.getOnMouseDraggedEventHandler());
        // Do NOT register ScrollEvent.ANY handler so that mouse scroll-wheel events bubble up to the parent ScrollPane
        // Consume ZoomEvent and RotateEvent to fully block touchpad pinch-to-zoom gestures
        graph.getCanvas().addEventHandler(javafx.scene.input.ZoomEvent.ANY, javafx.event.Event::consume);
        graph.getCanvas().addEventHandler(javafx.scene.input.RotateEvent.ANY, javafx.event.Event::consume);
        graph.getCanvas().addEventHandler(javafx.scene.input.SwipeEvent.ANY, javafx.event.Event::consume);

        // Setup double-click to fit view
        graph.getCanvas().setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                fitToView();
            }
        });

        // Setup empty graph state placeholder
        Label placeholder = new Label("Select an entity from the Entity Browser");
        placeholder.setStyle("-fx-text-fill: #849396; -fx-font-size: 14px; -fx-font-family: 'JetBrains Mono';");
        placeholder.visibleProperty().bind(javafx.beans.binding.Bindings.isEmpty(graphState.getNodes()));

        getChildren().addAll(graph.getCanvas(), placeholder);

        // Dynamic Resize Layout & Centering listeners
        widthProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                runLayout();
                fitToView();
            }
        });
        heightProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.doubleValue() > 0) {
                runLayout();
                fitToView();
            }
        });
    }

    public Graph getGraph() {
        return graph;
    }

    public CustomViewportGestures getViewportGestures() {
        return viewportGestures;
    }

    public void zoom(double factor) {
        double currentScale = graph.getCanvas().getScale();
        double newScale = currentScale * factor;
        if (newScale >= 0.4 && newScale <= 2.5) {
            graph.getCanvas().setScale(newScale);
        }
    }

    public void fitToView() {
        Platform.runLater(() -> {
            double viewportWidth = getWidth();
            double viewportHeight = getHeight();
            if (viewportWidth <= 0 || viewportHeight <= 0) return;

            double minX = Double.MAX_VALUE;
            double maxX = -Double.MAX_VALUE;
            double minY = Double.MAX_VALUE;
            double maxY = -Double.MAX_VALUE;

            List<ICell> cells = graph.getModel().getAllCells();
            if (cells.isEmpty()) return;

            for (ICell cell : cells) {
                Region r = graph.getGraphic(cell);
                if (r == null) continue;
                double rx = r.getLayoutX();
                double ry = r.getLayoutY();
                double rw = r.getWidth() > 0 ? r.getWidth() : 120.0;
                double rh = r.getHeight() > 0 ? r.getHeight() : 44.0;

                if (rx < minX) minX = rx;
                if (rx + rw > maxX) maxX = rx + rw;
                if (ry < minY) minY = ry;
                if (ry + rh > maxY) maxY = ry + rh;
            }

            double graphWidth = maxX - minX;
            double graphHeight = maxY - minY;
            if (graphWidth <= 0) graphWidth = 100.0;
            if (graphHeight <= 0) graphHeight = 100.0;

            // Compute ideal scale factor with 18% padding
            double scaleX = viewportWidth / graphWidth;
            double scaleY = viewportHeight / graphHeight;
            double scale = Math.min(scaleX, scaleY) * 0.82;
            scale = Math.max(0.4, Math.min(2.5, scale));

            // Compute translation to center in viewport
            double graphCenterX = minX + graphWidth / 2.0;
            double graphCenterY = minY + graphHeight / 2.0;

            double targetX = viewportWidth / 2.0;
            double targetY = viewportHeight / 2.0;

            double translateX = targetX - graphCenterX * scale;
            double translateY = targetY - graphCenterY * scale;

            // Smooth animate scale & translate over 250ms
            Timeline timeline = new Timeline(
                new KeyFrame(Duration.millis(250),
                    new KeyValue(graph.getCanvas().scaleProperty(), scale),
                    new KeyValue(graph.getCanvas().translateXProperty(), translateX),
                    new KeyValue(graph.getCanvas().translateYProperty(), translateY)
                )
            );
            timeline.play();
        });
    }

    public void updateGraph() {
        Platform.runLater(() -> {
            Model model = graph.getModel();
            graph.beginUpdate();

            try {
                model.clear();
            } catch (Exception e) {
                // Fail-safe
            }

            Map<String, EntityCell> cellMap = new HashMap<>();

            for (DependencyGraphState.GraphNode node : graphState.getNodes()) {
                boolean isFocus = node.name().equals(graphState.getFocusEntityName());
                EntityCell cell = new EntityCell(node, isFocus);
                model.addCell(cell);
                cellMap.put(node.name(), cell);
            }

            for (DependencyGraphState.GraphEdge edge : graphState.getEdges()) {
                EntityCell src = cellMap.get(edge.source());
                EntityCell dst = cellMap.get(edge.target());
                if (src != null && dst != null) {
                    model.addEdge(new CustomEdge(src, dst, edge.type(), graphState.hoveredEdgeSourceProperty(), graphState.hoveredEdgeTargetProperty()));
                }
            }

            graph.endUpdate();

            // Run layout
            runLayout();

            // Setup drag gestures on nodes
            NodeGestures nodeGestures = new NodeGestures(graph);
            for (ICell cell : model.getAllCells()) {
                if (cell instanceof EntityCell) {
                    nodeGestures.makeDraggable(((EntityCell) cell).getGraphic(graph));
                }
            }

            // Perform auto centering and fitting to view
            fitToView();
        });
    }

    public void runLayout() {
        List<ICell> cells = graph.getModel().getAllCells();
        if (cells.isEmpty()) return;

        EntityCell focusCell = null;
        List<String> incomingNames = new ArrayList<>();
        List<String> outgoingNames = new ArrayList<>();

        for (ICell cell : cells) {
            if (cell instanceof EntityCell) {
                EntityCell ec = (EntityCell) cell;
                if (ec.isFocus()) {
                    focusCell = ec;
                } else {
                    boolean isOutgoing = false;
                    for (DependencyGraphState.GraphEdge edge : graphState.getEdges()) {
                        if (edge.source().equals(graphState.getFocusEntityName()) && edge.target().equals(ec.getName())) {
                            isOutgoing = true;
                            break;
                        }
                    }
                    if (isOutgoing) {
                        outgoingNames.add(ec.getName());
                    } else {
                        incomingNames.add(ec.getName());
                    }
                }
            }
        }

        double canvasWidth = getWidth() > 0 ? getWidth() : 700.0;
        double canvasHeight = getHeight() > 0 ? getHeight() : 500.0;

        String focusName = focusCell != null ? focusCell.getName() : "";
        Map<String, Point2D> targetPositions = layoutEngine.computeLayout(focusName, incomingNames, outgoingNames, canvasWidth, canvasHeight);

        double cx = canvasWidth / 2.0;
        double cy = canvasHeight / 2.0;

        Timeline transitionTimeline = new Timeline();

        for (ICell cell : cells) {
            if (cell instanceof EntityCell) {
                EntityCell ec = (EntityCell) cell;
                Region graphic = graph.getGraphic(ec);
                Point2D target = targetPositions.get(ec.getName());

                if (target != null && graphic != null) {
                    Point2D start = lastNodePositions.getOrDefault(ec.getName(), new Point2D(cx - 60, cy - 22));
                    
                    graphic.relocate(start.getX(), start.getY());

                    transitionTimeline.getKeyFrames().addAll(
                        new KeyFrame(Duration.millis(250),
                            new KeyValue(graphic.layoutXProperty(), target.getX()),
                            new KeyValue(graphic.layoutYProperty(), target.getY())
                        )
                    );

                    lastNodePositions.put(ec.getName(), target);
                }
            }
        }

        transitionTimeline.play();
    }

    public class EntityCell extends AbstractCell {
        private final DependencyGraphState.GraphNode node;
        private final boolean isFocus;
        private StackPane graphic;

        public EntityCell(DependencyGraphState.GraphNode node, boolean isFocus) {
            this.node = node;
            this.isFocus = isFocus;
        }

        public boolean isFocus() {
            return isFocus;
        }

        public String getName() {
            return node.name();
        }

        @Override
        public Region getGraphic(Graph graph) {
            if (graphic == null) {
                graphic = new StackPane();
                graphic.getStyleClass().add(isFocus ? "dd-node-focus" : "dd-node-normal");

                VBox box = new VBox(2);
                box.setAlignment(Pos.CENTER);

                Label nameLabel = new Label(node.simpleName());
                nameLabel.getStyleClass().add("dd-node-title");

                Label subLabel = new Label(node.kind().name());
                subLabel.getStyleClass().add("dd-node-sub");

                box.getChildren().addAll(nameLabel, subLabel);
                graphic.getChildren().add(box);

                graphic.setOnMouseClicked(e -> {
                    if (state.analysisContextProperty().get() != null) {
                        state.analysisContextProperty().get().findEntity(node.name())
                             .ifPresent(entity -> state.selectedEntityProperty().set(entity));
                    }
                });

                graphState.hoveredNodeNameProperty().addListener((obs, old, name) -> {
                    if (node.name().equals(name)) {
                        graphic.setStyle("-fx-effect: dropshadow(three-pass-box, #00daf3, 16, 0.6, 0, 0); -fx-border-color: #00daf3; -fx-border-width: 2;");
                    } else {
                        graphic.setStyle("");
                    }
                });
            }
            return graphic;
        }
    }
}
