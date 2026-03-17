package com.example.bodhakfrontend;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder.*;
import com.example.bodhakfrontend.Backend.IncrementalPart.UpdateManager;
import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Project.ProjectInfo;
import com.example.bodhakfrontend.Backend.models.incrementalModel.ClassInfoViewModel;
import com.example.bodhakfrontend.Parser.Parsermanager;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.util.ClassNameResolver;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder.javaParseCache;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;
public class ProjectContext {
    public final javaParseCache cache;
    public final JavaFileParser javaFileParser;
    public final Parsermanager parsermanager;
    public final ClassNameResolver  classNameResolver;
    public final javaClassInfoBuilder javaClassInfoBuilder;
    public final ClassDependecygraphBuilder classDependecygraphBuilder;
    public final PackageInfoBuilder  packageInfoBuilder;
    public final ProjectInfoBuilder  projectInfoBuilder;
    public final UpdateManager updateManager;
    public final List<Path> sourceRoots;
    public final Set<String> sourceClasses;
    public final MultiModuleSourceRootDetector multiModuleSourceRootDetector;
    public final ClassInfoViewModelBuilder classInfoViewModelBuilder;
    public final ProjectInfo projectInfo;
    public final Map<String, ClassInfoViewModel> vmMap;
    public ProjectContext(File projectFolder,
                          LanguageDetector detector) {
      this. multiModuleSourceRootDetector = new MultiModuleSourceRootDetector();
        this.sourceRoots = multiModuleSourceRootDetector.detectSourceRoots(projectFolder.toPath());
        this.cache = new javaParseCache(sourceRoots);
        this.javaFileParser = new JavaFileParser(cache);
        this.classNameResolver = new ClassNameResolver();
        this.classDependecygraphBuilder=new ClassDependecygraphBuilder(cache);
        this.javaClassInfoBuilder =new javaClassInfoBuilder(cache,classDependecygraphBuilder);
        this.packageInfoBuilder=new PackageInfoBuilder(javaClassInfoBuilder);
        this.projectInfoBuilder=new ProjectInfoBuilder(javaClassInfoBuilder,packageInfoBuilder);
        this.parsermanager = new Parsermanager(detector, javaFileParser);

        this.sourceClasses = javaFileParser.getClassesfromSource(sourceRoots);
        this.classInfoViewModelBuilder=new ClassInfoViewModelBuilder(classDependecygraphBuilder);
        this.updateManager=new UpdateManager(javaClassInfoBuilder,packageInfoBuilder,projectInfoBuilder,classDependecygraphBuilder,projectFolder.toPath(),classInfoViewModelBuilder);

        projectInfoBuilder.buildAll(projectFolder.toPath());
        this.projectInfo=projectInfoBuilder.getProjectInfo();
        List<ClassInfo> c=projectInfo.getClassInfos();
        this.vmMap=classInfoViewModelBuilder.initialBuild(projectInfo.getClassInfos());



    }
}

