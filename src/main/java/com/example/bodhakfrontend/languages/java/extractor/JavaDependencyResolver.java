package com.example.bodhakfrontend.languages.java.extractor;

import com.example.bodhakfrontend.core.plugin.DependencyResolver;
import com.example.bodhakfrontend.core.plugin.Parser;
import com.example.bodhakfrontend.languages.java.util.JavaClassNameResolver;
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

public class JavaDependencyResolver implements DependencyResolver {
    
    private final Parser<CompilationUnit> parser;

    public JavaDependencyResolver(Parser<CompilationUnit> parser) {
        this.parser = parser;
    }

    @Override
    public Map<String, Set<String>> resolve(Path filePath, Set<String> knownNames) {
        Map<String, Set<String>> dependencyGraph = new HashMap<>();

        Path normalizedPath = filePath.toAbsolutePath().normalize();
        CompilationUnit cu = parser.parse(normalizedPath);
        if (cu == null) return dependencyGraph;

        // 1> first for classes or interfaces
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(clazz -> {
            Set<String> dependencies = new HashSet<>();
            String className = JavaClassNameResolver.resolveFqn(cu, clazz);

            // fields types
            clazz.getFields().forEach(field -> {
                field.getVariables().forEach(variable -> {
                    resolveType(variable.getType(), dependencies, knownNames);
                });
            });

            // constructor parameter type
            clazz.getConstructors().forEach(constructor -> {
                constructor.getParameters().forEach(parameter -> {
                    resolveType(parameter.getType(), dependencies, knownNames);
                });
            });

            // method Parameter type and return type
            clazz.getMethods().forEach(method -> {
                method.getParameters().forEach(parameter -> {
                    resolveType(parameter.getType(), dependencies, knownNames);
                });
                resolveType(method.getType(), dependencies, knownNames);
            });

            // methods called
            clazz.findAll(MethodCallExpr.class).forEach(call -> {
                try {
                    String dep = call.resolve().declaringType().getQualifiedName();
                    if (knownNames.contains(dep)) dependencies.add(dep);
                } catch (Exception ignored) {}
            });

            // object creation
            clazz.findAll(ObjectCreationExpr.class).forEach(expr -> {
                try {
                    String dep = expr.getType().resolve().asReferenceType().getQualifiedName();
                    if (knownNames.contains(dep)) dependencies.add(dep);
                } catch (Exception e) {
                    String simpleName = expr.getType().asString();
                    for (String cls : knownNames) {
                        if (cls.endsWith("." + simpleName)) {
                            dependencies.add(cls);
                            break;
                        }
                    }
                }
            });

            // generics
            clazz.findAll(ClassOrInterfaceType.class).forEach(type ->
                    type.getTypeArguments().ifPresent(args ->
                            args.forEach(arg -> resolveType(arg, dependencies, knownNames))
                    )
            );

            // inheritance and extended
            clazz.getExtendedTypes().forEach(extendedType -> resolveType(extendedType, dependencies, knownNames));
            clazz.getImplementedTypes().forEach(implementedType -> resolveType(implementedType, dependencies, knownNames));

            // annotation
            clazz.getAnnotations().forEach(ann -> {
                try {
                    String dep = ann.resolve().getQualifiedName();
                    if (knownNames.contains(dep)) dependencies.add(dep);
                } catch (Exception ignored) {}
            });

            dependencies.remove(className);
            dependencyGraph.computeIfAbsent(className, k -> new HashSet<>()).addAll(dependencies);
        });

        // Records
        cu.findAll(RecordDeclaration.class).forEach(record -> {
            String recordName = JavaClassNameResolver.resolveFqn(cu, record);
            Set<String> dependencies = new HashSet<>();

            record.getParameters().forEach(param -> resolveType(param.getType(), dependencies, knownNames));
            record.getImplementedTypes().forEach(type -> resolveType(type, dependencies, knownNames));
            record.getAnnotations().forEach(ann -> {
                try {
                    String dep = ann.resolve().getQualifiedName();
                    if (knownNames.contains(dep)) dependencies.add(dep);
                } catch (Exception ignored) {}
            });

            dependencies.remove(recordName);
            dependencyGraph.computeIfAbsent(recordName, k -> new HashSet<>()).addAll(dependencies);
        });

        // Enums
        cu.findAll(EnumDeclaration.class).forEach(enumDecl -> {
            String enumName = JavaClassNameResolver.resolveFqn(cu, enumDecl);
            Set<String> dependencies = new HashSet<>();

            enumDecl.getImplementedTypes().forEach(type -> resolveType(type, dependencies, knownNames));
            enumDecl.getFields().forEach(field ->
                    field.getVariables().forEach(var -> resolveType(var.getType(), dependencies, knownNames))
            );
            enumDecl.getConstructors().forEach(cons ->
                    cons.getParameters().forEach(param -> resolveType(param.getType(), dependencies, knownNames))
            );
            enumDecl.getMethods().forEach(method -> {
                method.getParameters().forEach(param -> resolveType(param.getType(), dependencies, knownNames));
                resolveType(method.getType(), dependencies, knownNames);
            });
            enumDecl.findAll(MethodCallExpr.class).forEach(call -> {
                try {
                    String dep = call.resolve().declaringType().getQualifiedName();
                    if (knownNames.contains(dep)) dependencies.add(dep);
                } catch (Exception ignored) {}
            });
            enumDecl.findAll(ObjectCreationExpr.class).forEach(expr -> {
                try {
                    String dep = expr.getType().resolve().asReferenceType().getQualifiedName();
                    if (knownNames.contains(dep)) dependencies.add(dep);
                } catch (Exception ignored) {}
            });
            enumDecl.getAnnotations().forEach(ann -> {
                try {
                    String dep = ann.resolve().getQualifiedName();
                    if (knownNames.contains(dep)) dependencies.add(dep);
                } catch (Exception ignored) {}
            });

            dependencies.remove(enumName);
            dependencyGraph.computeIfAbsent(enumName, k -> new HashSet<>()).addAll(dependencies);
        });

        return dependencyGraph;
    }

    private static void resolveType(Type type, Set<String> deps, Set<String> sourceClasses) {
        if (type.isPrimitiveType() || type.isVoidType()) return;

        try {
            ResolvedReferenceType resolved = type.resolve().asReferenceType();
            String name = resolved.getQualifiedName();
            if (sourceClasses.contains(name)) {
                deps.add(name);
            }
        } catch (Exception e) {
            String simpleName = type.asString();
            for (String cls : sourceClasses) {
                if (cls.endsWith("." + simpleName)) {
                    deps.add(cls);
                    break;
                }
            }
        }
    }
}
