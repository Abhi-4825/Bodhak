package com.example.bodhakfrontend.ui.Front;


import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TreeItem;
import javafx.scene.image.ImageView;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class FileTreeNodeFactory {

    private final Map<Path, TreeItem<File>> index = new HashMap<>();

    public TreeItem<File> createNode(File file) {

        TreeItem<File> item = new TreeItem<>(file);
        item.setGraphic(createIcon(file));
        index.put(file.toPath(), item);

        return new TreeItem<>(file) {

            private boolean loaded = false;

            {
                setGraphic(createIcon(file));
                index.put(file.toPath(), this);
            }

            @Override
            public ObservableList<TreeItem<File>> getChildren() {
                if (!loaded) {
                    loaded = true;
                    File[] files = file.listFiles();
                    if (files != null) {
                        super.getChildren().setAll(
                                FXCollections.observableArrayList(
                                        java.util.Arrays.stream(files)
                                                .map(FileTreeNodeFactory.this::createNode)
                                                .toList()
                                )
                        );
                    }
                }
                return super.getChildren();
            }

            @Override
            public boolean isLeaf() {
                return file.isFile();
            }
        };
    }

    private ImageView createIcon(File file) {
        ImageView iv = new ImageView(
                file.isDirectory()
                        ? IconCache.FOLDER_ICON
                        : IconCache.FILE_ICON
        );
        iv.setFitWidth(16);
        iv.setFitHeight(16);
        iv.setPreserveRatio(true);
        return iv;
    }

    public TreeItem<File> get(Path path) {
        return index.get(path);
    }

    public void remove(Path path) {
        index.remove(path);
    }
    public TreeItem<File> recreate(File file) {
        index.remove(file.toPath());
        return createNode(file);
    }

}


