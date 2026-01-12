package com.example.bodhakfrontend;

import com.example.bodhakfrontend.Builder.ClassInfoBuilder;
import com.example.bodhakfrontend.Builder.ClassDependencyInfoBuilder;
import com.example.bodhakfrontend.Builder.ProjectAnalysisResultBuilder;
import com.example.bodhakfrontend.Models.ClassDependencyInfo;
import com.example.bodhakfrontend.Models.DependencyNode;
import com.example.bodhakfrontend.Models.PackageAnalysis.ProjectAnalysisResult;
import com.example.bodhakfrontend.Overview.ConstructorInfoBuilder;
import com.example.bodhakfrontend.Overview.MethodInfoBuilder;
import com.example.bodhakfrontend.Parser.Parsermanager;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.dependency.CircularDependency;

import com.example.bodhakfrontend.dependency.PackageDependency;
import com.example.bodhakfrontend.projectAnalysis.*;
import com.example.bodhakfrontend.util.BuildClassIndex;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import com.example.bodhakfrontend.util.ParseCache;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProjectContext {

    public final ParseCache cache;
    public final JavaFileParser javaFileParser;
    public final BuildClassIndex buildClassIndex;
    public final ClassDependencyInfoBuilder classDependencyInfoBuilder;
    public final FileAnalyzer fileAnalyzer;
    public final Parsermanager parsermanager;
    public final MethodInfoBuilder methodInfoBuilder;
    public final ConstructorInfoBuilder constructorInfoBuilder;
    public final ClassInfoBuilder classInfoBuilder;

    public final ProjectAnalysisResultBuilder analysisResultBuilder;

    public final List<Path> sourceRoots;
    public final Set<String> sourceClasses;
    public final Map<String, DependencyNode> classIndex;
    public final ClassDependencyInfo classDependencyInfo;
    public final ProjectAnalysisResult analysisResult;

    public ProjectContext(File projectFolder,
                          LanguageDetector detector,
                          MultiModuleSourceRootDetector rootDetector,
                          CircularDependency circularDependency,
                          HotspotAnalyzer hotspotAnalyzer,
                          UnusedClassAnalyzer unusedClassAnalyzer,
                          ProjectHealthAnalyzer projectHealthAnalyzer,
                          ClassHealthAnalyzer classHealthAnalyzer) {

        this.cache = new ParseCache(projectFolder.toPath());
        this.classInfoBuilder = new ClassInfoBuilder(cache);
        this.javaFileParser = new JavaFileParser(cache);

        constructorInfoBuilder =
                new ConstructorInfoBuilder(javaFileParser, cache);
        methodInfoBuilder =
                new MethodInfoBuilder(javaFileParser, rootDetector,cache);
        this.parsermanager = new Parsermanager(detector, javaFileParser);
        this.classDependencyInfoBuilder = new ClassDependencyInfoBuilder(
                rootDetector,
                javaFileParser,
                circularDependency,
                constructorInfoBuilder,
                methodInfoBuilder
        );

        this.fileAnalyzer =
                new FileAnalyzer(detector, javaFileParser, cache);

        this.analysisResultBuilder =
                new ProjectAnalysisResultBuilder(
                        fileAnalyzer,
                        classHealthAnalyzer,
                        new PackageDependency(circularDependency),
                        classInfoBuilder,
                        projectHealthAnalyzer,
                        hotspotAnalyzer,
                        unusedClassAnalyzer
                );

        this.sourceRoots = rootDetector.detectSourceRoots(projectFolder.toPath());
        this.sourceClasses = javaFileParser.getClassesfromSource(sourceRoots);

        this.buildClassIndex = new BuildClassIndex(javaFileParser, cache);
        this.classIndex = buildClassIndex.build(projectFolder.toPath());

        this.classDependencyInfo =
                classDependencyInfoBuilder.buildFull(projectFolder, classIndex);

        this.analysisResult =
                analysisResultBuilder.build(projectFolder.toPath(), classDependencyInfo);

    }
}

