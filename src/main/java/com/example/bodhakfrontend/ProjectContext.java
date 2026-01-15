package com.example.bodhakfrontend;

import com.example.bodhakfrontend.IncrementalPart.Builder.ClassDependecygraphBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ClassInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.PackageInfoBuilder;
import com.example.bodhakfrontend.IncrementalPart.Builder.ProjectInfoBuilder;
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
    public final List<Path> sourceRoots;
    public final Set<String> sourceClasses;
    public ProjectContext(File projectFolder,
                          LanguageDetector detector,
                          MultiModuleSourceRootDetector rootDetector) {

        this.cache = new ParseCache(projectFolder.toPath());
        this.javaFileParser = new JavaFileParser(cache);
        this.classNameResolver = new ClassNameResolver();
        this.classDependecygraphBuilder=new ClassDependecygraphBuilder(cache,classNameResolver);
        this.classInfoBuilder=new ClassInfoBuilder(cache,classDependecygraphBuilder);
        this.packageInfoBuilder=new PackageInfoBuilder(classInfoBuilder);
        this.projectInfoBuilder=new ProjectInfoBuilder(classInfoBuilder,packageInfoBuilder);



        this.parsermanager = new Parsermanager(detector, javaFileParser);






        this.sourceRoots = rootDetector.detectSourceRoots(projectFolder.toPath());
        this.sourceClasses = javaFileParser.getClassesfromSource(sourceRoots);



    }
}

