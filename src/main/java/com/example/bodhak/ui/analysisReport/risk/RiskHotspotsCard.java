package com.example.bodhak.ui.analysisReport.risk;

import com.example.bodhak.ui.analysisReport.state.RiskHotspotsState;
import com.example.bodhak.ui.analysisReport.state.RiskHotspotsState.RiskEntry;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

/**
 * Risk Hotspots Card — Row 4, left panel.
 *
 * Shows inner glass cards for each hotspot entity.
 * Color-codes badges (Red for HIGH/CRITICAL, Amber for MEDIUM/ELEVATED, Cyan for LOW).
 * Rows resize to fit text dynamically.
 * Shows a green "No hotspots detected" empty state when empty.
 */
public class RiskHotspotsCard extends VBox {

    private final RiskHotspotsState state;
    private final VBox rowsContainer = new VBox(10);
    private final VBox emptyState = new VBox(8);

    public RiskHotspotsCard(RiskHotspotsState state) {
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
        Label title = new Label("Risk Hotspots");
        title.getStyleClass().add("ar-section-title");

        // ── Empty State ────────────────────────────────────────────
        emptyState.setAlignment(Pos.CENTER);
        emptyState.setPadding(new Insets(30, 0, 30, 0));
        
        Label emptyIcon = new Label("verified");
        emptyIcon.setStyle("-fx-font-family: 'Material Symbols Outlined'; " +
                           "-fx-font-size: 32px; -fx-text-fill: #56d69b;");
        Label emptyText = new Label("No hotspots detected");
        emptyText.getStyleClass().add("ar-empty-state-ok");
        
        emptyState.getChildren().addAll(emptyIcon, emptyText);

        // ── Scrollable Rows Container ──────────────────────────────
        ScrollPane scrollPane = new ScrollPane(rowsContainer);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().add("ar-scroll-pane");
        scrollPane.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(title, scrollPane);
    }

    private void bindState() {
        state.getHotspots().addListener((ListChangeListener<RiskEntry>) c -> rebuildRows());
        rebuildRows();
    }

    private void rebuildRows() {
        rowsContainer.getChildren().clear();
        
        if (state.getHotspots().isEmpty()) {
            rowsContainer.getChildren().add(emptyState);
            return;
        }

        for (RiskEntry entry : state.getHotspots()) {
            rowsContainer.getChildren().add(buildRow(entry));
        }
    }

    private HBox buildRow(RiskEntry entry) {
        HBox row = new HBox(12);
        row.getStyleClass().add("ar-hotspot-row");
        row.setAlignment(Pos.CENTER_LEFT);

        // Warning Icon
        String riskLevel = entry.riskLevel().toUpperCase();
        boolean isHigh = riskLevel.equals("HIGH") || riskLevel.equals("CRITICAL");
        boolean isMed  = riskLevel.equals("MEDIUM") || riskLevel.equals("ELEVATED");
        
        String iconColor = isHigh ? "#FF4B4B" : (isMed ? "#fec931" : "#00daf3");
        String iconName  = isHigh ? "error" : (isMed ? "warning" : "info");

        Label icon = new Label(iconName);
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; " +
                      "-fx-font-size: 24px; -fx-text-fill: " + iconColor + ";");

        // Entity Details
        VBox details = new VBox(2);
        HBox.setHgrow(details, Priority.ALWAYS);

        Label name = new Label(entry.entity());
        name.getStyleClass().add("ar-value-lg");
        name.setWrapText(true);

        Label flags = new Label(entry.activeFlags());
        flags.getStyleClass().add("ar-body-muted");
        flags.setWrapText(true);

        details.getChildren().addAll(name, flags);

        // Badge + LOC
        VBox rightSide = new VBox(8);
        rightSide.setAlignment(Pos.TOP_RIGHT);
        
        Label badge = new Label(riskLevel);
        badge.getStyleClass().add(isHigh ? "ar-badge-critical" : (isMed ? "ar-badge-elevated" : "ar-badge"));
        
        Label loc = new Label(entry.loc() + " LOC");
        loc.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; " +
                     "-fx-font-weight: 700; -fx-text-fill: #849494;");

        rightSide.getChildren().addAll(badge, loc);

        row.getChildren().addAll(icon, details, rightSide);
        return row;
    }
}
