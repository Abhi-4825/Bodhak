package com.example.bodhakfrontend.Backend.IncrementalPart;


import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder.*;
import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;

import java.nio.file.Path;
import java.util.List;
public class UpdateManager {
    private final javaClassInfoBuilder javaClassInfoBuilder;
    private final PackageInfoBuilder packageInfoBuilder;
    private final ProjectInfoBuilder projectInfoBuilder;
    private final ClassInfoViewModelBuilder classInfoViewModelBuilder;
    //for Path
    public UpdateManager(javaClassInfoBuilder classInfoToPathMapBuilder, PackageInfoBuilder packageInfoBuilder, ProjectInfoBuilder projectInfoBuilder, ClassDependecygraphBuilder classDependecygraphBuilder
            , Path projectPath, ClassInfoViewModelBuilder classInfoViewModelBuilder) {
        this.javaClassInfoBuilder = classInfoToPathMapBuilder;
        this.packageInfoBuilder = packageInfoBuilder;
        this.projectInfoBuilder = projectInfoBuilder;
        this.classInfoViewModelBuilder = classInfoViewModelBuilder;
    }
    // OnFile Create
    public void onFileCreate(Path path) {
        javaClassInfoBuilder.onFileCreate(path);
        List<ClassInfo> newClasses= javaClassInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        packageInfoBuilder.onFileCreate(newClasses);
        projectInfoBuilder.onFileCreated(path,newClasses);
        classInfoViewModelBuilder.onFileCreate(newClasses);
    }
    public void onFileDelete(Path path) {
        List<ClassInfo> removedClasses= javaClassInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        javaClassInfoBuilder.onFileDelete(path);
        packageInfoBuilder.onFileDelete(removedClasses);
        projectInfoBuilder.onFileDelete(path);
        classInfoViewModelBuilder.onFileDelete(removedClasses);
    }
    public void onFileUpdate(Path path) {
        List<ClassInfo> oldclasses= javaClassInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        javaClassInfoBuilder.onFileModify(path);
        List<ClassInfo> newClasses= javaClassInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
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





