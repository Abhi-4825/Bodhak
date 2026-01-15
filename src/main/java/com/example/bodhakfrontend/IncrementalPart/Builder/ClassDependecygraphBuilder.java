package com.example.bodhakfrontend.IncrementalPart.Builder;

import com.example.bodhakfrontend.dependency.CircularDependency;
import com.example.bodhakfrontend.util.ClassNameResolver;
import com.example.bodhakfrontend.util.ParseCache;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassDependecygraphBuilder {
    private final ParseCache cache;
    private final ClassNameResolver  classNameResolver;
    private final Map<Path,Map<String,Set<String>>> classDependenciesToPath =new ConcurrentHashMap<>();
    private final Map<Path,Map<String,Set<String>>> reverseClassDependencies =new ConcurrentHashMap<>();
    Map<String,Set<String>> classDependencies=new ConcurrentHashMap<>();

    public ClassDependecygraphBuilder(ParseCache cache, ClassNameResolver classNameResolver) {
        this.cache = cache;
        this.classNameResolver = classNameResolver;
    }


    public Map<Path,Map<String, Set<String>>> buildDependsOnGraph (Path projectPath,Set<String> sourceClasses){
        try {
            Files.walk(projectPath).
                    filter(path -> path.toString().endsWith(".java")).forEach(path -> {

                        buildDependencyGraph(path, sourceClasses);


                    });

        }catch (Exception e){
            System.out.println("Error reading project path"+e.getMessage());;
        }


        return classDependenciesToPath;
    }

    // for finding dependencies for class in a file
    public void buildDependencyGraph(Path filePath, Set<String> sourceClasses) {

        Path normalizedPath = filePath.toAbsolutePath().normalize();
        CompilationUnit cu = cache.get(normalizedPath);

        cu.findAll(TypeDeclaration.class).forEach(clazz -> {

            String className = ClassNameResolver.resolveFqn(cu, clazz);
            Set<String> dependsOn = new HashSet<>();

            clazz.findAll(ClassOrInterfaceType.class).forEach(type -> {
                String dep = ClassNameResolver.resolveTypeFqn(cu, type);

                if (dep != null
                        && !dep.equals(className)
                        && sourceClasses.contains(dep)) {

                    dependsOn.add(dep);
                }
            });
            clazz.findAll(MethodCallExpr.class).forEach(call -> {
                call.getScope().ifPresent(scope -> {

                    String dep = ClassNameResolver.resolveScopeFqn(cu, scope);

                    if (dep != null
                            && !dep.equals(className)
                            && sourceClasses.contains(dep)) {

                        dependsOn.add(dep);
                    }
                });
            });

            classDependenciesToPath
                    .computeIfAbsent(normalizedPath, k -> new ConcurrentHashMap<>())
                    .put(className, dependsOn);
        });
    }




    public Map<Path, Map<String, Set<String>>> getAffectedClassDependencies(
            Map<Path, Map<String, Set<String>>> classDependenciesToPath) {

        reverseClassDependencies.clear();

        for (Map.Entry<Path, Map<String, Set<String>>> pathEntry
                : classDependenciesToPath.entrySet()) {

            Path fromPath = pathEntry.getKey();
            Map<String, Set<String>> classDeps = pathEntry.getValue();

            for (Map.Entry<String, Set<String>> classEntry : classDeps.entrySet()) {

                String fromClass = classEntry.getKey();
                Set<String> dependsOn = classEntry.getValue();

                for (String toClass : dependsOn) {

                    reverseClassDependencies
                            .computeIfAbsent(fromPath, k -> new ConcurrentHashMap<>())
                            .computeIfAbsent(toClass, k -> ConcurrentHashMap.newKeySet())
                            .add(fromClass);
                }
            }
        }

        return reverseClassDependencies;
    }

    public Path normalize(Path filePath) {
        return filePath.toAbsolutePath().normalize();
    }


    // update the classDependency on File create
    public void onFileCreate(Path filePath,Set<String> sourceClasses){
        buildDependencyGraph(normalize(filePath),sourceClasses);
        getAffectedClassDependencies(classDependenciesToPath);


    }

    // update classDependency on File modify
    public void onFileModify(Path filePath,Set<String> sourceClasses){
        Path normalizedPath = normalize(filePath);
        cache.invalidate(normalizedPath);
        buildDependencyGraph(normalizedPath,sourceClasses);
        getAffectedClassDependencies(classDependenciesToPath);

    }

    // update on file Delete
    public void onFileDelete(Path filePath){
        Path normalizedPath = normalize(filePath);
        cache.invalidate(normalizedPath);
        classDependenciesToPath.remove(normalizedPath);
        getAffectedClassDependencies(classDependenciesToPath);

    }


    // get the maps
    public Map<Path, Map<String, Set<String>>> getClassDependenciesToPath() {
        return classDependenciesToPath;
    }
    public Map<Path, Map<String, Set<String>>> getReverseClassDependencies() {
        return reverseClassDependencies;
    }

    // Circular - dependency group
    CircularDependency circularDependency=new CircularDependency();
    // first get the required Map

    private Set<Set<String>> updateClassDependencies(){
        classDependencies.clear();
        Map<Path, Map<String, Set<String>>> perFileDeps = getClassDependenciesToPath();

        for (Map<String, Set<String>> classMap : perFileDeps.values()) {
            for (Map.Entry<String, Set<String>> e : classMap.entrySet()) {
                classDependencies
                        .computeIfAbsent(e.getKey(), k -> new HashSet<>())
                        .addAll(e.getValue());
            }
        }
        return circularDependency.findCircularDependency(classDependencies);

    }

    // get Cirulardependencies graph

    public Set<Set<String>> getClassDependenciesGroups() {
        return updateClassDependencies();
    }






}
