package com.example.anuviya.ui.nav.workspace.impl;

import com.example.anuviya.platform.ServicePackage;
import com.example.anuviya.platform.environment.model.MemorySnapshot;

import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.*;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class LowMemoryAlertDialog extends StackPane {

    private final MemorySnapshot memory;
    private final ServicePackage modelPackage;
    private final Runnable onCancel;
    private final Runnable onProceed;
    private final Pane overlayTarget;
    private final VBox card;

    public LowMemoryAlertDialog(
            MemorySnapshot memory,
            ServicePackage modelPackage,
            Runnable onCancel,
            Runnable onProceed,
            Pane overlayTarget
    ) {
        this.memory = memory;
        this.modelPackage = modelPackage;
        this.onCancel = onCancel != null ? onCancel : () -> {};
        this.onProceed = onProceed;
        this.overlayTarget = overlayTarget;

        // Full screen backdrop setup
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.72);");
        setAlignment(Pos.CENTER);

        // Close on backdrop click (outside card)
        setOnMouseClicked(e -> dismiss(false));

        // Create card dialog
        this.card = buildCard();
        this.card.setOnMouseClicked(e -> e.consume()); // prevent backdrop dismiss when clicking card

        getChildren().add(card);

        // Escape key listener
        setFocusTraversable(true);
        setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ESCAPE) {
                dismiss(false);
            }
        });
    }

    private VBox buildCard() {
        VBox box = new VBox(18);
        box.setPrefWidth(480);
        box.setMaxWidth(480);
        box.setPadding(new Insets(24));
        box.setStyle(
            "-fx-background-color: #111819; " +
            "-fx-border-color: #1e2526; " +
            "-fx-border-width: 1; " +
            "-fx-background-radius: 12; " +
            "-fx-border-radius: 12; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(75, 246, 255, 0.04), 32, 0, 0, 0);"
        );

        // 1. Icon (Hexagon amber)
        Label icon = new Label("⬡");
        icon.setStyle("-fx-font-size: 48px; -fx-text-fill: #ffa726;");
        icon.setAlignment(Pos.CENTER);
        
        HBox iconBox = new HBox(icon);
        iconBox.setAlignment(Pos.CENTER);

        // 2. Title
        Label title = new Label(memory.isCriticallyLow() ? "Critical Memory Warning" : "Low Memory Warning");
        title.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        title.setAlignment(Pos.CENTER);

        VBox headerBox = new VBox(6, iconBox, title);
        headerBox.setAlignment(Pos.CENTER);

        // 3. Divider
        Region divider = new Region();
        divider.setPrefHeight(1);
        divider.setStyle("-fx-background-color: #1e2526;");

        // 4. Callout Box
        VBox callout = new VBox();
        callout.setPadding(new Insets(14, 16, 14, 16));
        callout.setStyle(
            "-fx-background-color: #141c1d; " +
            "-fx-border-color: transparent transparent transparent #ffa726; " +
            "-fx-border-width: 0 0 0 3; " +
            "-fx-background-radius: 6;"
        );

        String msgText = memory.isCriticallyLow()
            ? "Your system is critically low on available memory (" + memory.freePhysicalMb() + " MB free). Running AI analysis is blocked to protect system stability. Please close other applications to free up RAM."
            : "Your system is running low on available memory (" + memory.freePhysicalMb() + " MB free). Running AI analysis at this time may cause instability or crashes. We recommend closing other applications before proceeding.";

        Label msg = new Label(msgText);
        msg.setStyle("-fx-font-size: 13px; -fx-text-fill: #94a3b8; -fx-line-spacing: 4px;");
        msg.setWrapText(true);
        callout.getChildren().add(msg);

        // 5. Metric Chips
        HBox chipsBox = new HBox(12);
        chipsBox.setAlignment(Pos.CENTER);

        VBox chip1 = buildMetricChip("Available RAM", memory.freePhysicalMb() + " MB");
        HBox.setHgrow(chip1, Priority.ALWAYS);

        int reqGb = (modelPackage != null && modelPackage.requiredRamGb() > 0) ? modelPackage.requiredRamGb() : 8;
        VBox chip2 = buildMetricChip("Recommended", reqGb + " GB+");
        HBox.setHgrow(chip2, Priority.ALWAYS);

        chipsBox.getChildren().addAll(chip1, chip2);

        // 6. Action Buttons
        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: #1a2122; " +
            "-fx-text-fill: #849494; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 12px; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 8 20; " +
            "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dismiss(false));

        Button proceedBtn = new Button("Analyze Anyway");
        if (onProceed != null && !memory.isCriticallyLow()) {
            proceedBtn.setStyle(
                "-fx-background-color: linear-gradient(to right, #ffa726, #ff6b35); " +
                "-fx-text-fill: #0e1415; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-background-radius: 6; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: hand;"
            );
            proceedBtn.setOnAction(e -> dismiss(true));
        } else {
            proceedBtn.setDisable(true);
            proceedBtn.setStyle(
                "-fx-background-color: #242b2c; " +
                "-fx-text-fill: #566465; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 12px; " +
                "-fx-background-radius: 6; " +
                "-fx-padding: 8 20; " +
                "-fx-cursor: default;"
            );
            Tooltip.install(proceedBtn, new Tooltip("Disabled because system memory is critically low (< 512 MB free)"));
        }

        btnBox.getChildren().addAll(cancelBtn, proceedBtn);

        box.getChildren().addAll(headerBox, divider, callout, chipsBox, btnBox);
        return box;
    }

    private VBox buildMetricChip(String label, String value) {
        VBox chip = new VBox(4);
        chip.setPadding(new Insets(10, 16, 10, 16));
        chip.setStyle("-fx-background-color: #1a2122; -fx-background-radius: 6;");

        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #566465;");

        Label val = new Label(value);
        val.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        chip.getChildren().addAll(lbl, val);
        return chip;
    }

    public void show() {
        if (overlayTarget != null && !overlayTarget.getChildren().contains(this)) {
            setOpacity(0);
            card.setScaleX(0.95);
            card.setScaleY(0.95);

            overlayTarget.getChildren().add(this);
            requestFocus();

            FadeTransition ft = new FadeTransition(Duration.millis(200), this);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();

            ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
            st.setFromX(0.95);
            st.setFromY(0.95);
            st.setToX(1.0);
            st.setToY(1.0);
            st.play();
        }
    }

    private void dismiss(boolean proceed) {
        FadeTransition ft = new FadeTransition(Duration.millis(150), this);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            if (overlayTarget != null) {
                overlayTarget.getChildren().remove(this);
            }
            if (proceed && onProceed != null) {
                onProceed.run();
            } else {
                onCancel.run();
            }
        });
        ft.play();
    }
}
