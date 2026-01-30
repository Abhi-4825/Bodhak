package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ConstructorInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.MethodInfo;
import com.example.bodhakfrontend.IncrementalPart.model.incrementalModel.ClassInfoViewModel;
import com.example.bodhakfrontend.Models.FixSuggestion;
import com.example.bodhakfrontend.Models.WarningRule;
import com.example.bodhakfrontend.projectAnalysis.fixes.FixSuggestionEngine;
import com.example.bodhakfrontend.ui.WarningCard;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.ListChangeListener;
import javafx.collections.SetChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.util.*;

public class HealthAnalyserView {

    private final Map<String, ClassInfoViewModel> vmMap;
    private final VBox root = new VBox(12);
    private ClassInfoViewModel currentVm;

    public HealthAnalyserView(Map<String, ClassInfoViewModel> vmMap) {
        this.vmMap = vmMap;
        root.setPadding(new Insets(12));
    }

    // ==================================================
    // PUBLIC API
    // ==================================================

    public Node show(String className) {
        unbind();
        currentVm = vmMap.get(className);

        if (currentVm == null) {
            root.getChildren().setAll(
                    new Label("Class not found: " + className)
            );
            return root;
        }

        bind();
        rebuild();
        return root;
    }


    private final ChangeListener<Number> numberListener =
            (obs, oldV, newV) -> Platform.runLater(this::rebuild);

    private final ListChangeListener<MethodInfo> methodListener =
            c -> Platform.runLater(this::rebuild);

    private final ListChangeListener<ConstructorInfo> constructorListener =
            c -> Platform.runLater(this::rebuild);

    private final ListChangeListener<WarningRule> warningListener =
            change -> Platform.runLater(this::rebuild);


    private final SetChangeListener<String> dependencyListener =
            c -> Platform.runLater(this::rebuild);


    private void bind() {
        currentVm.linesOfCodeProperty().addListener(numberListener);

        currentVm.getMethods().addListener(methodListener);
        currentVm.getConstructors().addListener(constructorListener);
        currentVm.getWarnings().addListener(warningListener);

        currentVm.getDependsOn().addListener(dependencyListener);
        currentVm.getUsedBy().addListener(dependencyListener);
    }

    private void unbind() {
        if (currentVm == null) return;

        currentVm.linesOfCodeProperty().removeListener(numberListener);

        currentVm.getMethods().removeListener(methodListener);
        currentVm.getConstructors().removeListener(constructorListener);
        currentVm.getWarnings().removeListener(warningListener);

        currentVm.getDependsOn().removeListener(dependencyListener);
        currentVm.getUsedBy().removeListener(dependencyListener);
    }


    private void rebuild() {
        root.getChildren().setAll(
                title(),
                classCard(),
                metricGrid()
                ,warningsSection(),
                aiFixSection()
        );
    }

    private Node title() {
        Label l = new Label("🫀 Class Health");
        l.getStyleClass().add("label-title");
        return l;
    }

    private Node classCard() {
        VBox card = cardBox();

        Label classLabel = new Label();
        classLabel.textProperty().bind(
                currentVm.simpleNameProperty().concat("")
        );

        Label pkg = new Label("Package: " + currentVm.getPackageName());

        card.getChildren().addAll(
                new Label("Class:"),
                classLabel,
                pkg
        );

        return card;
    }

    private Node metricGrid() {
        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        grid.add(metricCard("LOC", currentVm.getLinesOfCode(), 300), 0, 0);
        grid.add(metricCard("Methods", currentVm.getMethods().size(), 15), 1, 0);
        grid.add(metricCard("Fan-In", currentVm.getUsedBy().size(), 8), 0, 1);
        grid.add(metricCard("Fan-Out", currentVm.getDependsOn().size(), 10), 1, 1);

        return grid;
    }

    private Node metricCard(String name, int value, int warnLimit) {
        VBox card = cardBox();

        Label title = new Label(name);
        Label val = new Label(String.valueOf(value));

        val.getStyleClass().add(
                value > warnLimit ? "metric-bad" : "metric-good"
        );

        card.getChildren().addAll(title, val);
        return card;
    }

    private VBox cardBox() {
        VBox v = new VBox(6);
        v.setPadding(new Insets(10));
        v.getStyleClass().add("card");
        return v;
    }


    private Node warningsSection() {
        VBox box = cardBox();
        box.getChildren().add(new Label("⚠ Warnings"));

        if (currentVm.getWarnings().isEmpty()) {
            box.getChildren().add(new Label("✓ No warnings detected"));
            return box;
        }

        currentVm.getWarnings()
                .forEach(w -> box.getChildren().add(
                        WarningCard.create(w)
                ));

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

        VBox card = new VBox(10);
        card.getStyleClass().add("ai-fix-card");

        // ── Title ───────────────────────────
        Label title = new Label("💡 " + fix.getTitle());
        title.getStyleClass().add("ai-fix-title");

        // ── Description (typed) ─────────────
        Label desc = new Label();
        desc.setWrapText(true);
        desc.getStyleClass().add("ai-fix-desc");

        // ── Example (typed) ─────────────────
        Label example = new Label();
        example.setWrapText(true);
        example.getStyleClass().add("ai-fix-example");

        Label exampleHeader = new Label("Example");
        exampleHeader.getStyleClass().add("label-subtitle");

        VBox exampleBox = new VBox(4, exampleHeader, example);
        exampleBox.getStyleClass().add("ai-fix-example-box");

        card.getChildren().addAll(
                title,
                desc,
                exampleBox
        );

        // ── ✨ Typing Animation (UNCHANGED LOGIC) ──
        Platform.runLater(() -> {
            String resolvedDesc=resolvePlaceholders(fix.getDescription());
            String resolvedExample=resolvePlaceholders(fix.getExample());
            typeText(desc, resolvedDesc, 20);

            PauseTransition pause = new PauseTransition(Duration.seconds(0.8));
            pause.setOnFinished(e ->
                    typeText(example, resolvedExample, 15)
            );
            pause.play();
        });

        return card;
    }



    private Node aiFixSection() {

        VBox card = cardBox();

        Label header = new Label("🤖 AI Fix Suggestions");
        header.getStyleClass().add("label-subtitle");

        FixSuggestionEngine engine = new FixSuggestionEngine();
        List<FixSuggestion> allFixes =
                engine.suggestAll(currentVm.toClassInfo());

        VBox fixBox = new VBox(8);

        if (allFixes.isEmpty()) {
            fixBox.getChildren().add(
                    new Label("✓ No fix suggestions available")
            );
            card.getChildren().addAll(header, fixBox);
            return card;
        }

        AiFixSession session = new AiFixSession(allFixes);

        Button retry = new Button("🔄 Try another idea");
        retry.setDisable(true);

        Runnable runAi = () -> {
            fixBox.getChildren().clear();
            retry.setDisable(true);

            fixBox.getChildren().add(thinkingPane("Thinking..."));

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

        card.getChildren().addAll(
                header,
                fixBox,
                retry
        );

        return card;
    }


    // resolve classname in json
    private String resolvePlaceholders(String text) {
        if (text == null) return "";
        return text.replace("{{className}}", currentVm.simpleNameProperty().get());
    }




}


class AiFixSession {
    private final List<FixSuggestion> remaining;

    AiFixSession(List<FixSuggestion> all) {
        remaining = new ArrayList<>(all);
        Collections.shuffle(remaining);
    }

    Optional<FixSuggestion> next() {
        if (remaining.isEmpty()) return Optional.empty();
        return Optional.of(remaining.remove(0));
    }
}

