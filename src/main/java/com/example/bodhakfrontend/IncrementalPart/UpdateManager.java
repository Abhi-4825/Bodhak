package com.example.bodhakfrontend.IncrementalPart;

import com.example.bodhakfrontend.IncrementalPart.Builder.ClassDependecygraphBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ClassInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.PackageInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ProjectInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;

import java.nio.file.Path;
import java.util.List;
public class UpdateManager {
    private final ClassInfoBuilder classInfoBuilder;
    private final PackageInfoBuilder packageInfoBuilder;
    private final ProjectInfoBuilder projectInfoBuilder;
    //for Path
    public UpdateManager(ClassInfoBuilder classInfoToPathMapBuilder, PackageInfoBuilder packageInfoBuilder, ProjectInfoBuilder projectInfoBuilder, ClassDependecygraphBuilder classDependecygraphBuilder
            , Path projectPath) {
        this.classInfoBuilder = classInfoToPathMapBuilder;
        this.packageInfoBuilder = packageInfoBuilder;
        this.projectInfoBuilder = projectInfoBuilder;
    }
    // OnFile Create
    public void onFileCreate(Path path) {
        classInfoBuilder.onFileCreate(path);
        List<ClassInfo> newClasses=classInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        packageInfoBuilder.onFileCreate(newClasses);
        projectInfoBuilder.onFileCreated(path,newClasses);
    }
    public void onFileDelete(Path path) {
        List<ClassInfo> removedClasses=classInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        classInfoBuilder.onFileDelete(path);
        packageInfoBuilder.onFileDelete(removedClasses);
        projectInfoBuilder.onFileDelete(path);
    }
    public void onFileUpdate(Path path) {
        List<ClassInfo> oldclasses=classInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        classInfoBuilder.onFileModify(path);
        List<ClassInfo> newClasses=classInfoBuilder.getClassInfoMap().get(path.toAbsolutePath().normalize());
        packageInfoBuilder.onFileUpdate(oldclasses,newClasses);
        projectInfoBuilder.onFileUpdate(path,newClasses);
    }

    public void onFolderCreate(Path path) {
        projectInfoBuilder.onFolderCreated(path);
    }
    public void onFolderDelete(Path path) {
        projectInfoBuilder.onFolderDeleted(path);
    }


   }





