package com.example.bodhakfrontend.Backend.IncrementalPart.Update;

import java.nio.file.Path;

public record FileChangeEvent(ChangeType type, Path path) {
    public enum ChangeType{
        FILE_CREATED,
        FILE_DELETED,
        FILE_MODIFIED,
        DIR_CREATED,
        DIR_DELETED
    }

}
