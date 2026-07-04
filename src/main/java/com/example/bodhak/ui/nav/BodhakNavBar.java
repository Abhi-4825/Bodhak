package com.example.bodhak.ui.nav;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * IDE-style horizontal navigation strip for Bodhak.
 *
 * Renders as a segmented chip group in the top header bar.
 * When the user clicks a chip the active state animates in,
 * and the provided callback fires so the right panel can swap content.
 *
 * Adding a future tab = add an entry to {@link NavTab}. Nothing else changes here.
 */
public class BodhakNavBar {

    // ── Colour tokens ────────────────────────────────────────────────────────
    private static final String CLR_BG_INACTIVE  = "transparent";
    private static final String CLR_BG_ACTIVE    = "rgba(75,246,255,0.10)";
    private static final String CLR_TXT_INACTIVE = "#849494";
    private static final String CLR_TXT_ACTIVE   = "#4bf6ff";
    private static final String CLR_BORDER_ACTIVE = "#4bf6ff";

    // ── State ────────────────────────────────────────────────────────────────
    private NavTab              activeTab = NavTab.WORKSPACE;
    private Consumer<NavTab>    onTabChanged;

    private final Map<NavTab, Button> chips = new EnumMap<>(NavTab.class);
    private HBox                      root;

    // ── Public API ───────────────────────────────────────────────────────────

    /**
     * @param onTabChanged Called on FX thread whenever the user selects a new tab.
     */
    public BodhakNavBar(Consumer<NavTab> onTabChanged) {
        this.onTabChanged = onTabChanged;
    }

    /** Builds and returns the navigation HBox ready to embed in the top bar. */
    public Node build() {
        root = new HBox(2);
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(0, 0, 0, 20));

        // Thin left separator
        Region sep = new Region();
        sep.setPrefWidth(1);
        sep.setPrefHeight(18);
        sep.setStyle("-fx-background-color: #2f3637;");
        sep.setMaxHeight(18);
        HBox.setMargin(sep, new Insets(0, 18, 0, 0));
        root.getChildren().add(sep);

        for (NavTab tab : NavTab.values()) {
            Button chip = buildChip(tab);
            chips.put(tab, chip);
            root.getChildren().add(chip);
        }

        applyActive(activeTab);
        return root;
    }

    /** Programmatically selects a tab (e.g. after project load). */
    public void select(NavTab tab) {
        activate(tab);
    }

    public NavTab getActiveTab() {
        return activeTab;
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    private Button buildChip(NavTab tab) {
        Button btn = new Button(tab.getLabel());
        btn.setId("nav-chip-" + tab.name().toLowerCase());
        btn.getStyleClass().add("nav-chip");
        applyInactiveStyle(btn);

        btn.setOnMouseEntered(e -> {
            if (tab != activeTab) applyHoverStyle(btn);
        });
        btn.setOnMouseExited(e -> {
            if (tab != activeTab) applyInactiveStyle(btn);
        });
        btn.setOnAction(e -> activate(tab));

        return btn;
    }

    private void activate(NavTab tab) {
        if (tab == activeTab) return;
        NavTab previous = activeTab;
        activeTab = tab;

        applyInactiveStyle(chips.get(previous));
        applyActive(tab);

        if (onTabChanged != null) onTabChanged.accept(tab);
    }

    private void applyActive(NavTab tab) {
        Button btn = chips.get(tab);
        if (btn == null) return;

        btn.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-family: 'Inter';
            -fx-font-size: 12px;
            -fx-font-weight: bold;
            -fx-background-radius: 6;
            -fx-padding: 6 16;
            -fx-border-color: %s;
            -fx-border-width: 0 0 2 0;
            -fx-cursor: hand;
            """.formatted(CLR_BG_ACTIVE, CLR_TXT_ACTIVE, CLR_BORDER_ACTIVE));

        // Subtle glow on the active chip
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#4bf6ff", 0.4));
        glow.setRadius(8);
        glow.setSpread(0.1);
        btn.setEffect(glow);

        // Fade-in the glow
        FadeTransition ft = new FadeTransition(Duration.millis(180), btn);
        ft.setFromValue(0.7);
        ft.setToValue(1.0);
        ft.play();
    }

    private void applyInactiveStyle(Button btn) {
        btn.setEffect(null);
        btn.setStyle("""
            -fx-background-color: %s;
            -fx-text-fill: %s;
            -fx-font-family: 'Inter';
            -fx-font-size: 12px;
            -fx-font-weight: normal;
            -fx-background-radius: 6;
            -fx-padding: 6 16;
            -fx-border-color: transparent;
            -fx-border-width: 0 0 2 0;
            -fx-cursor: hand;
            """.formatted(CLR_BG_INACTIVE, CLR_TXT_INACTIVE));
    }

    private void applyHoverStyle(Button btn) {
        btn.setStyle("""
            -fx-background-color: rgba(255,255,255,0.05);
            -fx-text-fill: #dde4e5;
            -fx-font-family: 'Inter';
            -fx-font-size: 12px;
            -fx-font-weight: normal;
            -fx-background-radius: 6;
            -fx-padding: 6 16;
            -fx-border-color: transparent;
            -fx-border-width: 0 0 2 0;
            -fx-cursor: hand;
            """);
    }
}
