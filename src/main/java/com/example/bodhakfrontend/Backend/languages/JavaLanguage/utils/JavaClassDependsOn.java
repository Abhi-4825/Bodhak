package com.example.bodhakfrontend.Backend.languages.JavaLanguage.utils;

import com.example.bodhakfrontend.Backend.Factory.ParserFactory;
import com.example.bodhakfrontend.Backend.interfaces.ClassDependenciesBuilder;
import com.example.bodhakfrontend.Backend.interfaces.Parser;
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

import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class JavaClassDependsOn implements ClassDependenciesBuilder {
    private final ParserFactory parserFactory;
    private final Parser<CompilationUnit> parser;

    public JavaClassDependsOn(ParserFactory  parserFactory) {
        this.parserFactory = parserFactory;
        this.parser = parserFactory.getParser("java");
    }

    @Override
    public Map<String,Set<String>> getDependencies(Path filePath, Set<String> sourceClasses) {
        Map<String,Set<String>> dependencyGraph = new HashMap<>();

        Path normalizedPath = filePath.toAbsolutePath().normalize();
        CompilationUnit cu = parser.parse(normalizedPath);
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
            dependencyGraph
                    .computeIfAbsent(className, k -> new HashSet<>()).addAll(dependencies);
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

            dependencyGraph.computeIfAbsent(recordName, k -> new HashSet<>()).addAll(dependencies);
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

            dependencyGraph.computeIfAbsent(enumName, k -> new HashSet<>()).addAll(dependencies);
        });
        return dependencyGraph;


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
