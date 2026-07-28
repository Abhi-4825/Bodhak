package com.example.anuviya.ui.analysisReport.diagnostic;

import com.example.anuviya.ui.analysisReport.state.DiagnosticsSummaryState;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.shape.Circle;

/**
 * Diagnostics Summary Card — Row 4, right panel.
 *
 * Shows 3 count boxes (Errors=red, Warnings=amber, Refactorings=cyan).
 * If all counts are zero, shows a green empty state.
 *
 * Contains NO calculations — all data from DiagnosticsSummaryState.
 */
public class DiagnosticsSummaryCard extends VBox {

    private final DiagnosticsSummaryState state;
    private final VBox contentBox = new VBox(20);
    private final VBox emptyState = new VBox(8);

    public DiagnosticsSummaryCard(DiagnosticsSummaryState state) {
        this.state = state;
        initialise();
        bindState();
    }

    private void initialise() {
        getStyleClass().add("ar-glass-card");
        setPadding(new Insets(22));
        setSpacing(16);
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);

        // ── Title ──────────────────────────────────────────────────
        Label title = new Label("Live Diagnostics");
        title.getStyleClass().add("ar-section-title");

        // ── Empty State ────────────────────────────────────────────
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(30, 0, 30, 0));
        
        Label emptyIcon = new Label("check_circle");
        emptyIcon.setStyle("-fx-font-family: 'Material Symbols Outlined'; " +
                           "-fx-font-size: 32px; -fx-text-fill: #56d69b;");
        Label emptyText = new Label("No issues detected");
        emptyText.getStyleClass().add("ar-empty-state-ok");
        
        emptyState.getChildren().addAll(emptyIcon, emptyText);

        // ── Count Boxes Row ────────────────────────────────────────
        HBox countsRow = new HBox(12);
        countsRow.setAlignment(Pos.CENTER);
        
        VBox errorBox = buildCountBox("ERRORS", "ar-diag-box-error", "#FF4B4B");
        Label errorVal = (Label) ((HBox) errorBox.getChildren().get(0)).getChildren().get(0);
        errorVal.textProperty().bind(state.totalErrorsProperty().asString());
        
        VBox warnBox = buildCountBox("WARNINGS", "ar-diag-box-warning", "#fec931");
        Label warnVal = (Label) ((HBox) warnBox.getChildren().get(0)).getChildren().get(0);
        warnVal.textProperty().bind(state.totalWarningsProperty().asString());
        
        VBox infoBox = buildCountBox("FILES", "ar-diag-box-info", "#00daf3");
        Label infoVal = (Label) ((HBox) infoBox.getChildren().get(0)).getChildren().get(0);
        infoVal.textProperty().bind(state.filesWithIssuesProperty().asString());

        countsRow.getChildren().addAll(errorBox, warnBox, infoBox);

        // ── Sample Messages List ───────────────────────────────────
        VBox messageList = new VBox(8);
        messageList.setAlignment(Pos.CENTER_LEFT);
        
        // We don't have individual messages in the current state yet, 
        // so we'll just show a placeholder summarizing the files affected.
        HBox summaryRow = new HBox(8);
        summaryRow.setAlignment(Pos.CENTER_LEFT);
        
        Circle dot = new Circle(4, javafx.scene.paint.Color.web("#849494"));
        
        Label summaryText = new Label();
        summaryText.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 13px; -fx-text-fill: #bac9cc;");
        summaryText.textProperty().bind(Bindings.createStringBinding(
                () -> "Issues found across " + state.filesWithIssuesProperty().get() + " files.",
                state.filesWithIssuesProperty()
        ));
        
        summaryRow.getChildren().addAll(dot, summaryText);
        messageList.getChildren().add(summaryRow);

        contentBox.getChildren().addAll(countsRow, messageList);

        getChildren().addAll(title, contentBox);
    }

    private void bindState() {
        // Toggle empty state visibility based on issue counts
        javafx.beans.binding.BooleanBinding hasIssues = Bindings.createBooleanBinding(
                () -> state.totalErrorsProperty().get() > 0 || state.totalWarningsProperty().get() > 0,
                state.totalErrorsProperty(),
                state.totalWarningsProperty()
        );

        contentBox.visibleProperty().bind(hasIssues);
        contentBox.managedProperty().bind(hasIssues);

        emptyState.visibleProperty().bind(hasIssues.not());
        emptyState.managedProperty().bind(hasIssues.not());
        
        // Add empty state if not there yet
        if (!getChildren().contains(emptyState)) {
            getChildren().add(emptyState);
        }
    }

    private VBox buildCountBox(String label, String cssClass, String colorHex) {
        VBox box = new VBox(4);
        box.getStyleClass().add(cssClass);
        box.setAlignment(Pos.CENTER);
        HBox.setHgrow(box, Priority.ALWAYS);
        box.setMaxWidth(Double.MAX_VALUE);

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER);
        
        Label val = new Label("0");
        val.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 24px; " +
                     "-fx-font-weight: 700; -fx-text-fill: " + colorHex + ";");
        top.getChildren().add(val);

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; " +
                     "-fx-font-weight: 700; -fx-letter-spacing: 0.15em; -fx-text-fill: " + colorHex + ";");

        box.getChildren().addAll(top, lbl);
        return box;
    }
}
