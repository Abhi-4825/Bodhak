package com.example.bodhakfrontend.ui;

import com.example.bodhakfrontend.Backend.Analysis.Engine.AnalysisEngine;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder.ProjectInfoBuilder;
import com.example.bodhakfrontend.Backend.IncrementalPart.Update.UiRefreshEvent;

import com.example.bodhakfrontend.ui.Front.FileTreeNodeFactory;
import com.example.bodhakfrontend.ui.ProjectAnalysis.ProjectAnalysisUi;
import com.example.bodhakfrontend.ui.rightPanel.RightPanelTabManager;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.fxmisc.richtext.CodeArea;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class UiRerfeshController {

    private final TreeView<File> treeView;
    private final FileTreeNodeFactory factory;
    private final TabPane tabPane;

    private  final RightPanelTabManager  rightPanelTabManager;
    private  final AnalysisEngine analysisEngine;
    private final ProjectAnalysisUi projectAnalysisUi;

    public UiRerfeshController(TreeView<File> treeView, FileTreeNodeFactory factory, TabPane tabPane, RightPanelTabManager rightPanelTabManager, ProjectAnalysisUi projectAnalysisUi, AnalysisEngine analysisEngine) {
        this.treeView = treeView;
        this.factory = factory;
        this.tabPane = tabPane;
        this.rightPanelTabManager = rightPanelTabManager;
        this.projectAnalysisUi = projectAnalysisUi;
        this.analysisEngine = analysisEngine;

    }


    public void onUiRefresh(UiRefreshEvent event) {
        Path path = event.path();
        if (path == null) {return;}

        Platform.runLater(() -> {

            switch (event.type()) {
                case FILE_TREE -> refreshFolder(path);
                case CLOSE_EDITOR -> closeEditorTab(path);
                case REFRESH_EDITOR -> refreshEditorTab(path);
                case PROJECTINFO_CHANGED -> refreshAnalysis();
                case OVERVIEWTAB_CLOSED -> closeOverviewTab(path);

            }
        });
    }
  //reload or refresh the code tab when a file is edited
    private void refreshEditorTab(Path path) {
        if(path==null) return;
        for(Tab tab:tabPane.getTabs()){
            Object data=tab.getUserData();
            if(!(data instanceof File file)) continue;
            if(!file.toPath().equals(path)) continue;
            Node content =tab.getContent();
            if(content instanceof CodeArea codeArea) {
                reloadCodeArea(codeArea, file);
            }
            break;
        }
    }

    private void reloadCodeArea(CodeArea codeArea, File file) {
        try {
            String newContent = Files.readString(file.toPath());

            int oldCaret = codeArea.getCaretPosition();
            codeArea.replaceText(newContent);

            Platform.runLater(() -> {
                int safeCaret=Math.min(oldCaret,codeArea.getLength());
            });


        } catch (Exception e) {
            codeArea.replaceText("// Error reloading file\n" + e.getMessage());
        }
    }


    // for closing the tab related to deleted file
    private void closeEditorTab(Path path) {
        if (path == null) {return;}
        tabPane.getTabs().removeIf(tab->
        {
            Object userdata=tab.getUserData();
            if (userdata instanceof File file) {
                return file.toPath().equals(path);
            }
            return false;
        });
    }

    // for closing the overviewtab related to deleted file
    private void closeOverviewTab(Path path) {
        rightPanelTabManager.closeOverviewTabs(path);

    }

    // for File tree update
    private void refreshFolder(Path folderPath) {

        TreeItem<File> oldNode = factory.get(folderPath);
        if (oldNode == null) return;

        TreeItem<File> parent = oldNode.getParent();

        // 🔹 Capture expansion BEFORE destruction
        Map<Path, Boolean> expansionState =
                captureExpansion(oldNode);

        TreeItem<File> newNode =
                factory.recreate(folderPath.toFile());

        if (parent == null) {
            // root case
            treeView.setRoot(newNode);
            restoreExpansion(newNode, expansionState);
            return;
        }

        int index = parent.getChildren().indexOf(oldNode);
        parent.getChildren().set(index, newNode);

        // 🔥 Restore expansion AFTER replace
        restoreExpansion(newNode, expansionState);
    }
    private Map<Path, Boolean> captureExpansion(TreeItem<File> root) {
        Map<Path, Boolean> map = new HashMap<>();
        captureRecursively(root, map);
        return map;
    }

    private void captureRecursively(
            TreeItem<File> node,
            Map<Path, Boolean> map
    ) {
        map.put(node.getValue().toPath(), node.isExpanded());
        for (TreeItem<File> child : node.getChildren()) {
            captureRecursively(child, map);
        }
    }

    private void restoreExpansion(
            TreeItem<File> node,
            Map<Path, Boolean> state
    ) {
        Boolean expanded = state.get(node.getValue().toPath());
        if (expanded != null && expanded) {
            node.setExpanded(true);
            // force children creation before restoring deeper levels
            for (TreeItem<File> child : node.getChildren()) {
                restoreExpansion(child, state);
            }
        }
    }


    private void refreshAnalysis() {
        rightPanelTabManager.refreshAnalyzeTabIfOpen(()->projectAnalysisUi.build(analysisEngine.getProjectInfo()));
    }

    private void refreshDependency() {
        // future extension
    }

    private void refreshCode() {
        // future extension
    }
}
