package com.example.bodhakfrontend.IncrementalPart;

import com.example.bodhakfrontend.IncrementalPart.Builder.ClassDependecygraphBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ClassInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.PackageInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ProjectInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class UpdateManager {
    private final ClassInfoBuilder classInfoBuilder;
    private final PackageInfoBuilder packageInfoBuilder;
    private final ProjectInfoBuilder projectInfoBuilder;
    private final ClassDependecygraphBuilder classDependecygraphBuilder;
    //for Path
    private final Path projectPath;


    private ProjectInfo projectInfo;
    public UpdateManager(ClassInfoBuilder classInfoToPathMapBuilder, PackageInfoBuilder packageInfoBuilder, ProjectInfoBuilder projectInfoBuilder, ClassDependecygraphBuilder classDependecygraphBuilder
            , Path projectPath) {
        this.classInfoBuilder = classInfoToPathMapBuilder;
        this.packageInfoBuilder = packageInfoBuilder;
        this.projectInfoBuilder = projectInfoBuilder;
        this.classDependecygraphBuilder = classDependecygraphBuilder;
        this.projectPath = projectPath;

    }
    public void onLoad() {
        reBuildFull();
    }
    private void reBuildFull(){

        Set<String> sourceClasses =
                classInfoBuilder.collectSourceClasses(projectPath);

        classDependecygraphBuilder.buildDependsOnGraph(projectPath, sourceClasses);
        classInfoBuilder.buildAll(projectPath);
        projectInfo = projectInfoBuilder.getPackageInfo(projectPath);
        }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }
      public void onFileCreated(Path path){
      }
    }





