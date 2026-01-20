package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.util.Duration;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class ClassDependencyView {

    private final ProjectInfo projectInfo;
    private final UiFeatures uiFeatures;

    public ClassDependencyView(UiFeatures uiFeatures, ProjectInfo projectInfo) {
        this.uiFeatures = uiFeatures;
        this.projectInfo = projectInfo;
    }

    public TreeView<Object> build(String className) {

        TreeItem<Object> root = new TreeItem<>("ROOT");
        root.setExpanded(true);

        ClassInfo cls = projectInfo.getClassInfoMap().get(className);
        if (cls == null) {
            root.getChildren().add(
                    new TreeItem<>("Class not found: " + className)
            );
            return new TreeView<>(root);
        }

        TreeItem<Object> classNode = new TreeItem<>(cls);
        classNode.setExpanded(true);

        // ---------- Depends On ----------
        TreeItem<Object> dependsOnNode =
                new TreeItem<>("Depends On");

        Set<String> visited = new HashSet<>();
        for (String dep : cls.getDependsOn()) {
            ClassInfo depCls = projectInfo.getClassInfoMap().get(dep);
            if (depCls != null) {
                TreeItem<Object> depItem = new TreeItem<>(depCls);
                dependsOnNode.getChildren().add(depItem);
                buildTransitive(depItem, depCls, visited);
            }
        }

        if (dependsOnNode.getChildren().isEmpty()) {
            dependsOnNode.getChildren().add(
                    new TreeItem<>("— None")
            );
        }

        // ---------- Used By ----------
        TreeItem<Object> usedByNode =
                new TreeItem<>("Used By");

        for (String user : cls.getUsedBy()) {
            ClassInfo userCls = projectInfo.getClassInfoMap().get(user);
            if (userCls != null) {
                usedByNode.getChildren().add(
                        new TreeItem<>(userCls)
                );
            }
        }

        if (usedByNode.getChildren().isEmpty()) {
            usedByNode.getChildren().add(
                    new TreeItem<>("— None")
            );
        }

        classNode.getChildren().addAll(dependsOnNode, usedByNode);
        root.getChildren().add(classNode);

        TreeView<Object> treeView = new TreeView<>(root);
        treeView.setShowRoot(false);

        configureCells(treeView);
        configureClicks(treeView);

        return treeView;
    }

    // ---------------- helpers ----------------

    private void buildTransitive(
            TreeItem<Object> parent,
            ClassInfo cls,
            Set<String> visited
    ) {
        if (!visited.add(cls.getClassName())) return;

        for (String dep : cls.getDependsOn()) {
            ClassInfo depCls = projectInfo.getClassInfoMap().get(dep);
            if (depCls != null) {
                TreeItem<Object> child = new TreeItem<>(depCls);
                parent.getChildren().add(child);
                buildTransitive(child, depCls, visited);
            }
        }
    }

    private void configureClicks(TreeView<Object> treeView) {
        treeView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                Object value =
                        treeView.getSelectionModel()
                                .getSelectedItem()
                                .getValue();
                if(value == null) return;
                if (value instanceof ClassInfo ci) {
                    uiFeatures.openAndHighlight(
                            ci.getClassName(),
                            ci.getBeginLine(),
                            ci.getBeginColumn(),
                            ci.getSourceFile()
                    );
                }
            }
        });
    }

    private void configureCells(TreeView<Object> treeView) {

        treeView.setCellFactory(tv -> new TreeCell<>() {

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                getStyleClass().remove("health-tree-risky");
                setTooltip(null);

                if (empty || item == null) {
                    setText(null);
                    return;
                }

                if (item instanceof String s) {
                    setText(s);
                    return;
                }

                if (item instanceof ClassInfo ci) {
                    String label = ci.getClassName();

                    if (!ci.getCircularDependencyGroups().isEmpty()) {
                        label = "🔁 " + label;
                        getStyleClass().add("health-tree-risky");

                        Tooltip tooltip =
                                new Tooltip(buildCycleTooltip(ci));
                        tooltip.setShowDelay(Duration.millis(400));
                        setTooltip(tooltip);
                    }

                    setText(label);
                }
            }
        });
    }

    private String buildCycleTooltip(ClassInfo ci) {
        StringBuilder sb = new StringBuilder("🔁 Circular Dependency\n\n");

        for (Set<String> cycle : ci.getCircularDependencyGroups()) {
            Iterator<String> it = cycle.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) sb.append(" → ");
            }
            sb.append("\n\n");
        }
        return sb.toString();
    }
}
