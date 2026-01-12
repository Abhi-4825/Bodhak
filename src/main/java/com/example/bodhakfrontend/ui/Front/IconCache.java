package com.example.bodhakfrontend.ui.Front;

import javafx.scene.image.Image;

public final class IconCache {

    public static final Image FOLDER_ICON =
            new Image(IconCache.class.getResourceAsStream("/icons/folder.png"));

    public static final Image FILE_ICON =
            new Image(IconCache.class.getResourceAsStream("/icons/file.png"));

    private IconCache() {}
}
