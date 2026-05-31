package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.core.model.entity.*;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.model.entity.MethodFilter;
import com.example.bodhakfrontend.ui.helper.UiFeatures;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class MethodsViewBuilder {

    private final UiFeatures uiFeatures;
    private final ProjectInfo projectInfo;

    public MethodsViewBuilder(UiFeatures uiFeatures, ProjectInfo projectInfo) {
        this.uiFeatures = uiFeatures;
        this.projectInfo = projectInfo;
    }

    public Node build(String entityName) {
        EntityInfo entityInfo = projectInfo.getEntities().stream().filter(e -> e.getEntityName().equals(entityName)).findFirst().orElse(null);
        if (entityInfo == null) {
            return new Label("Entity not found: " + entityName);
        }
        
        List<MemberInfo> members = entityInfo.getMembers();
        
        VBox root = new VBox(8);
        root.setPadding(new Insets(8));
        VBox contentHolder = new VBox(6);
        
        renderFiltered(contentHolder, applyFilter(MethodFilter.ALL, members));
        
        Node summaryBar = buildSummaryBar(members, contentHolder);
        root.getChildren().addAll(summaryBar, contentHolder);
        
        return root;
    }

    private Node buildSummaryBar(List<MemberInfo> members, VBox contentHolder) {
        long totalMethods = members.stream().filter(m -> m.getKind() == MemberKind.METHOD || m.getKind() == MemberKind.FUNCTION).count();
        long totalConstructors = members.stream().filter(m -> m.getKind() == MemberKind.CONSTRUCTOR).count();
        long empty = members.stream().filter(m -> m.getStatementCount() == 0).count();
        long pub = members.stream().filter(MemberInfo::isPublic).count();
        long priv = members.stream().filter(m -> m.getModifiers().contains(ModifierKind.PRIVATE)).count();
        
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(6));
        bar.getStyleClass().add("summary-bar");

        bar.getChildren().addAll(
                badge("All", members.size(), MethodFilter.ALL, members, contentHolder),
                badge("🧩 Methods", totalMethods, MethodFilter.METHODS_ONLY, members, contentHolder),
                badge("🏗 Constructors", totalConstructors, MethodFilter.CONSTRUCTORS_ONLY, members, contentHolder),
                badge("🌐 Public", pub, MethodFilter.PUBLIC, members, contentHolder),
                badge("🔒 Private", priv, MethodFilter.PRIVATE, members, contentHolder),
                badge("⚠ Empty", empty, MethodFilter.EMPTY, members, contentHolder)
        );

        return bar;
    }

    private Label badge(String text, long value, MethodFilter filter, List<MemberInfo> members, VBox contentHolder) {
        Label l = new Label(text + ": " + value);
        l.getStyleClass().add("badge");

        l.setOnMouseClicked(event -> {
            contentHolder.getChildren().clear();
            renderFiltered(contentHolder, applyFilter(filter, members));
        });

        return l;
    }

    private Node buildConstructorRow(MemberInfo constructor) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(6));
        row.getStyleClass().add("file-row");

        Label signature = new Label("Constructor : " + buildMemberSignature(constructor));
        signature.setWrapText(true);
        HBox.setHgrow(signature, Priority.ALWAYS);

        row.getChildren().add(signature);

        row.setOnMouseClicked(e -> uiFeatures.openAndHighlight(
                constructor.getName(), constructor.getStartLine(), constructor.getStartColumn(), constructor.getSourceFile()
        ));

        return row;
    }

    private String buildMemberSignature(MemberInfo info) {
        String modifiers = info.getModifiers().stream()
                .map(Enum::name).map(String::toLowerCase).collect(Collectors.joining(" "));

        String params = info.getParameters().stream()
                .map(p -> p.getType() + " " + p.getName())
                .collect(Collectors.joining(", "));

        return String.format("%s %s(%s)", modifiers, info.getName(), params).trim();
    }

    private Node buildMethodRow(MemberInfo method) {
        VBox details = new VBox();
        details.setPadding(new Insets(6,0,0,16));

        details.getChildren().add(
                stringSection("Returns:", List.of(method.getReturnType() != null ? method.getReturnType() : "void"))
        );

        details.getChildren().add(
                stringSection(
                        "Parameters",
                        method.getParameters().stream()
                                .map(p -> p.getType() + " " + p.getName())
                                .toList()
                )
        );

        details.getChildren().add(
                dependencySection("Depends On", method.getCalledMembers())
        );

        TitledPane pane = new TitledPane(buildSignatureWithReturn(method), details);
        pane.setExpanded(false);
        pane.setAnimated(true);

        pane.setOnMouseClicked(e -> uiFeatures.openAndHighlight(
                method.getName(), method.getStartLine(), method.getStartColumn(), method.getSourceFile()
        ));

        return pane;
    }

    private String buildSignatureWithReturn(MemberInfo info) {
        return buildMemberSignature(info) + " : " + (info.getReturnType() != null ? info.getReturnType() : "void");
    }

    private Node stringSection(String title, List<String> items) {
        VBox box = new VBox(4);

        Label header = new Label(title);
        header.getStyleClass().add("label-subtitle");
        box.getChildren().add(header);

        if (items == null || items.isEmpty()) {
            box.getChildren().add(new Label("—"));
            return box;
        }

        for (String item : items) {
            Label l = new Label("• " + item);
            l.getStyleClass().add("label-muted");
            box.getChildren().add(l);
        }

        return box;
    }

    private Node dependencySection(String title, List<MethodCallInfo> items) {
        VBox root = new VBox(4);
        Label header = new Label(title);
        header.getStyleClass().add("label-subtitle");
        root.getChildren().add(header);

        if (items == null || items.isEmpty()) {
            root.getChildren().add(new Label("_"));
            return root;
        }

        var grouped = items.stream().collect(Collectors.groupingBy(MethodCallInfo::getType));
        addDependencyGroup(root, "Internal", grouped.get(MethodCallInfo.CallType.INTERNAL));
        addDependencyGroup(root, "External", grouped.get(MethodCallInfo.CallType.EXTERNAL));
        addDependencyGroup(root, "Library", grouped.get(MethodCallInfo.CallType.LIBRARY));

        return root;
    }

    private void addDependencyGroup(VBox parent, String title, List<MethodCallInfo> calls) {
        if (calls == null || calls.isEmpty()) return;

        VBox group = new VBox(4);
        group.setPadding(new Insets(0,0,0,12));
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("label-subtitle");
        group.getChildren().add(titleLabel);

        for (MethodCallInfo call : calls) {
            HBox row = new HBox(4);
            Label method = new Label(call.getMethodName() + "()");
            Label clazz = new Label("- " + call.getCalledEntity());

            switch (call.getType()) {
                case INTERNAL -> method.getStyleClass().add("method-dependency-internal");
                case EXTERNAL -> method.getStyleClass().add("method-dependency-external");
                case LIBRARY -> method.getStyleClass().add("method-dependency-library");
            }

            clazz.getStyleClass().add("label-muted");
            row.getChildren().addAll(new Label("•"), method, clazz);
            group.getChildren().add(row);
        }

        parent.getChildren().add(group);
    }

    private List<MemberInfo> applyFilter(MethodFilter filter, List<MemberInfo> members) {
        return switch (filter) {
            case METHODS_ONLY -> members.stream().filter(m -> m.getKind() == MemberKind.METHOD || m.getKind() == MemberKind.FUNCTION).toList();
            case CONSTRUCTORS_ONLY -> members.stream().filter(m -> m.getKind() == MemberKind.CONSTRUCTOR).toList();
            case PUBLIC -> members.stream().filter(MemberInfo::isPublic).toList();
            case PRIVATE -> members.stream().filter(m -> m.getModifiers().contains(ModifierKind.PRIVATE)).toList();
            case PROTECTED -> members.stream().filter(m -> m.getModifiers().contains(ModifierKind.PROTECTED)).toList();
            case EMPTY -> members.stream().filter(m -> m.getStatementCount() == 0).toList();
            case ALL -> new ArrayList<>(members);
        };
    }

    private void renderFiltered(VBox container, List<MemberInfo> items) {
        for (MemberInfo m : items) {
            if (m.getKind() == MemberKind.CONSTRUCTOR) {
                container.getChildren().add(buildConstructorRow(m));
            } else {
                container.getChildren().add(buildMethodRow(m));
            }
            container.getChildren().add(new Separator());
        }
    }
}