package com.example.bodhakfrontend.Backend.interfaces;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;

import java.nio.file.Path;
import java.util.List;

public interface ClassInfoBuilder {
    List<ClassInfo> getClassInfos(Path filePath);
    void onFileCreate(Path filePath);
    void onFileModify(Path filePath);
    void onFileDelete(Path filePath);
}
