package com.example.bodhakfrontend.projectAnalysis.ui;

import com.example.bodhakfrontend.Models.ClassHealthInfo;
import com.example.bodhakfrontend.Models.DependencyNode;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Map;

public class classHealthAnalyserViewBuilder {
    protected Node buildClassHealthTree(Map<String, ClassHealthInfo> classHealthInfos,UiFeatures uiFeatures) {
        TreeItem<ClassHealthInfo> root = new TreeItem<>(new ClassHealthInfo("🏥 Class Health"));
        root.setExpanded(true);
        TreeItem<ClassHealthInfo> risky = new TreeItem<>(new ClassHealthInfo("🔴 Risky Classes"));
        TreeItem<ClassHealthInfo> warning= new TreeItem<>(new ClassHealthInfo("🟡 Warning Classes"));
        TreeItem<ClassHealthInfo> healthy= new TreeItem<>(new ClassHealthInfo("🟢 Healthy Classes"));

        risky.setExpanded(true);
        warning.setExpanded(false);
        healthy.setExpanded(false);
        for(ClassHealthInfo ci : classHealthInfos.values()){

            TreeItem<ClassHealthInfo> classNode=new TreeItem<>(ci);
            classNode.getChildren().add(
                    new TreeItem<>(new ClassHealthInfo("LOC :"+ci.getLoc()))
            );
            classNode.getChildren().add(new TreeItem<>(new ClassHealthInfo("Depends On: "+ci.getOutgoingDependencies() + " Classes")));
            classNode.getChildren().add(new TreeItem<>(new ClassHealthInfo("Used By: "+ci.getIncomingDependencies() + " Classes")));
            for(String warningName : ci.getWarnings()){
                classNode.getChildren().add(new TreeItem<>(new ClassHealthInfo("⚠ "+warningName)));
            }

            // classification of classes
            if(ci.isInCircularDependency()|| ci.isGodClass() ){
                risky.getChildren().add(classNode);
            }
            else if(!ci.getWarnings().isEmpty()){
                warning.getChildren().add(classNode);
            }
            else {
                healthy.getChildren().add(classNode);
            }


        }
        root.getChildren().addAll(risky,warning,healthy);
        TreeView<ClassHealthInfo> treeView = new TreeView<>(root);
        treeView.setCellFactory(tv -> new TreeCell<ClassHealthInfo>() {
            protected void updateItem(ClassHealthInfo item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item.toString());

                if (item.toString().startsWith("🔴")) {
                    setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                } else if (item.toString().startsWith("🟡")) {
                    setStyle("-fx-text-fill: orange;");
                } else if (item.toString().startsWith("🟢")) {
                    setStyle("-fx-text-fill: green;");
                } else if (item.toString().startsWith("⚠")) {
                    setStyle("-fx-text-fill: #c47f00;");
                } else {
                    setStyle("");
                }
            }
        });
        setTreeViewClicker(uiFeatures,treeView);

        treeView.setShowRoot(false);
        treeView.setPrefHeight(400);
        return treeView;

    }


    private void setTreeViewClicker(
            UiFeatures uiFeatures,
            TreeView<ClassHealthInfo> treeView
    ) {
        treeView.setOnMouseClicked(event -> {

            // Only react on double click
            if (event.getClickCount() != 2) return;

            TreeItem<ClassHealthInfo> selectedItem =
                    treeView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) return;

            ClassHealthInfo info = selectedItem.getValue();

            // 🔒 Guard: only real class nodes have DependencyNode
            DependencyNode dependencyNode = info.getDependencyNode();
            if (dependencyNode == null) return;

            uiFeatures.openAndHighlight(dependencyNode.getClassName(),dependencyNode.getBeginLine(),dependencyNode.getSourceFile());
        });
    }










}
