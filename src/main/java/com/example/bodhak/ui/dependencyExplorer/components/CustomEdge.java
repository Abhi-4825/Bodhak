package com.example.bodhak.ui.dependencyExplorer.components;

import com.fxgraph.graph.Graph;
import com.fxgraph.graph.ICell;
import com.fxgraph.edges.AbstractEdge;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.StringProperty;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Region;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;

public class CustomEdge extends AbstractEdge {

    private final String type;
    private final StringProperty hoveredSrc;
    private final StringProperty hoveredDst;

    public CustomEdge(ICell source, ICell target, String type, StringProperty hoveredSrc, StringProperty hoveredDst) {
        super(source, target);
        this.type = type != null ? type : "IMPORT";
        this.hoveredSrc = hoveredSrc;
        this.hoveredDst = hoveredDst;
    }

    @Override
    public Region getGraphic(Graph graph) {
        return new CustomEdgeGraphic(graph, this, hoveredSrc, hoveredDst);
    }

    public String getType() {
        return type;
    }

    private static class CustomEdgeGraphic extends Pane {
        public CustomEdgeGraphic(Graph graph, CustomEdge edge, StringProperty hoveredSrc, StringProperty hoveredDst) {
            Region sourceGraphic = graph.getGraphic(edge.getSource());
            Region targetGraphic = graph.getGraphic(edge.getTarget());

            Line line = new Line();

            // Bind line ends to node centers
            line.startXProperty().bind(sourceGraphic.layoutXProperty().add(sourceGraphic.widthProperty().divide(2)));
            line.startYProperty().bind(sourceGraphic.layoutYProperty().add(sourceGraphic.heightProperty().divide(2)));
            line.endXProperty().bind(targetGraphic.layoutXProperty().add(targetGraphic.widthProperty().divide(2)));
            line.endYProperty().bind(targetGraphic.layoutYProperty().add(targetGraphic.heightProperty().divide(2)));

            String color = resolveColor(edge.getType());
            line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 1.5;");

            // Glow and hover highlighting via local mouse events
            line.setOnMouseEntered(e -> line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 3.0; -fx-effect: dropshadow(three-pass-box, " + color + ", 8, 0.5, 0, 0);"));
            line.setOnMouseExited(e -> {
                updateStyle(line, edge, color, hoveredSrc.get(), hoveredDst.get());
            });

            // Bind styling to shared hovered state properties
            hoveredSrc.addListener((obs, old, val) -> updateStyle(line, edge, color, hoveredSrc.get(), hoveredDst.get()));
            hoveredDst.addListener((obs, old, val) -> updateStyle(line, edge, color, hoveredSrc.get(), hoveredDst.get()));

            getChildren().add(line);

            // Triangle arrowhead (10px long, 10px wide) pointing along x-axis
            Polygon arrow = new Polygon(0, 0, -10, -5, -10, 5);
            arrow.setStyle("-fx-fill: " + color + ";");

            // Calculate arrow position (D = 55px offset from target center, just outside node box)
            DoubleBinding arrowX = new DoubleBinding() {
                {
                    super.bind(line.startXProperty(), line.startYProperty(), line.endXProperty(), line.endYProperty());
                }
                @Override
                protected double computeValue() {
                    double x1 = line.getStartX();
                    double y1 = line.getStartY();
                    double x2 = line.getEndX();
                    double y2 = line.getEndY();
                    double dx = x2 - x1;
                    double dy = y2 - y1;
                    double len = Math.sqrt(dx * dx + dy * dy);
                    if (len <= 0) return x2;
                    double ux = dx / len;
                    return x2 - 55.0 * ux;
                }
            };

            DoubleBinding arrowY = new DoubleBinding() {
                {
                    super.bind(line.startXProperty(), line.startYProperty(), line.endXProperty(), line.endYProperty());
                }
                @Override
                protected double computeValue() {
                    double x1 = line.getStartX();
                    double y1 = line.getStartY();
                    double x2 = line.getEndX();
                    double y2 = line.getEndY();
                    double dx = x2 - x1;
                    double dy = y2 - y1;
                    double len = Math.sqrt(dx * dx + dy * dy);
                    if (len <= 0) return y2;
                    double uy = dy / len;
                    return y2 - 55.0 * uy;
                }
            };

            DoubleBinding arrowAngle = new DoubleBinding() {
                {
                    super.bind(line.startXProperty(), line.startYProperty(), line.endXProperty(), line.endYProperty());
                }
                @Override
                protected double computeValue() {
                    double x1 = line.getStartX();
                    double y1 = line.getStartY();
                    double x2 = line.getEndX();
                    double y2 = line.getEndY();
                    return Math.toDegrees(Math.atan2(y2 - y1, x2 - x1));
                }
            };

            arrow.layoutXProperty().bind(arrowX);
            arrow.layoutYProperty().bind(arrowY);
            arrow.rotateProperty().bind(arrowAngle);

            getChildren().add(arrow);
        }

        private void updateStyle(Line line, CustomEdge edge, String color, String hSrc, String hDst) {
            String srcName = ((DependencyGraphRenderer.EntityCell) edge.getSource()).getName();
            String dstName = ((DependencyGraphRenderer.EntityCell) edge.getTarget()).getName();
            if (srcName.equals(hSrc) && dstName.equals(hDst)) {
                line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 3.5; -fx-effect: dropshadow(three-pass-box, " + color + ", 10, 0.6, 0, 0);");
            } else {
                line.setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 1.5;");
            }
        }

        private String resolveColor(String type) {
            return switch (type.toUpperCase()) {
                case "CALL" -> "#00daf3";       // Bright Blue (Method Call)
                case "MEMBER" -> "#00e676";     // Green (Field Reference)
                case "TYPE" -> "#ffa726";       // Orange (Type Reference)
                case "ANNOTATION" -> "#ec407a"; // Purple/Pink (Annotation)
                case "FRAMEWORK" -> "#4bf6ff";  // Cyan (Framework)
                default -> "#849396";           // Gray (Import / Other)
            };
        }
    }
}
