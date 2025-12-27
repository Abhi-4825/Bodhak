package com.example.bodhakfrontend.Parser.javaParser;

import com.example.bodhakfrontend.Models.ClassInfo;
import com.example.bodhakfrontend.Models.ClassKind;
import com.example.bodhakfrontend.dependenciesResultmodel.DependencyResult;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Stream;

public class JavaFileParser {
    //Ast
    public CompilationUnit parseJava(File file) throws Exception {
        return StaticJavaParser.parse(file);
    }
    // Extract import or dependencies
    public DependencyResult extractDependencies(File file,Set<String> sourceClasses) throws Exception {
      CompilationUnit cu = StaticJavaParser.parse(file);
      // imports
      List<String> externalDeps=cu.getImports()
              .stream()
              .map(i -> i.getNameAsString())
              .toList();
      // class dependencies
        Set<String> internalDeps=new HashSet<>();
        Set<String> javaLibrariesDependencies=new HashSet<>();
        cu.findAll(ClassOrInterfaceType.class).forEach(classOrI -> {
            {
                String name=classOrI.getNameAsString();
                if(sourceClasses.contains(name)){
                    internalDeps.add(name);
                }
                else
                    javaLibrariesDependencies.add(name);
            }
        });

        return  new DependencyResult(externalDeps,new ArrayList<>(internalDeps),new ArrayList<>(javaLibrariesDependencies));
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
                            CompilationUnit cu=StaticJavaParser.parse(javaFile);
                            cu.findAll(ClassOrInterfaceDeclaration.class)
                                    .forEach(classOrI -> {
                                        projectclasses.add(classOrI.getNameAsString());
                                    });
                            cu.findAll(EnumDeclaration.class).forEach(enumDeclaration -> {
                                projectclasses.add(enumDeclaration.getNameAsString());
                            });
                            cu.findAll(RecordDeclaration.class).forEach(recordDeclaration -> {
                                projectclasses.add(recordDeclaration.getNameAsString());
                            });
                        }catch (Exception ignored){}

                    });
        } catch (IOException e) {
           e.printStackTrace();
        }
    }

    // get dependencies from every class in the java file
    public Map<String,Set<String>> extractClassWiseDependencies(File file, Set<String> sourceClasses) throws FileNotFoundException {
        CompilationUnit cu=StaticJavaParser.parse(file);
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
    public List<ClassInfo> extractClassInfo(Path sourceRoot) {

        List<ClassInfo> classInfos = new ArrayList<>();

        try {
            Files.walk(sourceRoot)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        try {
                            CompilationUnit cu = StaticJavaParser.parse(p);

                            String pkg = cu.getPackageDeclaration()
                                    .map(pk -> pk.getNameAsString())
                                    .orElse("default");

                            File sourceFile = p.toFile();

                            // ---- Classes & Interfaces ----
                            cu.findAll(ClassOrInterfaceDeclaration.class)
                                    .forEach(c -> {

                                        int start = c.getBegin().map(pos -> pos.line).orElse(0);
                                        int end   = c.getEnd().map(pos -> pos.line).orElse(start);

                                        ClassKind kind = c.isInterface()
                                                ? ClassKind.INTERFACE
                                                : ClassKind.CLASS;

                                        classInfos.add(
                                                new ClassInfo(
                                                        c.getNameAsString(),
                                                        pkg,
                                                        kind,
                                                        c.getMethods().size(),
                                                        c.getFields().size(),
                                                        c.getConstructors().size(),
                                                        c.isAbstract(),
                                                        c.isFinal(),
                                                        c.isPublic(),
                                                        end - start + 1,
                                                        sourceFile,
                                                        start
                                                )
                                        );
                                    });

                            // ---- Enums ----
                            cu.findAll(EnumDeclaration.class)
                                    .forEach(e -> {

                                        int start = e.getBegin().map(pos -> pos.line).orElse(0);
                                        int end   = e.getEnd().map(pos -> pos.line).orElse(start);

                                        classInfos.add(
                                                new ClassInfo(
                                                        e.getNameAsString(),
                                                        pkg,
                                                        ClassKind.ENUM,
                                                        e.getMethods().size(),
                                                        e.getFields().size(),
                                                        0,
                                                        false,
                                                        false,
                                                        e.isPublic(),
                                                        end - start + 1,
                                                        sourceFile,
                                                        start
                                                )
                                        );
                                    });

                            // ---- Records ----
                            cu.findAll(RecordDeclaration.class)
                                    .forEach(r -> {

                                        int start = r.getBegin().map(pos -> pos.line).orElse(0);
                                        int end   = r.getEnd().map(pos -> pos.line).orElse(start);

                                        classInfos.add(
                                                new ClassInfo(
                                                        r.getNameAsString(),
                                                        pkg,
                                                        ClassKind.RECORD,
                                                        r.getMethods().size(),
                                                        r.getFields().size(),
                                                        r.getConstructors().size(),
                                                        false,
                                                        r.isFinal(),
                                                        r.isPublic(),
                                                        end - start + 1,
                                                        sourceFile,
                                                        start
                                                )
                                        );
                                    });

                        } catch (Exception e) {
                            System.err.println("Failed to parse: " + p);
                            e.printStackTrace();
                        }
                    });

        } catch (Exception e) {
            e.printStackTrace();
        }

        return classInfos;
    }
    public Map<String, Set<String>> buildProjectDependencyGraph(
            Path sourceRoots,
            Set<String> sourceClasses
    ) {
        Map<String, Set<String>> depMap = new HashMap<>();
            try {
                Files.walk(sourceRoots)
                        .filter(p -> p.toString().endsWith(".java"))
                        .forEach(p -> {
                            try {
                                CompilationUnit cu = StaticJavaParser.parse(p);

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

                                }

                            } catch (Exception ignored) {}
                        });
            } catch (Exception ignored) {}


        return depMap;
    }
    public List<String > getFileClasses(File file){
        List<String> classes=new ArrayList<>();
        try{
            CompilationUnit cu = StaticJavaParser.parse(file);
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
