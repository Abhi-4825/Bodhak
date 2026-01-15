//package com.example.bodhakfrontend.Builder;
//
//import com.example.bodhakfrontend.Models.ConstructorInfo;
//import com.example.bodhakfrontend.Models.DependencyNode;
//import com.example.bodhakfrontend.Models.IncrementalParseResult;
//import com.example.bodhakfrontend.Models.MethodsInfo;
//import com.example.bodhakfrontend.Overview.ConstructorInfoBuilder;
//import com.example.bodhakfrontend.Overview.MethodInfoBuilder;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.type.ClassOrInterfaceType;
//
//import java.io.File;
//import java.util.*;
//
//public class IncrementalResultbuilder {
//    private final MethodInfoBuilder methodInfoBuilder;
//    private final ConstructorInfoBuilder constructorInfoBuilder;
//
//    public IncrementalResultbuilder(MethodInfoBuilder methodInfoBuilder, ConstructorInfoBuilder constructorInfoBuilder) {
//        this.methodInfoBuilder = methodInfoBuilder;
//        this.constructorInfoBuilder = constructorInfoBuilder;
//    }
//
//    public List<IncrementalParseResult> buildAll(
//            CompilationUnit cu,
//            File source,
//            Set<String> sourceClasses
//    ) {
//        List<IncrementalParseResult> results = new ArrayList<>();
//
//        for (ClassOrInterfaceDeclaration cls :
//                cu.findAll(ClassOrInterfaceDeclaration.class)) {
//
//            String className = cls.getNameAsString();
//
//            String pkg = cu.getPackageDeclaration()
//                    .map(pk -> pk.getNameAsString())
//                    .orElse("default");
//
//            var rangeOpt = cls.getName().getRange();
//            int beginLine = rangeOpt.map(r -> r.begin.line).orElse(-1);
//            int beginColumn = rangeOpt.map(r -> r.begin.column).orElse(-1);
//
//            DependencyNode node =
//                    new DependencyNode(className, pkg, source, beginLine, beginColumn);
//
//            // Dependencies
//            Set<String> dependencies = new HashSet<>();
//            cls.findAll(ClassOrInterfaceType.class).forEach(type -> {
//                String dep = type.getNameAsString();
//                if (!dep.equals(className) && sourceClasses.contains(dep)) {
//                    dependencies.add(dep);
//                }
//            });
//            // extends
//            cls.getExtendedTypes()
//                    .forEach(t -> dependencies.add(t.getNameAsString()));
//
//// implements
//            cls.getImplementedTypes()
//                    .forEach(t -> dependencies.add(t.getNameAsString()));
//
//
//            // Methods
//            List<MethodsInfo> methods =
//                    new ArrayList<>();
//
//            // Constructors
//            List<ConstructorInfo> constructors =
//                    constructorInfoBuilder.getConstructorInfo(cls, source);
//
//            results.add(new IncrementalParseResult(
//                    className,
//                    node,
//                    dependencies,
//                    methods,
//                    constructors
//            ));
//        }
//        return results;
//    }
//
//
//}
