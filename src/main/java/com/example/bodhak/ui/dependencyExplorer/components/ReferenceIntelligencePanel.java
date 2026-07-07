package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.ui.dependencyExplorer.state.BreakdownState;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.chart.PieChart;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class ReferenceIntelligencePanel extends VBox {

    private final BreakdownState state;
    private final PieChart pieChart = new PieChart();

    public ReferenceIntelligencePanel(BreakdownState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        getStyleClass().add("dd-card");
        setSpacing(12);
        setPadding(new Insets(16));
        setStyle("-fx-background-color: #151c23; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(132, 147, 150, 0.08);");

        Label title = new Label("REFERENCE INTELLIGENCE & SEMANTIC BREAKDOWN");
        title.getStyleClass().add("dd-card-title");
        title.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #00daf3; -fx-letter-spacing: 0.1em;");

        // Split Content Layout
        HBox contentBox = new HBox(24);
        contentBox.setAlignment(Pos.CENTER_LEFT);
        VBox.setVgrow(contentBox, Priority.ALWAYS);

        // --- Left: Donut Chart ---
        pieChart.setLabelsVisible(false);
        pieChart.setLegendVisible(false); // Hidden since we have the list on the right
        pieChart.setStartAngle(90);
        pieChart.setPrefSize(140, 140);
        pieChart.setMinSize(140, 140);
        pieChart.setMaxSize(140, 140);

        StackPane chartContainer = new StackPane();
        chartContainer.setPrefSize(140, 140);
        chartContainer.getChildren().add(pieChart);

        Circle innerHole = new Circle(25);
        innerHole.setStyle("-fx-fill: #151c23;");
        chartContainer.getChildren().add(innerHole);

        // --- Right: VBox scorecard ---
        VBox scoreList = new VBox(6);
        scoreList.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(scoreList, Priority.ALWAYS);

        scoreList.getChildren().addAll(
            createRow("Method Calls", state.methodCallsProperty().asString(), "#00daf3", "Represents invocation of methods belonging to other classes."),
            createRow("Field References", state.fieldAccessesProperty().asString(), "#00e676", "Represents reading or writing member variables of other classes."),
            createRow("Type References", state.typeReferencesProperty().asString(), "#ffa726", "Represents references to type names in variable declarations, castings, etc."),
            createRow("Inheritance", state.otherProperty().asString(), "#ab47bc", "Represents sub-classing or interface implementations."),
            createRow("Annotations", state.annotationsProperty().asString(), "#ec407a", "Represents references to compiler or framework annotations."),
            createRow("Imports", state.importsProperty().asString(), "#26a69a", "Represents explicit imports of package namespaces.")
        );

        contentBox.getChildren().addAll(chartContainer, scoreList);
        getChildren().addAll(title, contentBox);

        // Hook up data sync
        updateChartData();
        state.methodCallsProperty().addListener((o, ov, nv) -> updateChartData());
        state.fieldAccessesProperty().addListener((o, ov, nv) -> updateChartData());
        state.typeReferencesProperty().addListener((o, ov, nv) -> updateChartData());
        state.annotationsProperty().addListener((o, ov, nv) -> updateChartData());
        state.otherProperty().addListener((o, ov, nv) -> updateChartData());
        state.importsProperty().addListener((o, ov, nv) -> updateChartData());

        // Dynamic slice coloring listener
        pieChart.getData().addListener((javafx.collections.ListChangeListener<PieChart.Data>) change -> {
            for (PieChart.Data d : pieChart.getData()) {
                String colorHex = getColorForName(d.getName());
                if (d.getNode() != null) {
                    d.getNode().setStyle("-fx-pie-color: " + colorHex + "; -fx-border-color: #151c23; -fx-border-width: 1px;");
                }
            }
        });
    }

    private void updateChartData() {
        ObservableList<PieChart.Data> data = FXCollections.observableArrayList(
                new PieChart.Data("Calls", state.methodCallsProperty().get()),
                new PieChart.Data("Fields", state.fieldAccessesProperty().get()),
                new PieChart.Data("Types", state.typeReferencesProperty().get()),
                new PieChart.Data("Inheritance", state.otherProperty().get()),
                new PieChart.Data("Annotations", state.annotationsProperty().get()),
                new PieChart.Data("Imports", state.importsProperty().get())
        );
        data.removeIf(d -> d.getPieValue() == 0);
        pieChart.setData(data);

        // Re-apply style for existing nodes
        for (PieChart.Data d : pieChart.getData()) {
            String colorHex = getColorForName(d.getName());
            if (d.getNode() != null) {
                d.getNode().setStyle("-fx-pie-color: " + colorHex + "; -fx-border-color: #151c23; -fx-border-width: 1px;");
            }
        }
    }

    private String getColorForName(String name) {
        return switch (name) {
            case "Calls" -> "#00daf3";
            case "Fields" -> "#00e676";
            case "Types" -> "#ffa726";
            case "Inheritance" -> "#ab47bc";
            case "Annotations" -> "#ec407a";
            case "Imports" -> "#26a69a";
            default -> "#849396";
        };
    }

    private HBox createRow(String labelStr, javafx.beans.value.ObservableValue<String> valProp, String colorHex, String descriptionText) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 2, 0));

        Circle dot = new Circle(4);
        dot.setFill(Color.web(colorHex));

        Label val = new Label();
        val.textProperty().bind(valProp);
        val.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #00daf3;");
        val.setMinWidth(Region.USE_PREF_SIZE);

        Label lbl = new Label(labelStr + ":");
        lbl.setStyle("-fx-font-family: 'Inter'; -fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #dce3ec;");
        lbl.setMinWidth(Region.USE_PREF_SIZE);

        Label desc = new Label(descriptionText);
        desc.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 10px; -fx-text-fill: #849396;");
        desc.setWrapText(true);
        desc.setMinWidth(0);
        HBox.setHgrow(desc, Priority.ALWAYS);

        row.getChildren().addAll(dot, val, lbl, desc);
        return row;
    }
}
