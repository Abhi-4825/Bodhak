package com.example.bodhakfrontend.Backend.IncrementalPart.Update;

import java.nio.file.Path;

public record UiRefreshEvent(
          UiRefreshType type,
          Path path
){
    public enum UiRefreshType{
        FILE_TREE,
        CLOSE_EDITOR,
        REFRESH_EDITOR,
        PROJECTINFO_CHANGED,
        OVERVIEWTAB_CLOSED
    }

}