package com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder;

import com.example.bodhakfrontend.Backend.dependency.CircularDependency;
import com.example.bodhakfrontend.util.ClassNameResolver;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.EnumDeclaration;
import com.github.javaparser.ast.body.RecordDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.resolution.types.ResolvedReferenceType;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassDependecygraphBuilder {
    private final javaParseCache cache;
    private final Map<String,Set<String>> reverseClassDependencies =new ConcurrentHashMap<>();
    private final Map<Path,Map<String,Set<String>>> classDependencies=new HashMap<>();
    Map<String,Set<String>> classDependenciesGroups =new ConcurrentHashMap<>();

    public ClassDependecygraphBuilder(javaParseCache cache) {
        this.cache = cache;

    }


    public Map<Path,Map<String, Set<String>>> buildDependsOnGraph (Path projectPath,Set<String> sourceClasses){
        classDependencies.clear();
        try {
            Files.walk(projectPath).
                    filter(path -> path.toString().endsWith(".java")).forEach(path -> {

                        buildDependencyGraph(path, sourceClasses);


                    });

        }catch (Exception e){
            System.out.println("Error reading project path"+e.getMessage());;
        }

        getAffectedClassDependencies(classDependencies);
        return classDependencies;

    }

    // for finding dependencies for class in a file
    public void buildDependencyGraph(Path filePath, Set<String> sourceClasses) {

        Path normalizedPath = filePath.toAbsolutePath().normalize();
        CompilationUnit cu = cache.get(normalizedPath);
        if (cu == null) return;

        // 1> first for classes or interfaces
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            Set<String> dependencies=new HashSet<>();

            String className = ClassNameResolver.resolveFqn(cu,clazz);
            // fields types--> private Parsecache cache;
            clazz.getFields().forEach(field -> {
                field.getVariables().forEach(variable -> {
                    resolveType(variable.getType(),dependencies,sourceClasses);
                });
            });

            // constructor parameter type
            clazz.getConstructors().forEach(constructor -> {
                constructor.getParameters().forEach(parameter -> {
                    resolveType(parameter.getType(),dependencies,sourceClasses);
                });
            });

            //method Parameter type and return type
            clazz.getMethods().forEach(method -> {
                method.getParameters().forEach(parameter -> {
                    resolveType(parameter.getType(),dependencies,sourceClasses);
                });
                resolveType(method.getType(),dependencies,sourceClasses);
            });
            // methods called
            clazz.findAll(MethodCallExpr.class).forEach(call -> {
                try {
                    String dep = call.resolve()
                            .declaringType()
                            .getQualifiedName();
                    if(sourceClasses.contains(dep)) dependencies.add(dep);
                } catch (Exception ignored) {

                }
            });

            // object creation
            clazz.findAll(ObjectCreationExpr.class).forEach(expr -> {
                try {
                    String dep = expr.getType()
                            .resolve()
                            .asReferenceType()
                            .getQualifiedName();
                    if(sourceClasses.contains(dep))dependencies.add(dep);
                } catch (Exception ignored) {
                }
            });
            //generics
            clazz.findAll(ClassOrInterfaceType.class).forEach(type ->
                    type.getTypeArguments().ifPresent(args ->
                            args.forEach(arg ->
                                    resolveType(arg, dependencies, sourceClasses)
                            )
                    )
            );

            // inheritance and extended
            clazz.getExtendedTypes().forEach(extendedType ->
                    resolveType(extendedType, dependencies, sourceClasses));
            clazz.getImplementedTypes().forEach(implementedType ->
                    resolveType(implementedType, dependencies, sourceClasses));


            // annotation
            clazz.getAnnotations().forEach(ann -> {
                try {
                    String dep = ann.resolve()
                            .getQualifiedName();
                    if(sourceClasses.contains(dep)) dependencies.add(dep);
                } catch (Exception ignored) {

                }

            });
            dependencies.remove(className);
            classDependencies
                    .computeIfAbsent(filePath, k -> new HashMap<>())
                    .put(className, dependencies);
        });
        cu.findAll(RecordDeclaration.class).forEach(record -> {

            String recordName = ClassNameResolver.resolveFqn(cu, record);
            Set<String> dependencies = new HashSet<>();

            /* 1 Record components */
            record.getParameters().forEach(param ->
                    resolveType(param.getType(), dependencies, sourceClasses)
            );

            /* 2 Implemented interfaces */
            record.getImplementedTypes().forEach(type ->
                    resolveType(type, dependencies, sourceClasses)
            );

            /* 3 Annotations */
            record.getAnnotations().forEach(ann -> {
                try {
                    String dep = ann.resolve().getQualifiedName();
                    if (sourceClasses.contains(dep)) {
                        dependencies.add(dep);
                    }
                } catch (Exception ignored) {}
            });

            /* 4 Remove self */
            dependencies.remove(recordName);

            classDependencies.computeIfAbsent(normalizedPath, k -> new HashMap<>()).put(recordName, dependencies);
        });

        // for Enums
        cu.findAll(EnumDeclaration.class).forEach(enumDecl -> {

            String enumName = ClassNameResolver.resolveFqn(cu, enumDecl);
            Set<String> dependencies = new HashSet<>();

            /* 1 Implemented interfaces */
            enumDecl.getImplementedTypes().forEach(type ->
                    resolveType(type, dependencies, sourceClasses)
            );

            /* 2 Fields */
            enumDecl.getFields().forEach(field ->
                    field.getVariables().forEach(var ->
                            resolveType(var.getType(), dependencies, sourceClasses)
                    )
            );

            /* 3 Constructors */
            enumDecl.getConstructors().forEach(cons ->
                    cons.getParameters().forEach(param ->
                            resolveType(param.getType(), dependencies, sourceClasses)
                    )
            );

            /* 4 Methods */
            enumDecl.getMethods().forEach(method -> {
                method.getParameters().forEach(param ->
                        resolveType(param.getType(), dependencies, sourceClasses)
                );
                resolveType(method.getType(), dependencies, sourceClasses);
            });

            /* 5 Method calls */
            enumDecl.findAll(MethodCallExpr.class).forEach(call -> {
                try {
                    String dep = call.resolve()
                            .declaringType()
                            .getQualifiedName();
                    if (sourceClasses.contains(dep)) {
                        dependencies.add(dep);
                    }
                } catch (Exception ignored) {}
            });

            /* 6️ Object creation */
            enumDecl.findAll(ObjectCreationExpr.class).forEach(expr -> {
                try {
                    String dep = expr.getType()
                            .resolve()
                            .asReferenceType()
                            .getQualifiedName();
                    if (sourceClasses.contains(dep)) {
                        dependencies.add(dep);
                    }
                } catch (Exception ignored) {}
            });

            /* 7️Annotations */
            enumDecl.getAnnotations().forEach(ann -> {
                try {
                    String dep = ann.resolve().getQualifiedName();
                    if (sourceClasses.contains(dep)) {
                        dependencies.add(dep);
                    }
                } catch (Exception ignored) {}
            });

            /* 8 Remove self */
            dependencies.remove(enumName);

            classDependencies.computeIfAbsent(normalizedPath, k -> new HashMap<>()).put(enumName, dependencies);
        });


    }


    public Map<String, Set<String>> getAffectedClassDependencies(
            Map<Path, Map<String, Set<String>>> classDependenciesToPath) {

        reverseClassDependencies.clear();

        classDependenciesToPath.forEach((path, classMap) -> {

            classMap.forEach((fromClass, dependsOnSet) -> {

                for (String toClass : dependsOnSet) {

                    reverseClassDependencies
                            .computeIfAbsent(toClass, k -> ConcurrentHashMap.newKeySet())
                            .add(fromClass);
                }
            });
        });

        return reverseClassDependencies;
    }





    public Path normalize(Path filePath) {
        return filePath.toAbsolutePath().normalize();
    }
    // update the classDependency on File create
    public void onFileCreate(Path filePath,Set<String> sourceClasses){
        buildDependencyGraph(normalize(filePath),sourceClasses);
        getAffectedClassDependencies(classDependencies);
    }

    // update classDependency on File modify
    public void onFileModify(Path filePath,Set<String> sourceClasses){
        Path normalizedPath = normalize(filePath);
        buildDependencyGraph(normalizedPath,sourceClasses);
        getAffectedClassDependencies(classDependencies);
    }

    // update on file Delete
    public void onFileDelete(Path filePath){
        Path normalizedPath = normalize(filePath);
        classDependencies.remove(normalizedPath);
        getAffectedClassDependencies(classDependencies);
    }
    // get the maps
    public Map<Path,Map<String, Set<String>>> getClassDependenciesToPath() {
        return classDependencies;
    }
    public Map<String, Set<String>> getReverseClassDependencies() {
        return reverseClassDependencies;
    }

    public Map<String, Set<String>> getDependsOn() {
        Map<String, Set<String>> dependsOn = new HashMap<>();
        for (Map<String, Set<String>> perFileDeps : classDependencies.values()) {
            for (Map.Entry<String, Set<String>> entry : perFileDeps.entrySet()) {
                String className = entry.getKey();
                Set<String> deps = entry.getValue();
                dependsOn
                        .computeIfAbsent(className, k -> new HashSet<>())
                        .addAll(deps);
            }
        }
        return dependsOn;
    }


    // Circular - dependency group
    CircularDependency circularDependency=new CircularDependency();
    // first get the required Map

    private Set<Set<String>> updateClassDependencies(){
        classDependenciesGroups.clear();
        Map<Path,Map<String, Set<String>>>perFileDeps = getClassDependenciesToPath();
        for(Map.Entry<Path,Map<String, Set<String>>> entry:perFileDeps.entrySet()){
            for (Map.Entry<String, Set<String>> e : entry.getValue().entrySet()) {
                classDependenciesGroups
                        .computeIfAbsent(e.getKey(), k -> new HashSet<>())
                        .addAll(e.getValue());
            }}
        return circularDependency.findCircularDependency(classDependenciesGroups);
    }
    // get Cirulardependencies graph
    public Set<Set<String>> getClassDependenciesGroups() {
        return updateClassDependencies();
    }
    private static void resolveType(Type type, Set<String> deps, Set<String> sourceClasses) {

        if (type.isPrimitiveType() || type.isVoidType()) return;

        try {
            ResolvedReferenceType resolved =
                    type.resolve().asReferenceType();
            String name=resolved.getQualifiedName();
            if (sourceClasses.contains(name)) {
                deps.add(name);
            }
        } catch (Exception e) {

        }
    }
}
