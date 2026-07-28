package com.example.anuviya.ui.dashboard.architecture.components;

import com.example.anuviya.ui.dashboard.architecture.graph.GraphViewMode;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;

import java.util.function.Consumer;

public class ArchitectureGraphToolbar extends HBox {

    public ArchitectureGraphToolbar(Consumer<GraphViewMode> listener) {
        setSpacing(0);
        setAlignment(Pos.CENTER);
        setStyle("-fx-background-color: #111b1c; -fx-background-radius: 20; -fx-border-color: #1a2324; -fx-border-radius: 20; -fx-border-width: 1;");
        setMaxWidth(USE_PREF_SIZE);
        setMaxHeight(USE_PREF_SIZE);
        setPadding(new Insets(4));

        ToggleGroup group = new ToggleGroup();

        for (GraphViewMode mode : GraphViewMode.values()) {
            ToggleButton btn = new ToggleButton(mode.displayName().replace(" ", "\n"));
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #849494; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 16; -fx-text-alignment: center; -fx-cursor: hand;");
            btn.setToggleGroup(group);

            if (mode == GraphViewMode.ARCHITECTURE) {
                btn.setSelected(true);
                btn.setStyle("-fx-background-color: #162a2d; -fx-text-fill: #4bf6ff; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 16; -fx-text-alignment: center; -fx-cursor: hand;");
            }

            btn.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (selected) {
                    listener.accept(mode);
                    btn.setStyle("-fx-background-color: #162a2d; -fx-text-fill: #4bf6ff; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 16; -fx-text-alignment: center; -fx-cursor: hand;");
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #849494; -fx-font-family: 'JetBrains Mono', monospace; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 8 16; -fx-background-radius: 16; -fx-text-alignment: center; -fx-cursor: hand;");
                }
            });

            getChildren().add(btn);
        }
    }
}
