package com.example.bodhakfrontend.ui.overviewButton;


import com.example.bodhakfrontend.Backend.models.incrementalModel.ClassInfoViewModel;
import com.example.bodhakfrontend.Backend.models.Class.*;
import com.example.bodhakfrontend.Models.MethodFilter;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class MethodView {

    private final UiFeatures uiFeatures;
    private final Map<String, ClassInfoViewModel> vmMap;
    private final VBox root = new VBox(8);
    private final VBox contentHolder = new VBox(6);

    private ClassInfoViewModel currentVm;
    private MethodFilter activeFilter = MethodFilter.ALL;

    public MethodView(UiFeatures uiFeatures, Map<String, ClassInfoViewModel> vmMap) {
        this.uiFeatures = uiFeatures;
        this.vmMap = vmMap;
        root.setPadding(new Insets(8));
        root.getChildren().add(contentHolder);
    }



    public Node show(String className) {
        unbind();
        bind(className);
        rebuild();
        return root;
    }



    private void bind(String className) {
        this.currentVm = vmMap.get(className);

        currentVm.getMethods().addListener(methodListener);
        currentVm.getConstructors().addListener(constructorListener);

        root.getChildren().setAll(
                buildSummaryBar(),
                contentHolder
        );
    }

    private void unbind() {
        if (currentVm == null) return;

        currentVm.getMethods().removeListener(methodListener);
        currentVm.getConstructors().removeListener(constructorListener);
    }

    private final ListChangeListener<MethodInfo> methodListener =
            c -> Platform.runLater(this::rebuild);

    private final ListChangeListener<ConstructorInfo> constructorListener =
            c -> Platform.runLater(this::rebuild);



    private void rebuild() {
        contentHolder.getChildren().clear();

        List<Object> items =
                applyFilter(
                        activeFilter,
                        currentVm.getMethods(),
                        currentVm.getConstructors()
                );

        render(items);
    }

    private Node buildSummaryBar() {
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(6));
        bar.getStyleClass().add("summary-bar");

        bar.getChildren().addAll(
                badge("All", MethodFilter.ALL),
                badge("🧩 Methods", MethodFilter.METHODS_ONLY),
                badge("🏗 Constructors", MethodFilter.CONSTRUCTORS_ONLY),
                badge("🌐 Public", MethodFilter.PUBLIC),
                badge("🔒 Private", MethodFilter.PRIVATE),
                badge("⚠ Empty", MethodFilter.EMPTY)
        );

        return bar;
    }

    private Label badge(String text, MethodFilter filter) {
        Label l = new Label(text);
        l.getStyleClass().add("badge");

        l.setOnMouseClicked(e -> {
            activeFilter = filter;
            rebuild();
        });

        return l;
    }
    private Node buildConstructorRow(ConstructorInfo c) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(6));
        row.getStyleClass().add("file-row");

        Label sig = new Label("Constructor : " + c.getConstructorName());
        HBox.setHgrow(sig, Priority.ALWAYS);

        row.getChildren().add(sig);

        row.setOnMouseClicked(e ->
                uiFeatures.openAndHighlight(
                        c.getConstructorName(),
                        c.getStartLine(),
                        c.getStartColumn(),
                        c.getSourceFile()
                )
        );

        return row;
    }

    private Node buildMethodRow(MethodInfo m) {
        VBox details = new VBox(4);
        details.setPadding(new Insets(6,0,0,16));

        details.getChildren().add(
                stringSection("Returns", List.of(m.getReturnType()))
        );

        details.getChildren().add(
                stringSection(
                        "Parameters",
                        m.getParameters().stream()
                                .map(p -> p.getParameterType() + " " + p.getParameterName())
                                .toList()
                )
        );

        TitledPane pane = new TitledPane(
                buildSignature(m),
                details
        );

        pane.setExpanded(false);

        pane.setOnMouseClicked(e ->
                uiFeatures.openAndHighlight(
                        m.getMethodName(),
                        m.getStartLine(),
                        m.getStartColumn(),
                        m.getSourceFile()
                )
        );

        return pane;
    }

    // ==================================================
    // HELPERS
    // ==================================================

    private String buildSignature(MethodInfo m) {
        String mods = m.getModifier()
                .stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));

        return mods + " " + m.getMethodName() + "() : " + m.getReturnType();
    }

    private Node stringSection(String title, List<String> items) {
        VBox box = new VBox(4);
        box.getChildren().add(new Label(title));

        if (items.isEmpty()) {
            box.getChildren().add(new Label("—"));
            return box;
        }

        items.forEach(s ->
                box.getChildren().add(new Label("• " + s))
        );

        return box;
    }

    private List<Object> applyFilter(
            MethodFilter filter,
            List<MethodInfo> methods,
            List<ConstructorInfo> constructors
    ) {
        return switch (filter) {
            case METHODS_ONLY -> new ArrayList<>(methods);
            case CONSTRUCTORS_ONLY -> new ArrayList<>(constructors);
            case PUBLIC ->
                    methods.stream()
                            .filter(m -> m.getModifier().contains(ModifierKind.PUBLIC))
                            .map(m -> (Object) m)
                            .toList();
            case PRIVATE ->
                    methods.stream()
                            .filter(m -> m.getModifier().contains(ModifierKind.PRIVATE))
                            .map(m -> (Object) m)
                            .toList();
            case PROTECTED -> null;
            case EMPTY ->
                    methods.stream()
                            .filter(MethodInfo::isBodyEmpty)
                            .map(m -> (Object) m)
                            .toList();
            case ALL -> {
                List<Object> all = new ArrayList<>();
                all.addAll(methods);
                all.addAll(constructors);
                yield all;
            }
        };
    }

    private void render(List<Object> items) {
        for (Object o : items) {
            if (o instanceof MethodInfo m) {
                contentHolder.getChildren().add(buildMethodRow(m));
            } else if (o instanceof ConstructorInfo c) {
                contentHolder.getChildren().add(buildConstructorRow(c));
            }
            contentHolder.getChildren().add(new Separator());
        }
    }
}

