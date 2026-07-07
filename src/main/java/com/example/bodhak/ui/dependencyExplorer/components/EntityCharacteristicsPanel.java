package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.quality.flag.EntityCharacteristics;
import com.example.bodhak.quality.flag.EntityFlag;
import com.example.bodhak.ui.dependencyExplorer.state.DependencyExplorerState;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Optional;
import java.util.Set;

public class EntityCharacteristicsPanel extends VBox {

    private final DependencyExplorerState state;
    private final VBox flagListBox = new VBox(8);

    public EntityCharacteristicsPanel(DependencyExplorerState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        getStyleClass().add("dd-card");
        setSpacing(12);
        setPadding(new Insets(16));
        setStyle("-fx-background-color: #151c23; -fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: rgba(132, 147, 150, 0.08);");

        Label title = new Label("ENTITY CHARACTERISTICS");
        title.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #849396;");

        flagListBox.setStyle("-fx-background-color: transparent;");
        VBox.setVgrow(flagListBox, Priority.ALWAYS);

        ScrollPane scroll = new ScrollPane(flagListBox);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(scroll, Priority.ALWAYS);

        getChildren().addAll(title, scroll);

        // Listen for selection changes to update characteristics flags
        state.selectedEntityProperty().addListener((obs, oldVal, newVal) -> updateCharacteristics(newVal));

        // Initial update
        updateCharacteristics(state.selectedEntityProperty().get());
    }

    private void updateCharacteristics(EntityInfo entity) {
        Platform.runLater(() -> {
            flagListBox.getChildren().clear();
            
            if (entity == null) {
                showPlaceholder("Select an entity to view architectural flags");
                return;
            }

            AnalysisContext context = state.analysisContextProperty().get();
            if (context == null) {
                showPlaceholder("Analysis context not loaded");
                return;
            }

            Optional<EntityCharacteristics> characteristicsOpt = context.findCharacteristics(entity.getEntityName());
            if (characteristicsOpt.isEmpty() || characteristicsOpt.get().flags().isEmpty()) {
                showPlaceholder("No warnings or anti-patterns detected");
                return;
            }

            Set<EntityFlag> flags = characteristicsOpt.get().flags();
            for (EntityFlag flag : flags) {
                flagListBox.getChildren().add(buildFlagBadge(flag));
            }
        });
    }

    private void showPlaceholder(String message) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(12, 16, 12, 16));
        box.setStyle("-fx-background-color: rgba(0, 230, 118, 0.04); -fx-border-color: rgba(0, 230, 118, 0.15); -fx-border-radius: 6; -fx-background-radius: 6;");

        Label dot = new Label("●");
        dot.setStyle("-fx-text-fill: #00e676; -fx-font-size: 11px;");

        Label msgLabel = new Label(message);
        msgLabel.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-text-fill: #849396;");

        box.getChildren().addAll(dot, msgLabel);
        flagListBox.getChildren().add(box);
    }

    private HBox buildFlagBadge(EntityFlag flag) {
        String displayName = resolveDisplayName(flag);
        String explanation = resolveExplanation(flag);
        String color = resolveColor(flag);

        HBox container = new HBox(12);
        container.setAlignment(Pos.CENTER_LEFT);
        container.setPadding(new Insets(8, 12, 8, 12));
        container.setStyle(String.format(
            "-fx-background-color: rgba(%s, 0.04); -fx-border-color: rgba(%s, 0.18); -fx-border-radius: 6; -fx-background-radius: 6;",
            hexToRgb(color), hexToRgb(color)
        ));

        Label dot = new Label("⚠");
        dot.setStyle(String.format("-fx-text-fill: %s; -fx-font-size: 12px; -fx-font-weight: bold;", color));

        VBox textCol = new VBox(2);
        HBox.setHgrow(textCol, Priority.ALWAYS);
        Label nameLbl = new Label(displayName.toUpperCase());
        nameLbl.setStyle(String.format("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: %s;", color));

        Label explLbl = new Label(explanation);
        explLbl.setStyle("-fx-font-size: 11px; -fx-text-fill: #bac9cc;");
        explLbl.setWrapText(true);
        explLbl.setMinWidth(0);
        textCol.getChildren().addAll(nameLbl, explLbl);

        container.getChildren().addAll(dot, textCol);
        return container;
    }

    private String resolveDisplayName(EntityFlag flag) {
        return switch (flag) {
            case OVERSIZED_ENTITY -> "Oversized Class";
            case HIGH_COUPLING -> "High Coupling";
            case HIGH_INSTABILITY -> "High Instability";
            case CIRCULAR_DEPENDENCY -> "Circular Dependency";
            case ANEMIC_DOMAIN -> "Anemic Domain Model";
            case HIGH_FAN_IN -> "High Fan-In";
            case HIGH_FAN_OUT -> "High Fan-Out";
            default -> flag.name().replace('_', ' ');
        };
    }

    private String resolveExplanation(EntityFlag flag) {
        return switch (flag) {
            case OVERSIZED_ENTITY ->
                    "This class is doing too much. It has too many lines, methods, or fields, making it hard to maintain and splitting it up is highly recommended.";

            case HIGH_COUPLING ->
                    "Strongly coupled to multiple other components, hindering isolated refactoring and reuse.";

            case HIGH_INSTABILITY ->
                    "This class is fragile because it heavily depends on other parts of the system without being used by them. Changing external code will likely break it.";

            case CIRCULAR_DEPENDENCY ->
                    "This class is trapped in a loop (A depends on B, which depends back on A). This locks the components together and makes them impossible to separate.";

            case ANEMIC_DOMAIN ->
                    "This class only holds data and has no business logic. It should swallow its own operations instead of letting other classes manipulate its fields.";

            case HIGH_FAN_IN ->
                    "This is a core pillar class that many other files depend on. Be extremely careful when editing it, as a single mistake here will ripple across the entire app.";

            case HIGH_FAN_OUT ->
                    "This class reaches out to too many external classes. This makes it heavily tangled, incredibly difficult to unit test, and prone to breaking.";

            default ->
                    "Architectural warning flag detected. Review this component's structure for potential design improvements.";
        };
    }


    private String resolveColor(EntityFlag flag) {
        return switch (flag) {
            case OVERSIZED_ENTITY -> "#ff4b4b";    // Red
            case HIGH_COUPLING -> "#ffa726";       // Orange
            case HIGH_INSTABILITY -> "#ffa726";    // Orange
            case CIRCULAR_DEPENDENCY -> "#ec407a";  // Pink
            case ANEMIC_DOMAIN -> "#fec931";        // Yellow
            case HIGH_FAN_IN -> "#00daf3";          // Cyan
            case HIGH_FAN_OUT -> "#ab47bc";         // Purple
            default -> "#849396";                   // Gray
        };
    }

    private String hexToRgb(String hex) {
        try {
            int r = Integer.valueOf(hex.substring(1, 3), 16);
            int g = Integer.valueOf(hex.substring(3, 5), 16);
            int b = Integer.valueOf(hex.substring(5, 7), 16);
            return r + "," + g + "," + b;
        } catch (Exception e) {
            return "132,147,150";
        }
    }
}
