package com.example.bodhakfrontend.projectAnalysis.ui;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassRole;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.Models.Severity;
import com.example.bodhakfrontend.projectAnalysis.warning.WarningBuilder;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import javafx.scene.Node;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.Region;

import java.util.List;

public class classHealthAnalyserViewBuilder {

    // get a new ClassInfo with null values
   private ClassInfo getClassInfo(String name){
       return new ClassInfo(name,null,null,null,null,null,null,
               null,null,null,null,false,false,
               false,-1,-1,-1,new ClassRole(false,false,false,false,false,false));
   }



    protected Node buildClassHealthTree(ProjectInfo projectInfo, UiFeatures uiFeatures) {
        List<ClassInfo> classInfos = projectInfo.getClassInfos();
        WarningBuilder warningBuilder = new WarningBuilder();
        TreeItem<ClassInfo> root = new TreeItem<>(getClassInfo("🏥 Class Health"));
        root.setExpanded(true);
        TreeItem<ClassInfo> risky = new TreeItem<>(getClassInfo("🔴 Risky Classes"));
        TreeItem<ClassInfo> warning= new TreeItem<>(getClassInfo("🟡 Warning Classes"));
        TreeItem<ClassInfo> healthy= new TreeItem<>(getClassInfo("🟢 Healthy Classes"));

        risky.setExpanded(true);
        warning.setExpanded(false);
        healthy.setExpanded(false);
        for(ClassInfo ci : classInfos){

            TreeItem<ClassInfo> classNode=new TreeItem<>(ci);
            classNode.getChildren().add(
                    new TreeItem<>(getClassInfo("LOC :"+ci.getLinesOfCode()))
            );
            classNode.getChildren().add(new TreeItem<>(getClassInfo("Depends On: "+ci.getDependsOn().size() + " Classes")));
            classNode.getChildren().add(new TreeItem<>(getClassInfo("Used By: "+ci.getUsedBy().size() + " Classes")));
            var warnings=warningBuilder.buildWarnings(ci);
            boolean hasHigh=warnings.stream().anyMatch(w->
                    w.getSeverity()== Severity.HIGH);
            boolean hasMedium=warnings.stream().anyMatch(w->
                    w.getSeverity()== Severity.MEDIUM);
            if (hasHigh) {
                risky.getChildren().add(classNode);
            } else if (hasMedium) {
                warning.getChildren().add(classNode);
            } else {
                healthy.getChildren().add(classNode);
            }


        }
        root.getChildren().addAll(risky,warning,healthy);
        TreeView<ClassInfo> treeView = new TreeView<>(root);
        treeView.setCellFactory(tv -> new TreeCell<ClassInfo>() {
            protected void updateItem(ClassInfo item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                setText(item.toString());

                if (item.toString().startsWith("🔴")) {
                    getStyleClass().add("health-tree-risky");
                } else if (item.toString().startsWith("🟡")) {
                    getStyleClass().add("health-tree-warning");
                } else if (item.toString().startsWith("🟢")) {
                    getStyleClass().add("health-tree-healthy");
                } else if (item.toString().startsWith("⚠")) {
                    getStyleClass().add("health-tree-warning-icon");
                }
            }
        });
        setTreeViewClicker(uiFeatures,treeView);

        treeView.setShowRoot(false);
        treeView.setPrefHeight(Region.USE_COMPUTED_SIZE);
        return treeView;

    }


    private void setTreeViewClicker(
            UiFeatures uiFeatures,
            TreeView<ClassInfo> treeView
    ) {
        treeView.setOnMouseClicked(event -> {

            // Only react on double click
            if (event.getClickCount() != 2) return;

            TreeItem<ClassInfo> selectedItem =
                    treeView.getSelectionModel().getSelectedItem();

            if (selectedItem == null) return;

            ClassInfo info = selectedItem.getValue();

            uiFeatures.openAndHighlight(info.getClassName(),info.getBeginLine(),info.getBeginColumn(),info.getSourceFile());
        });
    }










}
