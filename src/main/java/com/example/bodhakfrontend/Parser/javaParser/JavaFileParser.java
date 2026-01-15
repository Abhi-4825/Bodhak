package com.example.bodhakfrontend.Parser.javaParser;

import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.util.ClassNameResolver;
import com.example.bodhakfrontend.util.ParseCache;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class JavaFileParser {

 private final ParseCache cache;

    public JavaFileParser(ParseCache cache) {
        this.cache = cache;
    }

    public Set<String> getClassesfromSource(List<Path> sourceRoot) {
        Set<String> projectclasses = new HashSet<>();
        for(Path path: sourceRoot) {
            scanSourceRoot(path,projectclasses);
        }
        return projectclasses;
    }

    private void scanSourceRoot(Path sourceRoot, Set<String> projectclasses) {
        try(Stream<Path> paths= Files.walk(sourceRoot) ) {
            paths.filter(p-> p.toString().endsWith(".java"))
                    .forEach(javaFile->{
                        try {
                            CompilationUnit cu=cache.get(javaFile);
                            cu.findAll(ClassOrInterfaceDeclaration.class)
                                    .forEach(classOrI -> {
                                        String className = ClassNameResolver.resolveFqn(cu,classOrI);
                                        projectclasses.add(className);
                                    });
                            cu.findAll(EnumDeclaration.class).forEach(enumDeclaration -> {
                                String  enumName = ClassNameResolver.resolveFqn(cu,enumDeclaration);
                                projectclasses.add(enumName);
                            });
                            cu.findAll(RecordDeclaration.class).forEach(recordDeclaration -> {
                                String  recordName = ClassNameResolver.resolveFqn(cu,recordDeclaration);
                                projectclasses.add(recordName);
                            });
                        }catch (Exception ignored){}

                    });
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    // get dependencies from every class in the java file
    public Map<String,Set<String>> extractClassWiseDependencies(File file, Set<String> sourceClasses) throws FileNotFoundException {
        CompilationUnit cu=cache.get(file.toPath());
        Map<String,Set<String>> classDeps=new HashMap<>();
        // find all class in the file
       for(ClassOrInterfaceDeclaration classOrI: cu.findAll(ClassOrInterfaceDeclaration.class)) {
           String name=classOrI.getNameAsString();
           Set<String> dependencies=new HashSet<>();
           classOrI.findAll(ClassOrInterfaceType.class).forEach(
                   type -> {
                       String typeName=type.getNameAsString();
                       if(!typeName.equals(name)){
                           if(sourceClasses.contains(typeName)){
                               dependencies.add(typeName);
                           }

                       }
                   }
           );
           classDeps.put(name,dependencies);

       }

        return classDeps;

    }

    public Map<String, Set<String>> buildClassDependencyGraph(
            Path sourceRoots,
            Set<String> sourceClasses
    ) {
        Map<String, Set<String>> depMap = new HashMap<>();
            try {
                Files.walk(sourceRoots)
                        .filter(p -> p.toString().endsWith(".java"))
                        .forEach(p -> {
                            try {
                                CompilationUnit cu = cache.get(p);

                                for (ClassOrInterfaceDeclaration cls :
                                        cu.findAll(ClassOrInterfaceDeclaration.class)) {

                                    String className = cls.getNameAsString();

                                    Set<String> deps =
                                            depMap.computeIfAbsent(
                                                    className,
                                                    k -> new HashSet<>()
                                            );

                                    cls.findAll(ClassOrInterfaceType.class)
                                            .forEach(type -> {
                                                String dep = type.getNameAsString();
                                                if (!dep.equals(className)
                                                        && sourceClasses.contains(dep)) {
                                                    deps.add(dep);
                                                }
                                            });
                                    cls.findAll(MethodCallExpr.class).forEach(m -> {
                                        m.getScope().ifPresent(scope -> {
                                            String name=scope.toString();
                                            if(!name.equals(className) && sourceClasses.contains(name)) {
                                                deps.add(name);
                                            }
                                        });
                                    });


                                }

                            } catch (Exception ignored) {}
                        });
            } catch (Exception ignored) {}


        return depMap;
    }
    public List<String > getFileClasses(File file){
        List<String> classes=new ArrayList<>();
        try{
            CompilationUnit cu = cache.get(file.toPath());
            for(ClassOrInterfaceDeclaration clazz:cu.findAll(ClassOrInterfaceDeclaration.class)){
                classes.add(clazz.getNameAsString());
            }
            for(EnumDeclaration clazz:cu.findAll(EnumDeclaration.class)){
                classes.add(clazz.getNameAsString());
            }
            for(RecordDeclaration clazz:cu.findAll(RecordDeclaration.class)){
                classes.add(clazz.getNameAsString());
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return classes;
    }






}
