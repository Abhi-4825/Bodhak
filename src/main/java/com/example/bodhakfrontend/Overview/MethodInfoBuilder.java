package com.example.bodhakfrontend.Overview;

import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
import com.example.bodhakfrontend.util.SymbolSolverConfig;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class MethodInfoBuilder {
    JavaFileParser javaFileParser = new JavaFileParser();
    MultiModuleSourceRootDetector rootDetector = new MultiModuleSourceRootDetector();
    Set<String> srcClasses;
    public Map<String, List<MethodsInfo>> build(Path projectRoot) {
        List<Path> srcRoot=rootDetector.detectSourceRoots(projectRoot);
        srcClasses=javaFileParser.getClassesfromSource(srcRoot);

        Map<String, List<MethodsInfo>> methodsInfos = new HashMap<>();
        JavaParser parser = SymbolSolverConfig.createParser(projectRoot);

        try {
            for (Path path : srcRoot) {
            Files.walk(path)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(filePath -> {

                        File file = filePath.toFile();

                        try {
                            CompilationUnit cu =
                                    parser.parse(file).getResult().orElse(null);
                            if(cu==null)return;

                            cu.findAll(ClassOrInterfaceDeclaration.class)
                                    .forEach(clazz -> {

                                        String className = clazz.getNameAsString();
                                        List<MethodsInfo> methodList = new ArrayList<>();
                                        List<ConstructorInfo> constructorList = new ArrayList<>();

                                     // for finding out Methods
                                        clazz.getMethods().forEach(method -> {

                                            // Parameters
                                            List<MethodParameterInfo> params = new ArrayList<>();
                                            method.getParameters().forEach(p ->
                                                    params.add(new MethodParameterInfo(
                                                            p.getNameAsString(),
                                                            p.getTypeAsString()
                                                    ))
                                            );

                                            // Called methods
                                            List<MethodCallInfo> calledMethods = new ArrayList<>();
                                            method.findAll(MethodCallExpr.class)
                                                    .forEach(call ->{
                                                            try{
                                                                var resolved=call.resolve();
                                                                String methodName=resolved.getName();
                                                                String declaringClass=resolved.declaringType().getName();
                                                                if(clazz.getName().equals(declaringClass)){
                                                                calledMethods.add(new MethodCallInfo(methodName,declaringClass, MethodCallInfo.CallType.INTERNAL));}
                                                                else if(srcClasses.contains(declaringClass)){
                                                                   calledMethods.add(new MethodCallInfo(methodName,declaringClass, MethodCallInfo.CallType.EXTERNAL));

                                                                }
                                                                else
                                                                    calledMethods.add(new MethodCallInfo(methodName,declaringClass, MethodCallInfo.CallType.LIBRARY));


                                                            } catch (Exception e) {
                                                                calledMethods.add(
                                                                        new MethodCallInfo(
                                                                                call.getNameAsString(),
                                                                                "unresolved",
                                                                                MethodCallInfo.CallType.LIBRARY
                                                                        )
                                                                );
                                                            }
                                                            }
                                                    );

                                            // Modifiers
                                            List<ModifierKind> modifiers = new ArrayList<>();
                                            method.getModifiers().forEach(m ->
                                                    modifiers.add(
                                                            ModifierKind.from(m.getKeyword())
                                                    )
                                            );

                                            // Line numbers
                                            int startLine = -1;
                                            int endLine = -1;
                                            if (method.getRange().isPresent()) {
                                                startLine = method.getRange().get().begin.line;
                                                endLine = method.getRange().get().end.line;
                                            }

                                            MethodsInfo info = new MethodsInfo(
                                                    className,
                                                    method.getNameAsString(),
                                                    method.getTypeAsString(),
                                                    params,
                                                    calledMethods,
                                                    modifiers,
                                                    startLine,
                                                    endLine,
                                                    file,method.getBody().isPresent()
                                            );

                                            methodList.add(info);
                                        });

                                        methodsInfos.put(className, methodList);
                                    });

                        } catch (Exception e) {
                            // log and continue
                            e.printStackTrace();
                        }
                    });}

        } catch (Exception e) {
            e.printStackTrace();
        }

        return methodsInfos;
    }

}

