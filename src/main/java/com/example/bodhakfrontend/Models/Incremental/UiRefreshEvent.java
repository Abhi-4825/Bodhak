package com.example.bodhakfrontend.Models.Incremental;

import java.nio.file.Path;

public record UiRefreshEvent(
          UiRefreshType type,
          Path path



){
    public enum UiRefreshType{
        FILE_TREE,
        CLOSE_EDITOR,
        REFRESH_EDITOR,
        REFRESH_ANALYSIS,
    }

}