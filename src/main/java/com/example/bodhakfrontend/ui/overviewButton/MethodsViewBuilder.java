package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.Models.ClassDependencyInfo;
import com.example.bodhakfrontend.Models.ConstructorInfo;
import com.example.bodhakfrontend.Models.MethodCallInfo;
import com.example.bodhakfrontend.Models.MethodsInfo;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
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


    public Node build(String className
                   ){
        List<MethodsInfo> methods =
                classDependencyInfo.getMethods()
                        .getOrDefault(className, List.of());

        List<ConstructorInfo> constructorInfos =
                classDependencyInfo.getConstructorInfos()
                        .getOrDefault(className, List.of());
     VBox root = new VBox();
     root.setPadding(new Insets(8));
     root.getChildren().add(buildSummaryBar(methods,constructorInfos));
        root.getChildren().add(buildConstructorList(constructorInfos));
     root.getChildren().add(buildMethodList(methods));


             return root;


 }
    private Node buildSummaryBar(
            List<MethodsInfo> methods,
            List<ConstructorInfo> constructors
    ) {
        int totalMethods = methods.size();
        int totalConstructors = constructors.size();

        long empty = methods.stream()
                .filter(m -> m.getEndLine() <= m.getStartLine())
                .count();

        long pub = methods.stream()
                .filter(m -> m.getModifier().toString().contains("PUBLIC"))
                .count();

        long priv = methods.stream()
                .filter(m -> m.getModifier().toString().contains("PRIVATE"))
                .count();

        HBox bar = new HBox(12);
        bar.setPadding(new Insets(6));
        bar.setStyle(
                "-fx-border-color: #ddd;" +
                        "-fx-border-radius: 6;" +
                        "-fx-background-radius: 6;"
        );

        bar.getChildren().addAll(
                badge("🧩 Methods", totalMethods),
                badge("🏗 Constructors", totalConstructors),
                badge("🌐 Public", pub),
                badge("🔒 Private", priv),
                badge("⚠ Empty", empty)
        );

        return bar;
    }

    private Label badge(String text, long value) {
        Label l = new Label(text + ": " + value);
        l.setStyle(
                "-fx-padding: 4 8 4 8;" +
                        "-fx-border-color: #ccc;" +
                        "-fx-border-radius: 4;"
        );
        return l;
    }
    //Constructor list
    private Node buildConstructorList(List<ConstructorInfo> constructors) {
        VBox list = new VBox();
        list.setPadding(new Insets(4));

        if (constructors.isEmpty()) {
            list.getChildren().add(new Label("No constructors found."));
            return new TitledPane("Constructors", list);
        }

        for (ConstructorInfo c : constructors) {
            list.getChildren().add(buildConstructorRow(c));
            list.getChildren().add(new Separator());
        }

        TitledPane pane = new TitledPane("Constructors", list);
        pane.setExpanded(true);
        return pane;
    }
    private Node buildConstructorRow(ConstructorInfo constructor) {
        HBox row = new HBox(8);
        row.setPadding(new Insets(6));
        row.setStyle(
                "-fx-border-color: #e0e0e0;" +
                        "-fx-border-radius: 4;"
        );

        Label signature = new Label(buildConstructorSignature(constructor));
        signature.setWrapText(true);
        HBox.setHgrow(signature, Priority.ALWAYS);

        row.getChildren().add(signature);

        row.setOnMouseClicked(event -> {
            uiFeatures.openAndHighlight(
                    constructor.getConstructorName(),
                    constructor.getStartLine(),
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



    // methods list
    private Node buildMethodList(List<MethodsInfo> methods){
        VBox list = new VBox();
        list.setPadding(new Insets(4));
        if (methods.isEmpty()) {
            list.getChildren().add(new Label("No methods found."));
            return list;
        }
        for (MethodsInfo method : methods) {
            list.getChildren().add(buildMethodRow(method));
            list.getChildren().add(new Separator());
        }

        TitledPane pane = new TitledPane("Methods", list);
        pane.setExpanded(true);
        return pane;
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
          uiFeatures.openAndHighlight(method.getMethodName(), method.getStartLine(), method.getSourceFile());
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
        header.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: #555;"
        );

        box.getChildren().add(header);

        if (items == null || items.isEmpty()) {
            box.getChildren().add(new Label("—"));
            return box;
        }

        for (String item : items) {
            Label l = new Label("• " + item);
            l.setStyle("-fx-padding: 0 0 0 6;");
            box.getChildren().add(l);
        }

        return box;
    }
    private Node dependencySection(String title,List<MethodCallInfo> items){
        VBox root = new VBox(4);
        Label header = new Label(title);
        header.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: #555;");
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
        titleLabel.setStyle(
                "-fx-font-weight: bold;" +
                        "-fx-text-fill: #666;"
        );
        group.getChildren().add(titleLabel);
        for (MethodCallInfo call : calls) {
            HBox row = new HBox(4);
            Label method = new Label(call.getMethodName() + "()");
            Label clazz=new Label("- "+call.getClassName());
            switch (call.getType()) {
                case INTERNAL -> method.setStyle("-fx-text-fill: #2e7d32;");
                case EXTERNAL -> method.setStyle("-fx-text-fill: #1565c0;");
                case LIBRARY  -> method.setStyle("-fx-text-fill: #a15c00;");
            }

            clazz.setStyle("-fx-text-fill: #888; -fx-font-size: 11;");
            row.getChildren().addAll(
                    new Label("•"),
                    method,
                    clazz
            );


            group.getChildren().add(row);

        }
        parent.getChildren().add(group);

    }




}