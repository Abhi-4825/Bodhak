package com.example.anuviya.ui.analysisReport.uiComponent;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Standard visual shell that wraps any AnalysisInspectorContent.
 * Provides the dark glass header, title, badges, close button, and scrollable body.
 */
public class AnalysisInspectorContainer extends VBox {

    private final Button closeButton;
    private final ScrollPane scrollPane;
    private final AnalysisInspectorContent content;

    public AnalysisInspectorContainer(AnalysisInspectorContent content, Runnable onClose) {
        this.content = content;

        getStyleClass().addAll("ar-glass-card", "ar-inspector-container");
        setPadding(new Insets(24));
        setSpacing(0);

        // ── Header Row ─────────────────────────────────────────────
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(header, new Insets(0, 0, 16, 0));

        // Icon
        Node icon = content.getIcon();
        if (icon != null) {
            header.getChildren().add(icon);
        }

        // Title
        Label titleLabel = new Label(content.getTitle());
        titleLabel.getStyleClass().add("ar-section-title");
        header.getChildren().add(titleLabel);

        // Confidence Badge
        String confText = content.getConfidenceText();
        if (confText != null && !confText.isEmpty()) {
            Label badge = new Label(confText);
            badge.getStyleClass().add("ar-badge");
            header.getChildren().add(badge);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        header.getChildren().add(spacer);

        // Close Button
        closeButton = new Button("✕");
        closeButton.getStyleClass().add("ar-close-button");
        closeButton.setFocusTraversable(true);
        closeButton.setOnAction(e -> onClose.run());
        header.getChildren().add(closeButton);

        // ── Divider ───────────────────────────────────────────────
        Region divider = new Region();
        divider.getStyleClass().add("ar-divider");
        VBox.setMargin(divider, new Insets(0, 0, 20, 0));

        // ── Body ScrollPane ─────────────────────────────────────────
        scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().setAll("ar-scroll-pane");
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(header, divider, scrollPane);

        // Responsive max sizes to prevent stretching on large displays
        setMaxWidth(1200);
        setMaxHeight(850);
    }

    public AnalysisInspectorContent getContent() {
        return content;
    }
}
