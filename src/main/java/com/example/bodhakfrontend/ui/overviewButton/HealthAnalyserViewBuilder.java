package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.Models.ClassHealthInfo;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;


public class HealthAnalyserViewBuilder {

    public Node build(ClassHealthInfo info) {

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        root.getChildren().add(title("🫀 Class Health"));
        root.getChildren().add(classHeader(info));
        root.getChildren().add(metrics(info));
        root.getChildren().add(issues(info));

        return root;
    }
    private Node title(String text) {
        Label l = new Label(text);
        l.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
        return l;
    }
    private Node classHeader(ClassHealthInfo info) {
        VBox box = new VBox(4);
        box.getChildren().add(new Label("Class: " + info.getClassName()));
        box.getChildren().add(new Label("Package: " + info.getPackage()));
        return box;
    }
    private Node metrics(ClassHealthInfo info) {
        VBox box = new VBox(6);

        box.getChildren().add(metric("LOC", info.getLoc(), info.getLoc() > 300));
        box.getChildren().add(metric("Methods", info.getMethodCount(), info.getMethodCount() > 15));
        box.getChildren().add(metric("Fan-In", info.getIncomingDependencies(), info.getIncomingDependencies() > 8));
        box.getChildren().add(metric("Fan-Out", info.getOutgoingDependencies(), info.getOutgoingDependencies() > 10));
//        box.getChildren().add(metric("Cycles", info.getCycleCount(), info.getCycleCount() > 0));
//        box.getChildren().add(metric("Hotspot", info.get, info.getHotspotScore() > 70));

        return box;
    }
    private Node metric(String name, int value, boolean warn) {
        HBox row = new HBox(10);

        Label label = new Label(name + ": " + value);
        label.setStyle(warn
                ? "-fx-text-fill: #c62828;"
                : "-fx-text-fill: #2e7d32;"
        );

        row.getChildren().add(label);
        return row;
    }
    private Node issues(ClassHealthInfo info) {
        VBox box = new VBox(4);
        Label header = new Label("⚠ Health Issues");
        header.setStyle("-fx-font-weight: bold;");
        box.getChildren().add(header);

        if (info.getWarnings().isEmpty()) {
            box.getChildren().add(new Label("✓ No major issues"));
        } else {
            for (String r : info.getWarnings()) {
                box.getChildren().add(new Label("• " + r));
            }
        }

        return box;
    }





}

