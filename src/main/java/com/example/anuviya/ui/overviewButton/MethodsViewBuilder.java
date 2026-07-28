package com.example.anuviya.ui.overviewButton;

import com.example.anuviya.ir.declaration.CallableDeclaration;
import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.entity.MethodFilter;
import com.example.anuviya.model.entity.ModifierKind;
import com.example.anuviya.ui.helper.UiFeatures;
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
    private final AnalysisContext context;

    public MethodsViewBuilder(UiFeatures uiFeatures, AnalysisContext context) {
        this.uiFeatures = uiFeatures;
        this.context = context;
    }

    public Node build(String entityName) {
        EntityInfo entityInfo = context.getEntities().stream().filter(e -> e.getEntityName().equals(entityName)).findFirst().orElse(null);
        if (entityInfo == null) {
            return new Label("Entity not found: " + entityName);
        }
        
        List<CallableDeclaration> members = entityInfo.getStructure().callableDeclarations();
        
        VBox root = new VBox(8);
        root.setPadding(new Insets(8));
        VBox contentHolder = new VBox(6);
        
        renderFiltered(contentHolder, applyFilter(MethodFilter.ALL, members), entityInfo.getSourceFile());
        
        Node summaryBar = buildSummaryBar(members, contentHolder, entityInfo.getSourceFile());
        root.getChildren().addAll(summaryBar, contentHolder);
        
        return root;
    }

    private Node buildSummaryBar(List<CallableDeclaration> members, VBox contentHolder, java.io.File sourceFile) {
        long totalMethods = members.stream().filter(m -> !m.name().equals("<init>")).count();
        long totalConstructors = members.stream().filter(m -> m.name().equals("<init>")).count();
        long empty = members.stream().filter(m -> m.body() == null || !(m.body() instanceof com.example.anuviya.ir.statement.BlockStatement bs) || bs.statements().isEmpty()).count();
        long pub = members.stream().filter(m -> m.modifiers().contains(ModifierKind.PUBLIC)).count();
        long priv = members.stream().filter(m -> m.modifiers().contains(ModifierKind.PRIVATE)).count();
        
        HBox bar = new HBox(12);
        bar.setPadding(new Insets(6));
        bar.getStyleClass().add("summary-bar");

        bar.getChildren().addAll(
                badge("All", members.size(), MethodFilter.ALL, members, contentHolder, sourceFile),
                badge("🧩 Methods", totalMethods, MethodFilter.METHODS_ONLY, members, contentHolder, sourceFile),
                badge("🏗 Constructors", totalConstructors, MethodFilter.CONSTRUCTORS_ONLY, members, contentHolder, sourceFile),
                badge("🌐 Public", pub, MethodFilter.PUBLIC, members, contentHolder, sourceFile),
                badge("🔒 Private", priv, MethodFilter.PRIVATE, members, contentHolder, sourceFile),
                badge("⚠ Empty", empty, MethodFilter.EMPTY, members, contentHolder, sourceFile)
        );

        return bar;
    }

    private Label badge(String text, long value, MethodFilter filter, List<CallableDeclaration> members, VBox contentHolder, java.io.File sourceFile) {
        Label l = new Label(text + ": " + value);
        l.getStyleClass().add("badge");

        l.setOnMouseClicked(event -> {
            contentHolder.getChildren().clear();
            renderFiltered(contentHolder, applyFilter(filter, members), sourceFile);
        });

        return l;
    }

    private Node buildConstructorRow(CallableDeclaration constructor, java.io.File sourceFile) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(6));
        row.getStyleClass().add("file-row");

        Label signature = new Label("Constructor : " + buildMemberSignature(constructor));
        signature.setWrapText(true);
        HBox.setHgrow(signature, Priority.ALWAYS);

        row.getChildren().add(signature);

        row.setOnMouseClicked(e -> uiFeatures.openAndHighlight(
                constructor.name(), constructor.sourceRange().startLine(), constructor.sourceRange().startColumn(), sourceFile
        ));

        return row;
    }

    private String buildMemberSignature(CallableDeclaration info) {
        String modifiers = info.modifiers().stream()
                .map(Enum::name).map(String::toLowerCase).collect(Collectors.joining(" "));

        String params = info.parameters().stream()
                .map(p -> (p.type() != null ? p.type().qualifiedName().toString() : "") + " " + p.name())
                .collect(Collectors.joining(", "));

        return String.format("%s %s(%s)", modifiers, info.name(), params).trim();
    }

    private Node buildMethodRow(CallableDeclaration method, java.io.File sourceFile) {
        VBox details = new VBox();
        details.setPadding(new Insets(6,0,0,16));

        details.getChildren().add(
                stringSection("Returns:", List.of(method.returnType() != null && method.returnType().qualifiedName() != null ? method.returnType().qualifiedName().toString() : "void"))
        );

        details.getChildren().add(
                stringSection(
                        "Parameters",
                        method.parameters().stream()
                                .map(p -> (p.type() != null ? p.type().qualifiedName().toString() : "") + " " + p.name())
                                .toList()
                )
        );

        details.getChildren().add(
                dependencySection("Depends On", List.of())
        );

        TitledPane pane = new TitledPane(buildSignatureWithReturn(method), details);
        pane.setExpanded(false);
        pane.setAnimated(true);

        pane.setOnMouseClicked(e -> uiFeatures.openAndHighlight(
                method.name(), method.sourceRange().startLine(), method.sourceRange().startColumn(), sourceFile
        ));

        return pane;
    }

    private String buildSignatureWithReturn(CallableDeclaration info) {
        return buildMemberSignature(info) + " : " + (info.returnType() != null && info.returnType().qualifiedName() != null ? info.returnType().qualifiedName().toString() : "void");
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

    private Node dependencySection(String title, List<?> items) {
        VBox root = new VBox(4);
        Label header = new Label(title);
        header.getStyleClass().add("label-subtitle");
        root.getChildren().add(header);

        root.getChildren().add(new Label("—"));

        return root;
    }

    private List<CallableDeclaration> applyFilter(MethodFilter filter, List<CallableDeclaration> members) {
        return switch (filter) {
            case METHODS_ONLY -> members.stream().filter(m -> !m.name().equals("<init>")).toList();
            case CONSTRUCTORS_ONLY -> members.stream().filter(m -> m.name().equals("<init>")).toList();
            case PUBLIC -> members.stream().filter(m -> m.modifiers().contains(ModifierKind.PUBLIC)).toList();
            case PRIVATE -> members.stream().filter(m -> m.modifiers().contains(ModifierKind.PRIVATE)).toList();
            case PROTECTED -> members.stream().filter(m -> m.modifiers().contains(ModifierKind.PROTECTED)).toList();
            case EMPTY -> members.stream().filter(m -> m.body() == null || !(m.body() instanceof com.example.anuviya.ir.statement.BlockStatement bs) || bs.statements().isEmpty()).toList();
            case ALL -> new ArrayList<>(members);
        };
    }

    private void renderFiltered(VBox container, List<CallableDeclaration> items, java.io.File sourceFile) {
        for (CallableDeclaration m : items) {
            if (m.name().equals("<init>")) {
                container.getChildren().add(buildConstructorRow(m, sourceFile));
            } else {
                container.getChildren().add(buildMethodRow(m, sourceFile));
            }
            container.getChildren().add(new Separator());
        }
    }
}
