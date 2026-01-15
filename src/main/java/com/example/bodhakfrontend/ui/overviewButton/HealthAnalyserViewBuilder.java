package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.Models.FixSuggestion;
import com.example.bodhakfrontend.Models.WarningRule;
import com.example.bodhakfrontend.projectAnalysis.fixes.FixSuggestionEngine;
import com.example.bodhakfrontend.projectAnalysis.warning.WarningBuilder;
import com.example.bodhakfrontend.ui.WarningCard;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;


public class HealthAnalyserViewBuilder {

    public Node build(ClassInfo info) {

        VBox root = new VBox(10);
        root.setPadding(new Insets(10));

        root.getChildren().add(title("🫀 Class Health"));
        root.getChildren().add(classHeader(info));
        root.getChildren().add(metrics(info));
        root.getChildren().add(warnings(info));
        root.getChildren().add(aiFixSection(info));
        return root;
    }
    private Node title(String text) {
        Label l = new Label(text);
        l.getStyleClass().add("label-title");
        return l;
    }
    private Node classHeader(ClassInfo info) {
        VBox box = new VBox(4);
        box.getChildren().add(new Label("Class: " + info.getClassName()));
        box.getChildren().add(new Label("Package: " + info.getPackageName()));
        return box;
    }
    private Node metrics(ClassInfo info) {
        VBox box = new VBox(6);

        box.getChildren().add(metric("LOC", info.getLinesOfCode(), info.getLinesOfCode() > 300));
        box.getChildren().add(metric("Methods", info.getMethods().size(), info.getMethods().size() > 15));
        box.getChildren().add(metric("Fan-In", info.getUsedBy().size(), info.getUsedBy().size() > 8));
        box.getChildren().add(metric("Fan-Out", info.getDependsOn().size(), info.getDependsOn().size() > 10));
//        box.getChildren().add(metric("Cycles", info.getCycleCount(), info.getCycleCount() > 0));
//        box.getChildren().add(metric("Hotspot", info.get, info.getHotspotScore() > 70));

        return box;
    }
    private Node metric(String name, int value, boolean warn) {
        HBox row = new HBox(10);

        Label label = new Label(name + ": " + value);
        if (warn) {
            label.getStyleClass().add("label-error");
        } else {
            label.getStyleClass().add("label-success");
        }

        row.getChildren().add(label);
        return row;
    }

    private Node warnings(ClassInfo info) {

        VBox box = new VBox(8);

        Label header = new Label("⚠ Health Warnings");
        header.getStyleClass().add("label-subtitle");
        box.getChildren().add(header);

        WarningBuilder warningBuilder = new WarningBuilder();
        var warnings = warningBuilder.buildWarnings(info);

        if (warnings.isEmpty()) {
            Label ok = new Label("✓ No major issues detected");
            ok.getStyleClass().add("label-success");
            box.getChildren().add(ok);
            return box;
        }

        for (WarningRule rule : warnings) {
            box.getChildren().add(WarningCard.create(rule));
        }

        return box;
    }



private Node thinkingPane(String text){
    ProgressIndicator spinner=new ProgressIndicator();
    spinner.setPrefSize(24,24);
    Label label = new Label(text);
    label.getStyleClass().add("thinking-label");
    HBox box = new HBox(10, spinner, label);
    box.setPadding(new Insets(10));
    return box;
}
    private void typeText(
            Label label,
            String text,
            int delayMillis
    ) {
        Timeline timeline = new Timeline();
        label.setText("");

        for (int i = 0; i < text.length(); i++) {
            final int index = i;
            timeline.getKeyFrames().add(
                    new KeyFrame(
                            Duration.millis(delayMillis * index),
                            e -> label.setText(text.substring(0, index + 1))
                    )
            );
        }

        timeline.play();
    }
    private Node animatedFixCard(FixSuggestion fix) {

        VBox box = new VBox(6);
        box.setPadding(new Insets(10));
        box.getStyleClass().add("ai-fix-card");

        Label title = new Label("💡 " + fix.getTitle());
        title.getStyleClass().add("ai-fix-title");

        Label desc = new Label();
        desc.setWrapText(true);

        Label example = new Label();
        example.getStyleClass().add("ai-fix-example");
        example.setWrapText(true);

        box.getChildren().addAll(title, desc, example);

        // ✨ typing animation
        Platform.runLater(() -> {
            typeText(desc, fix.getDescription(), 25);
            PauseTransition pause = new PauseTransition(Duration.seconds(1));
            pause.setOnFinished(e ->
                    typeText(example, "Example: " + fix.getExample(), 20)
            );
            pause.play();
        });

        return box;
    }
    private Node aiFixSection(ClassInfo info) {

        VBox container = new VBox(10);
        container.setPadding(new Insets(8));

        FixSuggestionEngine engine = new FixSuggestionEngine();
        List<FixSuggestion> allFixes = engine.suggestAll(info);

        if (allFixes.isEmpty()) {
            container.getChildren().add(
                    new Label("✓ No fix suggestions available")
            );
            return container;
        }

        AiFixSession session = new AiFixSession(allFixes);

        Button retry = new Button("🔄 Try another idea");
        retry.setDisable(true);

        VBox fixBox = new VBox(8);

        Runnable runAi = () -> {
            fixBox.getChildren().clear();
            retry.setDisable(true);

            Node thinking = thinkingPane("Thinking...");
            fixBox.getChildren().add(thinking);

            PauseTransition pause = new PauseTransition(Duration.seconds(1.2));
            pause.setOnFinished(e -> {
                fixBox.getChildren().clear();

                session.next().ifPresentOrElse(
                        fix -> {
                            fixBox.getChildren().add(animatedFixCard(fix));
                            retry.setDisable(false);
                        },
                        () -> {
                            retry.setDisable(true);
                            retry.setText("✓ No more ideas");
                            fixBox.getChildren().add(
                                    new Label("No more suggestions available.")
                            );
                        }
                );
            });

            pause.play();
        };

        retry.setOnAction(e -> runAi.run());

        // first run automatically
        runAi.run();

        container.getChildren().addAll(fixBox, retry);
        return container;
    }



}

class AiFixSession {
    private final List<FixSuggestion> remaining;

    AiFixSession(List<FixSuggestion> all) {
        this.remaining = new ArrayList<>(all);
        Collections.shuffle(this.remaining);
    }

    Optional<FixSuggestion> next() {
        if (remaining.isEmpty()) return Optional.empty();
        return Optional.of(remaining.remove(0));
    }
}


