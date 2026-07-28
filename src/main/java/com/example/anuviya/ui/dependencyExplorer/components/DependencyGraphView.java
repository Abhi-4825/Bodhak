package com.example.anuviya.ui.dependencyExplorer.components;

import com.example.anuviya.ui.dependencyExplorer.state.DependencyExplorerState;
import com.example.anuviya.ui.dependencyExplorer.state.DependencyGraphState;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;

public class DependencyGraphView extends BorderPane {

    private final DependencyExplorerState state;
    private final DependencyGraphState graphState;
    private final DependencyGraphRenderer renderer;
    private final DependencyGraphLayoutEngine layoutEngine = new DependencyGraphLayoutEngine();

    public DependencyGraphView(DependencyExplorerState state) {
        this.state = state;
        this.graphState = state.getDependencyGraphState();
        this.renderer = new DependencyGraphRenderer(state);
        initialise();
    }

    private void initialise() {
        getStyleClass().add("dd-card");
        
        // Header with title
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setPadding(new Insets(0, 0, 10, 0));
        
        Label title = new Label("DEPENDENCY GRAPH ");
        title.getStyleClass().add("dd-card-title");
        
        header.getChildren().add(title);
        setTop(header);

        setCenter(renderer);

        // Bottom Layout: Toolbar on top, Legend below it
        VBox bottomLayout = new VBox(10);
        bottomLayout.setPadding(new Insets(10, 0, 0, 0));

        HBox toolbar = new HBox(8);
        toolbar.setAlignment(Pos.CENTER_RIGHT);

        Button zoomInBtn = createToolbarButton("+", "Zoom In", e -> renderer.zoom(1.1));
        Button zoomOutBtn = createToolbarButton("-", "Zoom Out", e -> renderer.zoom(0.9));
        Button fitBtn = createToolbarButton("Fit to View", "Recenter and reset zoom", e -> renderer.fitToView());
        
        toolbar.getChildren().addAll(zoomInBtn, zoomOutBtn, fitBtn);

        // Color Legends (with lines and arrows)
        HBox legends = new HBox(14);
        legends.setAlignment(Pos.CENTER);
        legends.setPadding(new Insets(5, 0, 0, 0));
        legends.getChildren().addAll(
                createLegendItem("Method Call", "#00daf3"),
                createLegendItem("Field Reference", "#00e676"),
                createLegendItem("Type Reference", "#ffa726"),
                createLegendItem("Inheritance", "#ab47bc"),
                createLegendItem("Annotation", "#ec407a"),
                createLegendItem("Framework", "#4bf6ff"),
                createLegendItem("Import", "#849396")
        );

        bottomLayout.getChildren().addAll(toolbar, legends);
        setBottom(bottomLayout);

        graphState.getNodes().addListener((ListChangeListener<DependencyGraphState.GraphNode>) c -> updateGraph());
        graphState.getEdges().addListener((ListChangeListener<DependencyGraphState.GraphEdge>) c -> updateGraph());
        
        updateGraph();
    }

    private Button createToolbarButton(String text, String tooltip, javafx.event.EventHandler<javafx.event.ActionEvent> handler) {
        Button btn = new Button(text);
        btn.setStyle("-fx-background-color: #151c23; -fx-text-fill: #00daf3; -fx-border-color: rgba(0, 218, 243, 0.2); -fx-border-radius: 4; -fx-cursor: hand; -fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold;");
        btn.setTooltip(new Tooltip(tooltip));
        btn.setOnAction(handler);
        return btn;
    }

    private HBox createLegendItem(String labelText, String hexColor) {
        HBox item = new HBox(6);
        item.setAlignment(Pos.CENTER_LEFT);

        // A tiny horizontal line with an arrowhead pointing right
        StackPane arrowPane = new StackPane();
        arrowPane.setPrefSize(20, 10);

        javafx.scene.shape.Line line = new javafx.scene.shape.Line(0, 5, 14, 5);
        line.setStyle("-fx-stroke: " + hexColor + "; -fx-stroke-width: 2;");

        javafx.scene.shape.Polygon arrow = new javafx.scene.shape.Polygon(0, 0, -5, -2.5, -5, 2.5);
        arrow.setStyle("-fx-fill: " + hexColor + ";");
        arrow.setTranslateX(7); // place arrow at line end

        arrowPane.getChildren().addAll(line, arrow);

        Label label = new Label(labelText);
        label.setStyle("-fx-text-fill: #849396; -fx-font-size: 10px; -fx-font-family: 'JetBrains Mono';");

        item.getChildren().addAll(arrowPane, label);
        return item;
    }

    private void updateGraph() {
        // Calculate dynamic height based on graph complexity
        List<String> incoming = new ArrayList<>();
        List<String> outgoing = new ArrayList<>();

        for (DependencyGraphState.GraphEdge edge : graphState.getEdges()) {
            if (edge.source().equals(graphState.getFocusEntityName())) {
                outgoing.add(edge.target());
            } else {
                incoming.add(edge.source());
            }
        }

        double prefHeight = layoutEngine.getPreferredHeight(incoming.size(), outgoing.size());
        Platform.runLater(() -> setPrefHeight(prefHeight));

        renderer.updateGraph();
    }
}
