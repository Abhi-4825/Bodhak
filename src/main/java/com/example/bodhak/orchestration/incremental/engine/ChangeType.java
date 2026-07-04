package com.example.bodhak.orchestration.incremental.engine;

public enum ChangeType {
    CONTENT_CHANGE,
    NEW_FILE,
    DELETED_FILE,
    FILE_RENAME,
    FILE_MOVE,
    PACKAGE_CHANGE,
    BUILD_CONFIG_CHANGE,
    RESOURCE_CHANGE,
    GENERATED_FILE_CHANGE
}
