package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.ui.dependencyExplorer.state.HealthState;
import javafx.beans.binding.Bindings;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

public class HealthPanel extends VBox {

    private final HealthState state;

    public HealthPanel(HealthState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        getStyleClass().add("dd-card");
        setSpacing(12);

        Label title = new Label("DEPENDENCY HEALTH");
        title.getStyleClass().add("dd-card-title");

        VBox list = new VBox(6);
        VBox.setVgrow(list, Priority.ALWAYS);

        list.getChildren().addAll(
                buildIndicatorRow("Circular Dependencies", state.cyclesCountProperty().asString(), state.cyclesCountProperty().greaterThan(0), "#ff4b4b"),
                buildIndicatorRow("High Fan-Out / Hub", Bindings.createStringBinding(() -> state.isHubProperty().get() ? "Yes" : "No", state.isHubProperty()), state.isHubProperty(), "#ffb020"),
                buildIndicatorRow("Instability Index", Bindings.format("%.2f", state.instabilityProperty()), state.instabilityProperty().greaterThan(0.7), "#ffb020"),
                buildIndicatorRow("Isolated / Orphan", Bindings.createStringBinding(() -> state.isOrphanProperty().get() ? "Yes" : "No", state.isOrphanProperty()), state.isOrphanProperty(), "#849396")
        );

        getChildren().addAll(title, list);
    }

    private HBox buildIndicatorRow(String labelStr, javafx.beans.value.ObservableValue<String> valProp, javafx.beans.value.ObservableBooleanValue isWarning, String warningColorHex) {
        HBox row = new HBox(8);
        row.setAlignment(Pos.CENTER_LEFT);

        Label dot = new Label("fiber_manual_record");
        dot.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 10px;");
        dot.setMinWidth(Region.USE_PREF_SIZE);
        
        // Listen to state changes to switch dot colors based on warning flags
        isWarning.addListener((obs, old, warning) -> {
            if (warning) {
                dot.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 10px; -fx-text-fill: " + warningColorHex + ";");
            } else {
                dot.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 10px; -fx-text-fill: #00e676;");
            }
        });
        // initial dot color
        if (isWarning.get()) {
            dot.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 10px; -fx-text-fill: " + warningColorHex + ";");
        } else {
            dot.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 10px; -fx-text-fill: #00e676;");
        }

        Label name = new Label(labelStr);
        name.setStyle("-fx-font-size: 11px; -fx-text-fill: #dce3ec;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label value = new Label();
        value.textProperty().bind(valProp);
        value.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-text-fill: #849396;");

        row.getChildren().addAll(dot, name, spacer, value);
        return row;
    }
}
