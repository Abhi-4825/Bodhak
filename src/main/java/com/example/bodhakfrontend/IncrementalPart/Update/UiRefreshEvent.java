package com.example.bodhakfrontend.IncrementalPart.Update;

import java.nio.file.Path;

public record UiRefreshEvent(
          UiRefreshType type,
          Path path
){
    public enum UiRefreshType{
        FILE_TREE,
        CLOSE_EDITOR,
        REFRESH_EDITOR,
        PROJECTINFO_CHANGED
    }

}