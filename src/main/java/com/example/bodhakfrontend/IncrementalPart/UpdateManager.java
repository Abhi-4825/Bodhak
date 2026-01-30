package com.example.bodhakfrontend.IncrementalPart;


import com.example.bodhakfrontend.IncrementalPart.Builder.*;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;

import java.nio.file.Path;
import java.util.List;
public class UpdateManager {
    private final ClassInfoBuilder classInfoBuilder;
    private final PackageInfoBuilder packageInfoBuilder;
    private final ProjectInfoBuilder projectInfoBuilder;
    private final ClassInfoViewModelBuilder classInfoViewModelBuilder;
    //for Path
    public UpdateManager(ClassInfoBuilder classInfoToPathMapBuilder, PackageInfoBuilder packageInfoBuilder, ProjectInfoBuilder projectInfoBuilder, ClassDependecygraphBuilder classDependecygraphBuilder
            , Path projectPath, ClassInfoViewModelBuilder classInfoViewModelBuilder) {
        this.classInfoBuilder = classInfoToPathMapBuilder;
        this.packageInfoBuilder = packageInfoBuilder;
        this.projectInfoBuilder = projectInfoBuilder;
        this.classInfoViewModelBuilder = classInfoViewModelBuilder;
    }
    // OnFile Create
    public void onFileCreate(Path path) {
        classInfoBuilder.onFileCreate(path);
        List<ClassInfo> newClasses=classInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        packageInfoBuilder.onFileCreate(newClasses);
        projectInfoBuilder.onFileCreated(path,newClasses);
        classInfoViewModelBuilder.onFileCreate(newClasses);
    }
    public void onFileDelete(Path path) {
        List<ClassInfo> removedClasses=classInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        classInfoBuilder.onFileDelete(path);
        packageInfoBuilder.onFileDelete(removedClasses);
        projectInfoBuilder.onFileDelete(path);
        classInfoViewModelBuilder.onFileDelete(removedClasses);
    }
    public void onFileUpdate(Path path) {
        List<ClassInfo> oldclasses=classInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        classInfoBuilder.onFileModify(path);
        List<ClassInfo> newClasses=classInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        packageInfoBuilder.onFileUpdate(oldclasses,newClasses);
        projectInfoBuilder.onFileUpdate(path,newClasses);
        classInfoViewModelBuilder.onFileModify(oldclasses,newClasses);
    }

    public void onFolderCreate(Path path) {
        projectInfoBuilder.onFolderCreated(path);
    }
    public void onFolderDelete(Path path) {
        projectInfoBuilder.onFolderDeleted(path);
    }


   }





