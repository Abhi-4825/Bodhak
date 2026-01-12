package com.example.bodhakfrontend.Builder;

import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.dependency.CircularDependency;
import com.example.bodhakfrontend.Overview.MethodInfoBuilder;
import com.example.bodhakfrontend.Overview.ConstructorInfoBuilder;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class ClassDependencyInfoBuilder {

    private final MultiModuleSourceRootDetector detector;
    private final JavaFileParser javaFileParser;
    private final CircularDependency circularDependency;
    private final MethodInfoBuilder methodInfoBuilder;
    private final ConstructorInfoBuilder constructorInfoBuilder;

    // Global State collector








    //  FILE-LEVEL INDICES
    private final Map<Path, Map<String, Set<String>>> fileDepIndex =
            new ConcurrentHashMap<>();

    private final Map<Path, Set<String>> fileClassIndex =
            new ConcurrentHashMap<>();

    public ClassDependencyInfoBuilder(
            MultiModuleSourceRootDetector detector,
            JavaFileParser javaFileParser,
            CircularDependency circularDependency,
            ConstructorInfoBuilder constructorInfoBuilder,
            MethodInfoBuilder methodInfoBuilder
    ) {
        this.detector = detector;
        this.javaFileParser = javaFileParser;
        this.circularDependency = circularDependency;
        this.methodInfoBuilder = methodInfoBuilder;
        this.constructorInfoBuilder = constructorInfoBuilder;
    }

 //full build

    public ClassDependencyInfo buildFull(
            File projectRoot,
            Map<String, DependencyNode> classInfo
    ) {
        fileDepIndex.clear();
        fileClassIndex.clear();

        List<Path> sourceRoots =
                detector.detectSourceRoots(projectRoot.toPath());

        Set<String> sourceClasses =
                javaFileParser.getClassesfromSource(sourceRoots);

        for (Path root : sourceRoots) {
            scanSourceRoot(root, sourceClasses);
        }

        return assemble(classInfo, projectRoot.toPath());
    }

   // file incremental
    public void onFileCreated(Path file, Set<String> sourceClasses) {
        indexFile(file, sourceClasses);
    }

    public void onFileModified(Path file, Set<String> sourceClasses) {
        indexFile(file, sourceClasses);
    }

    public void onFileDeleted(Path file) {
        fileDepIndex.remove(file);
        fileClassIndex.remove(file);
    }

    //internal

    private void scanSourceRoot(Path root, Set<String> sourceClasses) {
        try {
            Files.walk(root)
                    .filter(this::isJava)
                    .forEach(p -> indexFile(p, sourceClasses));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void indexFile(Path path, Set<String> sourceClasses) {
        try {
            Map<String, Set<String>> deps =
                    javaFileParser.buildClassDependencyGraph(path, sourceClasses);

            fileDepIndex.put(path, deps);
            fileClassIndex.put(path, deps.keySet());

        } catch (Exception e) {
            System.err.println("Dependency parse failed: " + path);
            e.printStackTrace();
        }
    }

    private boolean isJava(Path p) {
        return p.toString().endsWith(".java");
    }



    private ClassDependencyInfo assemble(
            Map<String, DependencyNode> classInfo,
            Path projectRoot
    ) {
        Map<String, Set<String>> depMap = new HashMap<>();

        //  Merge file-level dep maps
        for (Map<String, Set<String>> map : fileDepIndex.values()) {
            map.forEach((k, v) ->
                    depMap
                            .computeIfAbsent(k, kk -> new HashSet<>())
                            .addAll(v)
            );
        }

        //  Reverse dependency map
        Map<String, Set<String>> revDepMap = new HashMap<>();
        for (Map.Entry<String, Set<String>> e : depMap.entrySet()) {
            String from = e.getKey();
            for (String to : e.getValue()) {
                revDepMap
                        .computeIfAbsent(to, k -> new HashSet<>())
                        .add(from);
            }
        }

        //  Circular dependencies
        Set<Set<String>> circularGroups =
                circularDependency.findCircularDependency(depMap);

        //  Methods + constructors
        Map<String, List<MethodsInfo>> methods =
                methodInfoBuilder.build(projectRoot);

        Map<String, List<ConstructorInfo>> constructors =
                constructorInfoBuilder.build(projectRoot);

        return new ClassDependencyInfo(
                constructors,
                classInfo,
                depMap,
                revDepMap,
                circularGroups,
                methods
        );
    }



    // incremental on file create








}
