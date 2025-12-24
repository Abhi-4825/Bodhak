package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.Models.ClassDependencyInfo;
import com.example.bodhakfrontend.Models.DependencyNode;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Duration;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class ClassDependencyView {
    private final ClassDependencyInfo classDependencyInfo;
   private final UiFeatures uiFeatures;
    public ClassDependencyView(UiFeatures uiFeatures, ClassDependencyInfo classDependencyInfo) {
       this.uiFeatures=uiFeatures;
       this.classDependencyInfo=classDependencyInfo;
    }



    // showing Dependencies in TreeView
    public TreeView<DependencyNode> build(
            File userFile

    ) {
        TreeView<DependencyNode> treeView = new TreeView<>();
        TreeItem<DependencyNode> root =
                new TreeItem<>(new DependencyNode("ROOT", null, null,-1));
        root.setExpanded(true);

        Map<String, DependencyNode> classInfo =
                classDependencyInfo.getClassInfo();

        Map<String, Set<String>> depMap =
                classDependencyInfo.getClassDependencies();

        classInfo.values().stream()
                .filter(node ->
                        node.getSourceFile() != null &&
                                node.getSourceFile().getAbsolutePath()
                                        .equals(userFile.getAbsolutePath())
                )
                .forEach(node -> {

                    TreeItem<DependencyNode> classItem =
                            new TreeItem<>(node);
                    classItem.setExpanded(true);

                    // ---------- Depends On ----------
                    TreeItem<DependencyNode> dependsOn =
                            new TreeItem<>(new DependencyNode("Depends On", null, null,-1));

                    Set<String> deps =
                            depMap.getOrDefault(node.getClassName(), Set.of());

                    for (String dep : deps) {
                        DependencyNode depNode = classInfo.get(dep);
                        if (depNode != null) {
                            TreeItem<DependencyNode> child =
                                    new TreeItem<>(depNode);
                            dependsOn.getChildren().add(child);

                            buildTransitiveTree(
                                    child,
                                    depNode,
                                    classDependencyInfo,
                                    new HashSet<>()
                            );
                        }
                    }

                    // ---------- Used By ----------
                    TreeItem<DependencyNode> usedBy =
                            new TreeItem<>(new DependencyNode("Affects / Used By", null, null,-1));

                    showAffectedDependencies(
                            usedBy,
                            node,
                            classDependencyInfo
                    );

                    classItem.getChildren().addAll(dependsOn,usedBy);
                    root.getChildren().add(classItem);
                });

        treeView.setRoot(root);
        treeView.setShowRoot(false);
        setOnCell(treeView);
        onClick(treeView);
        return treeView;
    }

    private void buildTransitiveTree(
            TreeItem<DependencyNode> root,
            DependencyNode rootNode,
            ClassDependencyInfo classDependencyInfo,
            Set<String> visited
    ) {
        String className = rootNode.getClassName();

        //  Stop cycles
        if (!visited.add(className)) {
            return;
        }

        Set<String> deps =
                classDependencyInfo.getClassDependencies()
                        .get(className);

        if (deps == null) return;

        for (String dep : deps) {
            DependencyNode childNode =
                    classDependencyInfo.getClassInfo().get(dep);

            if (childNode != null) {
                TreeItem<DependencyNode> childItem =
                        new TreeItem<>(childNode);

                root.getChildren().add(childItem);

                buildTransitiveTree(
                        childItem,
                        childNode,
                        classDependencyInfo,
                        visited
                );
            }
        }
    }

    private void showAffectedDependencies(
            TreeItem<DependencyNode> root,
            DependencyNode rootNode,
            ClassDependencyInfo classDependencyInfo
    ) {
        Set<String> affected =
                classDependencyInfo.getReverseClassDependencies()
                        .get(rootNode.getClassName());

        if (affected == null || affected.isEmpty()) {
            root.getChildren().add(
                    new TreeItem<>(
                            new DependencyNode(
                                    "No classes are affected",
                                    null,
                                    null,
                                    -1
                            )
                    )
            );
            return;
        }

        for (String cls : affected) {
            DependencyNode node =
                    classDependencyInfo.getClassInfo().get(cls);

            if (node != null) {
                root.getChildren().add(new TreeItem<>(node));
            }
        }
    }


    private void onClick(TreeView<DependencyNode> dependencyTreeView){
        dependencyTreeView.setOnMouseClicked(e -> {

            if(e.getClickCount()==2 ){
                TreeItem<DependencyNode> treeItem=dependencyTreeView.getSelectionModel().getSelectedItem();
                if(treeItem==null){return;}
                DependencyNode selectedNode=(DependencyNode)treeItem.getValue();
                uiFeatures.openAndHighlight(selectedNode);
            }
        });
    }
    private void setOnCell(TreeView<DependencyNode> dependencyTreeView){
        dependencyTreeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(DependencyNode item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setTooltip(null);

                    return;
                }
                String classn=item.getClassName();
                String label=classn;
                Set<String> c= classDependencyInfo.getClassesInCycles();
                if(classDependencyInfo !=null && c.contains(label)){
                    label = "🔁 " + label + " [CIRCULAR]";
                }
                setText(label);
                classDependencyInfo.getCycleForClass(classn).ifPresent(cycle->{
                    Tooltip tooltip=new Tooltip();
                    tooltip.setText(buildCycleText(cycle));
                    tooltip.setShowDelay(Duration.millis(500));
                    setTooltip(tooltip);
                });
            }
        });
    }



    private String buildCycleText(Set<String> cycle) {

        StringBuilder sb = new StringBuilder();
        sb.append("⚠ Circular Dependency Detected\n\n");

        Iterator<String> it = cycle.iterator();
        while (it.hasNext()) {
            sb.append(it.next());
            if (it.hasNext()) sb.append(" → ");
        }

        // close the loop
        sb.append(" → ").append(cycle.iterator().next());

        return sb.toString();
    }

}
