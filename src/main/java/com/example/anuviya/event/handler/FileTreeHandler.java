package com.example.anuviya.event.handler;

import com.example.anuviya.event.UiUpdateEvent;
import com.example.anuviya.event.domain.FileTreeChangedEvent;
import com.example.anuviya.event.store.UIStore;
import com.example.anuviya.ui.Front.FileTreeNodeFactory;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles {@link FileTreeChangedEvent} — refreshes the affected folder node
 * in the project file tree, preserving the user's expansion state.
 *
 * <p>All methods run on the JavaFX Application Thread (guaranteed by the dispatcher).
 */
public final class FileTreeHandler implements UiUpdateHandler {

    private final TreeView<File> treeView;
    private final FileTreeNodeFactory factory;
    private final UIStore store;

    public FileTreeHandler(TreeView<File> treeView, FileTreeNodeFactory factory, UIStore store) {
        this.treeView = treeView;
        this.factory  = factory;
        this.store    = store;
    }

    @Override
    public boolean canHandle(UiUpdateEvent event) {
        return event instanceof FileTreeChangedEvent;
    }

    @Override
    public void apply(UiUpdateEvent event) {
        FileTreeChangedEvent e = (FileTreeChangedEvent) event;
        refreshFolder(e.affectedDirectory());
        store.clearDirtyPath(e.affectedDirectory());
    }

    // ── File tree refresh logic ───────────────────────────────────────────────

    private void refreshFolder(Path folderPath) {
        TreeItem<File> oldNode = factory.get(folderPath);
        if (oldNode == null) return;

        TreeItem<File> parent = oldNode.getParent();
        Map<Path, Boolean> expansionState = captureExpansion(oldNode);
        TreeItem<File> newNode = factory.recreate(folderPath.toFile());

        if (parent == null) {
            treeView.setRoot(newNode);
        } else {
            int index = parent.getChildren().indexOf(oldNode);
            if (index >= 0) parent.getChildren().set(index, newNode);
        }
        restoreExpansion(newNode, expansionState);
    }

    private Map<Path, Boolean> captureExpansion(TreeItem<File> root) {
        Map<Path, Boolean> map = new HashMap<>();
        captureRecursive(root, map);
        return map;
    }

    private void captureRecursive(TreeItem<File> node, Map<Path, Boolean> map) {
        if (node.getValue() != null) {
            map.put(node.getValue().toPath(), node.isExpanded());
        }
        for (TreeItem<File> child : node.getChildren()) {
            captureRecursive(child, map);
        }
    }

    private void restoreExpansion(TreeItem<File> node, Map<Path, Boolean> state) {
        if (node.getValue() == null) return;
        Boolean expanded = state.get(node.getValue().toPath());
        if (expanded != null && expanded) {
            node.setExpanded(true);
            for (TreeItem<File> child : node.getChildren()) {
                restoreExpansion(child, state);
            }
        }
    }
}
