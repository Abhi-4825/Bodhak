package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
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
    private final ClassDependencyInfo  classDependencyInfo;
    private final MethodDependenciesViewBuilder methodDependenciesViewBuilder=new MethodDependenciesViewBuilder();
    public MethodsViewBuilder(UiFeatures uiFeatures, ClassDependencyInfo classDependencyInfo) {
        this.uiFeatures = uiFeatures;
        this.classDependencyInfo = classDependencyInfo;
    }

    public Node build(String className) {

        List<MethodsInfo> methods =
                classDependencyInfo.getMethods()
                        .getOrDefault(className, List.of());

        List<ConstructorInfo> constructorInfos =
                classDependencyInfo.getConstructorInfos()
                        .getOrDefault(className, List.of());

        VBox root = new VBox(8);
        root.setPadding(new Insets(8));

        VBox contentHolder = new VBox(6);

        renderFiltered(
                contentHolder,
                applyFilter(MethodFilter.ALL, methods, constructorInfos)
        );

        Node summaryBar = buildSummaryBar(
                methods,
                constructorInfos,
                contentHolder
        );

        root.getChildren().addAll(
                summaryBar,
                contentHolder
        );

        return root;
    }

    private Node buildSummaryBar(
            List<MethodsInfo> methods,
            List<ConstructorInfo> constructors,
            VBox contentHolder
    ) {
        int totalMethods = methods.size();
        int totalConstructors = constructors.size();

        long empty = methods.stream()
                .filter(MethodsInfo::isEmpty)
                .count();

        long pub = methods.stream()
                .filter(m -> m.getModifier().toString().contains("PUBLIC"))
                .count();

        long priv = methods.stream()
                .filter(m -> m.getModifier().toString().contains("PRIVATE"))
                .count();

        HBox bar = new HBox(12);
        bar.setPadding(new Insets(6));
        bar.getStyleClass().add("summary-bar");

        bar.getChildren().addAll(
                badge("All",totalMethods+totalConstructors,MethodFilter.ALL,methods,constructors,contentHolder),
                badge("🧩 Methods", totalMethods,MethodFilter.METHODS_ONLY,methods,constructors,contentHolder),
                badge("🏗 Constructors", totalConstructors,MethodFilter.CONSTRUCTORS_ONLY,methods,constructors,contentHolder),
                badge("🌐 Public", pub,MethodFilter.PUBLIC,methods,constructors,contentHolder),
                badge("🔒 Private", priv,MethodFilter.PRIVATE,methods,constructors,contentHolder),
                badge("⚠ Empty", empty,MethodFilter.EMPTY,methods,constructors,contentHolder)
        );

        return bar;
    }

    private Label badge(
            String text,
            long value,
            MethodFilter filter,
            List<MethodsInfo> methods,
            List<ConstructorInfo> constructors,
            VBox contentHolder
    ) {
        Label l = new Label(text + ": " + value);
        l.getStyleClass().add("badge");

        l.setOnMouseClicked(event -> {
            contentHolder.getChildren().clear();
            renderFiltered(
                    contentHolder,
                    applyFilter(filter, methods, constructors)
            );
        });

        return l;
    }


    private Node buildConstructorRow(ConstructorInfo constructor) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(6));
        row.getStyleClass().add("file-row");

        Label signature = new Label("Constructor : "+buildConstructorSignature(constructor));
        signature.setWrapText(true);
        HBox.setHgrow(signature, Priority.ALWAYS);

        row.getChildren().add(signature);

        row.setOnMouseClicked(event -> {
            uiFeatures.openAndHighlight(
                    constructor.getConstructorName(),
                    constructor.getStartLine(),
constructor.getStartColumn(),
                    constructor.getSourceFile()
            );
        });

        return row;
    }
    private String buildConstructorSignature(ConstructorInfo info) {

        String modifiers = info.getModifiers()
                .stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));

        String params = info.getParameters()
                .stream()
                .map(p -> p.getParameterType() + " " + p.getParameterName())
                .collect(Collectors.joining(", "));

        return String.format(
                "%s %s(%s)",
                modifiers,
                info.getConstructorName(),
                params
        ).trim();
    }




    private Node buildMethodRow(MethodsInfo method){
      VBox details = new VBox();
        details.setPadding(new Insets(6,0,0,16));
        // returnType
        details.getChildren().add(stringSection("Returns:",List.of(method.getReturnType())));
        //Paramters
        details.getChildren().add(stringSection("Parameters",method.getParameters().stream().map(p->p.getParameterName() + " "+p.getParameterType()).collect(Collectors.toList())));
        // depends On
        details.getChildren().add(dependencySection("Depends On",method.getCalledMethods()));
       TitledPane pane = new TitledPane(buildSignature(method), details);

      pane.setExpanded(false);
      pane.setAnimated(true);
      pane.setOnMouseClicked(event -> {
          uiFeatures.openAndHighlight(method.getMethodName(), method.getStartLine(),method.getStartColumn(), method.getSourceFile());
      });

      return  pane;
    }
    private String buildSignature(MethodsInfo info) {
        String modifiers = info.getModifier()
                .stream()
                .map(Enum::name)
                .map(String::toLowerCase)
                .collect(Collectors.joining(" "));
        String params = info.getParameters()
                .stream()
                .map(p -> p.getParameterType()+ " " + p.getParameterName())
                .collect(Collectors.joining(", "));
        return String.format(
                "%s %s(%s) : %s",
                modifiers,
                info.getMethodName(),
                params,
                info.getReturnType()
        ).trim();
    }

    private Node stringSection(String title,List<String> items){
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
    private Node dependencySection(String title,List<MethodCallInfo> items){
        VBox root = new VBox(4);
        Label header = new Label(title);
        header.getStyleClass().add("label-subtitle");
        root.getChildren().add(header);
        if (items == null || items.isEmpty()) {
            root.getChildren().add(new Label("_"));
            return root;
        }
        var grouped=items.stream().collect(Collectors.groupingBy(MethodCallInfo::getType));
        addDependencyGroup(root,"Internal",grouped.get(MethodCallInfo.CallType.INTERNAL));
        addDependencyGroup(root,"External",grouped.get(MethodCallInfo.CallType.EXTERNAL));
        addDependencyGroup(root,"Library",grouped.get(MethodCallInfo.CallType.LIBRARY));

         return root;
    }
    private void addDependencyGroup(VBox parent,String title,List<MethodCallInfo> calls){
        if(calls==null || calls.isEmpty()){return;
        }
        VBox group = new VBox(4);
        group.setPadding(new  Insets(0,0,0,12));
        Label titleLabel=new Label(title);
        titleLabel.getStyleClass().add("label-subtitle");
        group.getChildren().add(titleLabel);
        for (MethodCallInfo call : calls) {
            HBox row = new HBox(4);
            Label method = new Label(call.getMethodName() + "()");
            Label clazz=new Label("- "+call.getFromClass());
            switch (call.getType()) {
                case INTERNAL -> method.getStyleClass().add("method-dependency-internal");
                case EXTERNAL -> method.getStyleClass().add("method-dependency-external");
                case LIBRARY  -> method.getStyleClass().add("method-dependency-library");
            }

            clazz.getStyleClass().add("label-muted");
            row.getChildren().addAll(
                    new Label("•"),
                    method,
                    clazz
            );


            group.getChildren().add(row);

        }
        parent.getChildren().add(group);

    }
    private List<Object> applyFilter(
            MethodFilter filter,
            List<MethodsInfo> methods,
            List<ConstructorInfo> constructors
    ) {
        return switch (filter) {

            case METHODS_ONLY ->
                    new ArrayList<>(methods);

            case CONSTRUCTORS_ONLY ->
                    new ArrayList<>(constructors);

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

            case PROTECTED ->
                    methods.stream()
                            .filter(m -> m.getModifier().contains(ModifierKind.PROTECTED))
                            .map(m -> (Object) m)
                            .toList();

            case EMPTY ->
                    methods.stream()
                            .filter(MethodsInfo::isEmpty)
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
    private void renderFiltered(
            VBox container,
            List<Object> items
    ) {
        for (Object o : items) {
            if (o instanceof MethodsInfo m) {
                container.getChildren().add(buildMethodRow(m));
                container.getChildren().add(new Separator());
            } else if (o instanceof ConstructorInfo c) {
                container.getChildren().add(buildConstructorRow(c));
                container.getChildren().add(new Separator());
            }
        }
    }






}