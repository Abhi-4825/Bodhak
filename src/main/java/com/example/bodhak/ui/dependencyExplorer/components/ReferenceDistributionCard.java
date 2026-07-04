package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.ui.dependencyExplorer.state.BreakdownState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Circle;

public class ReferenceDistributionCard extends VBox {

    private final BreakdownState state;
    private final PieChart pieChart = new PieChart();

    public ReferenceDistributionCard(BreakdownState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        setSpacing(12);

        Label title = new Label("SEMANTIC REFERENCE BREAKDOWN");
        title.getStyleClass().add("dd-card-title");

        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(true);
        pieChart.setStartAngle(90);
        pieChart.setPrefHeight(150);

        StackPane chartContainer = new StackPane();
        chartContainer.getChildren().add(pieChart);

        // Center cutout to convert PieChart into a Donut Chart
        Circle innerHole = new Circle(30);
        innerHole.setStyle("-fx-fill: #151c23;");
        chartContainer.getChildren().add(innerHole);

        updateChartData();
        
        state.methodCallsProperty().addListener((o, ov, nv) -> updateChartData());
        state.fieldAccessesProperty().addListener((o, ov, nv) -> updateChartData());
        state.typeReferencesProperty().addListener((o, ov, nv) -> updateChartData());
        state.annotationsProperty().addListener((o, ov, nv) -> updateChartData());
        state.frameworkProperty().addListener((o, ov, nv) -> updateChartData());
        state.importsProperty().addListener((o, ov, nv) -> updateChartData());

        getChildren().addAll(title, chartContainer);
    }

    private void updateChartData() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Calls", state.methodCallsProperty().get()),
                new PieChart.Data("Fields", state.fieldAccessesProperty().get()),
                new PieChart.Data("Types", state.typeReferencesProperty().get()),
                new PieChart.Data("Annotations", state.annotationsProperty().get()),
                new PieChart.Data("Framework", state.frameworkProperty().get()),
                new PieChart.Data("Imports", state.importsProperty().get())
        );
        data.removeIf(d -> d.getPieValue() == 0);
        pieChart.setData(data);
    }
}
