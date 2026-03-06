package com.example.bodhakfrontend.ui.overviewButton;

import com.example.bodhakfrontend.IncrementalPart.model.incrementalModel.ClassInfoViewModel;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import javafx.application.Platform;
import javafx.collections.SetChangeListener;
import javafx.scene.control.*;
import javafx.util.Duration;

import java.util.*;

public class ClassDependencyView {

    private final Map<String, ClassInfoViewModel> vmMap;
    private final UiFeatures uiFeatures;

    private final TreeView<Object> treeView = new TreeView<>();
    private ClassInfoViewModel currentVm;
    private ClassInfoViewModel vm;

    public ClassDependencyView(
            UiFeatures uiFeatures,
            Map<String, ClassInfoViewModel> vmMap
    ) {
        this.uiFeatures = uiFeatures;
        this.vmMap = vmMap;

        treeView.setShowRoot(false);
        configureCells();
        configureClicks();
    }



    public TreeView<Object> show(String className) {

        ClassInfoViewModel vm = vmMap.get(className);
        if (vm == null) {
            treeView.setRoot(
                    new TreeItem<>("Class not found: " + className)
            );
            return treeView;
        }

        bindTo(vm);
        rebuild();


        return treeView;
    }


    private void bindTo(ClassInfoViewModel vm) {
        if (currentVm != null) {
            currentVm.getDependsOn().removeListener(depListener);
            currentVm.getUsedBy().removeListener(depListener);
        }
        currentVm = vm;
        // bind new
        vm.getDependsOn().addListener(depListener);
        vm.getUsedBy().addListener(depListener);
    }

    private final SetChangeListener<String> depListener = change -> {
        // rebuild must happen on FX thread
        Platform.runLater(this::rebuild);
    };

    private void rebuild() {
        if (currentVm == null) return;
        TreeItem<Object> root = new TreeItem<>("ROOT");
        root.setExpanded(true);
        TreeItem<Object> classNode = new TreeItem<>(currentVm);
        classNode.setExpanded(true);
        // depends on
        TreeItem<Object> dependsOnNode = new TreeItem<>("Depends On");
        Set<String> visited = new HashSet<>();
        List<String> depsSnapshot =
                new ArrayList<>(currentVm.getDependsOn());

        for (String dep : depsSnapshot) {
            ClassInfoViewModel depVm = vmMap.get(dep);
            if (depVm != null) {
                TreeItem<Object> depItem = new TreeItem<>(depVm);
                dependsOnNode.getChildren().add(depItem);
                buildTransitive(depItem, depVm, visited);
            }
        }

        if (dependsOnNode.getChildren().isEmpty()) {
            dependsOnNode.getChildren().add(
                    new TreeItem<>("— None")
            );
        }

        // used by
        TreeItem<Object> usedByNode = new TreeItem<>("Used By");
        List<String> usedBySnapshot =
                new ArrayList<>(currentVm.getUsedBy());
        for (String user : usedBySnapshot) {
            ClassInfoViewModel userVm = vmMap.get(user);
            if (userVm != null) {
                usedByNode.getChildren().add(
                        new TreeItem<>(userVm)
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
        treeView.setRoot(root);
    }

    private void buildTransitive(
            TreeItem<Object> parent,
            ClassInfoViewModel vm,
            Set<String> visited
    ) {
        if (!visited.add(vm.getName())) return;
        for (String dep : vm.getDependsOn()) {
            ClassInfoViewModel depVm = vmMap.get(dep);
            if (depVm != null) {
                TreeItem<Object> child = new TreeItem<>(depVm);
                parent.getChildren().add(child);
                buildTransitive(child, depVm, visited);
            }
        }
    }

    private void configureCells() {

        treeView.setCellFactory(tv -> new TreeCell<>() {

            private ClassInfoViewModel boundVm;

            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);

                textProperty().unbind();
                setText(null);
                setTooltip(null);
                getStyleClass().remove("health-tree-risky");

                if (empty || item == null) return;

                if (item instanceof String s) {
                    setText(s);
                    return;
                }
                if (item instanceof ClassInfoViewModel vm) {
                    boundVm = vm;
                    textProperty().bind(vm.simpleNameProperty());

                    if (!vm.getCircularDependencyGroups().isEmpty()) {
                        getStyleClass().add("health-tree-risky");
                        Tooltip tip =
                                new Tooltip(buildCycleTooltip(vm));
                        tip.setShowDelay(Duration.millis(400));
                        setTooltip(tip);
                    }
                }
            }
        });
    }


    private void configureClicks() {
        treeView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                TreeItem<Object> item =
                        treeView.getSelectionModel().getSelectedItem();
                if (item == null) return;

                Object value = item.getValue();
                if (value instanceof ClassInfoViewModel vm) {
                    uiFeatures.openAndHighlight(
                            vm.getName(),
                            vm.getBeginLine(),
                            vm.getBeginColumn(),
                            vm.getSourceFile()
                    );
                }
            }
        });
    }



    private String buildCycleTooltip(ClassInfoViewModel vm) {
        StringBuilder sb =
                new StringBuilder("🔁 Circular Dependency\n\n");

        for (Set<String> cycle : vm.getCircularDependencyGroups()) {
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
