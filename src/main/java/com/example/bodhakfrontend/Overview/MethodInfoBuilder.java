//package com.example.bodhakfrontend.Overview;
//
//import com.example.bodhakfrontend.Builder.MethodCallInfoBuilder;
//import com.example.bodhakfrontend.Models.*;
//import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
//import com.example.bodhakfrontend.util.MultiModuleSourceRootDetector;
//import com.example.bodhakfrontend.util.ParseCache;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.body.MethodDeclaration;
//import com.github.javaparser.ast.expr.MethodCallExpr;
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.*;
//
//public class MethodInfoBuilder {
//    private final JavaFileParser javaFileParser ;
//    private final MultiModuleSourceRootDetector rootDetector;
//    private final ParseCache cache;
//    Set<String> srcClasses;
//    private MethodCallInfoBuilder methodCallInfoBuilder;
//    Map<String, List<MethodsInfo>> methodsInfos = new HashMap<>();
//
//    public MethodInfoBuilder(JavaFileParser javaFileParser, MultiModuleSourceRootDetector rootDetector, ParseCache cache) {
//        this.javaFileParser = javaFileParser;
//        this.rootDetector = rootDetector;
//        this.cache = cache;
//        this.methodCallInfoBuilder = new MethodCallInfoBuilder();
//    }
//
//    public Map<String, List<MethodsInfo>> build(Path projectRoot) {
//        List<Path> srcRoot=rootDetector.detectSourceRoots(projectRoot);
//        srcClasses=javaFileParser.getClassesfromSource(srcRoot);
//        try {
//            for (Path path : srcRoot) {
//            Files.walk(path)
//                    .filter(p -> p.toString().endsWith(".java"))
//                    .forEach(filePath -> {
//                        updateMethodInfoMap(filePath);
//                    });}
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return methodsInfos;
//    }
//
//    // for getting each File path cu
//
//    public void updateMethodInfoMap(Path filePath) {
//
//        try {
//            CompilationUnit cu=cache.get(filePath);
//            if(cu==null)return;
//
//            cu.findAll(ClassOrInterfaceDeclaration.class)
//                    .forEach(clazz -> {
//
//
//                        String className = clazz.getNameAsString();
//                        List<MethodsInfo> methodList = getMethodInfo(clazz,filePath.toFile());
//                        methodsInfos.put(className, methodList);
//                    });
//
//        } catch (Exception e) {
//            // log and continue
//            e.printStackTrace();
//        }
//
//
//
//    }
//
//
//
//    private List<MethodsInfo> getMethodInfo(
//            ClassOrInterfaceDeclaration clazz,
//            File file
//    ) {
//        List<MethodsInfo> methods = new ArrayList<>();
//
//        clazz.getMethods().forEach(method -> {
//
//            // ---- Parameters ----
//            List<MethodParameterInfo> params = new ArrayList<>();
//            method.getParameters().forEach(p ->
//                    params.add(new MethodParameterInfo(
//                            p.getNameAsString(),
//                            p.getTypeAsString()
//                    ))
//            );
//
//            // ---- Class FQN ----
//            String classFqn =
//                    clazz.getFullyQualifiedName()
//                            .orElse(clazz.getNameAsString());
//
//            // ---- Called methods ----
//            List<MethodCallInfo> calledMethods =
//                    methodCallInfoBuilder.build(method, classFqn);
//
//            // ---- Modifiers ----
//            List<ModifierKind> modifiers = new ArrayList<>();
//            method.getModifiers().forEach(m ->
//                    modifiers.add(
//                            ModifierKind.from(m.getKeyword())
//                    )
//            );
//
//            // ---- Line numbers ----
//            var range = method.getName().getRange().orElse(null);
//            if (range == null) return;
//
//            int startLine = range.begin.line;
//            int startColumn = range.begin.column;
//            int endLine = range.end.line;
//
//            boolean hasBody = method.getBody().isPresent();
//            int statementCount = hasBody
//                    ? method.getBody().get().getStatements().size()
//                    : 0;
//
//            // ---- Create MethodsInfo ----
//            MethodsInfo info = new MethodsInfo(
//                    clazz.getNameAsString(),
//                    method.getNameAsString(),
//                    method.getTypeAsString(),
//                    params,
//                    calledMethods,
//                    modifiers,
//                    startLine,
//                    endLine,
//                    startColumn,
//                    file,
//                    hasBody,
//                    statementCount
//            );
//
//            methods.add(info);
//        });
//
//        return methods;
//    }
//
//
//}
//
