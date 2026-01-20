package com.example.bodhakfrontend;
import com.example.bodhakfrontend.IncrementalPart.Builder.ClassDependecygraphBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ClassInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.PackageInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ProjectInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.UpdateManager;
import com.example.bodhakfrontend.Parser.Parsermanager;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.util.ClassNameResolver;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import com.example.bodhakfrontend.util.ParseCache;
import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
public class ProjectContext {
    public final ParseCache cache;
    public final JavaFileParser javaFileParser;
    public final Parsermanager parsermanager;
    public final ClassNameResolver  classNameResolver;
    public final ClassInfoBuilder classInfoBuilder;
    public final ClassDependecygraphBuilder classDependecygraphBuilder;
    public final PackageInfoBuilder  packageInfoBuilder;
    public final ProjectInfoBuilder  projectInfoBuilder;
    public final UpdateManager updateManager;
    public final List<Path> sourceRoots;
    public final Set<String> sourceClasses;
    public final MultiModuleSourceRootDetector multiModuleSourceRootDetector;
    public ProjectContext(File projectFolder,
                          LanguageDetector detector) {
      this. multiModuleSourceRootDetector = new MultiModuleSourceRootDetector();
        this.sourceRoots = multiModuleSourceRootDetector.detectSourceRoots(projectFolder.toPath());
        this.cache = new ParseCache(sourceRoots);
        this.javaFileParser = new JavaFileParser(cache);
        this.classNameResolver = new ClassNameResolver();
        this.classDependecygraphBuilder=new ClassDependecygraphBuilder(cache);
        this.classInfoBuilder=new ClassInfoBuilder(cache,classDependecygraphBuilder);
        this.packageInfoBuilder=new PackageInfoBuilder(classInfoBuilder);
        this.projectInfoBuilder=new ProjectInfoBuilder(classInfoBuilder,packageInfoBuilder);
        this.parsermanager = new Parsermanager(detector, javaFileParser);

        this.sourceClasses = javaFileParser.getClassesfromSource(sourceRoots);
        this.updateManager=new UpdateManager(classInfoBuilder,packageInfoBuilder,projectInfoBuilder,classDependecygraphBuilder,projectFolder.toPath());
    }
}

