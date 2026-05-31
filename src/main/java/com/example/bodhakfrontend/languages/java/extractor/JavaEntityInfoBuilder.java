package com.example.bodhakfrontend.languages.java.extractor;

import com.example.bodhakfrontend.core.model.entity.*;
import com.example.bodhakfrontend.core.plugin.EntityInfoBuilder;
import com.example.bodhakfrontend.core.plugin.Parser;
import com.example.bodhakfrontend.languages.java.util.JavaClassNameResolver;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class JavaEntityInfoBuilder implements EntityInfoBuilder {

    private static final Set<String> SPRING_ANNOTATIONS = Set.of(
            "SpringBootApplication", "Configuration", "Component", "Service",
            "Repository", "Controller", "RestController", "ComponentScan", "EnableAutoConfiguration"
    );

    private final Parser<CompilationUnit> parser;

    public JavaEntityInfoBuilder(Parser<CompilationUnit> parser) {
        this.parser = parser;
    }

    @Override
    public List<EntityInfo> build(Path filePath) {
        Path normalizedPath = filePath.toAbsolutePath().normalize();
        List<EntityInfo> classInfoList = new ArrayList<>();

        if (!normalizedPath.toString().endsWith(".java")) { return classInfoList; }
        
        File sourceFile = normalizedPath.toFile();
        CompilationUnit cu = parser.parse(normalizedPath);
        if (cu == null) return classInfoList;

        cu.findAll(TypeDeclaration.class).forEach(clazz -> {
            String packageName = cu.getPackageDeclaration().map(p -> p.getNameAsString()).orElse("");
            String className = JavaClassNameResolver.resolveFqn(cu, clazz);
            
            EntityKind kind = EntityKind.CLASS;
            if (clazz.isEnumDeclaration()) kind = EntityKind.ENUM;
            else if (clazz.isRecordDeclaration()) kind = EntityKind.RECORD;
            else if (clazz.isClassOrInterfaceDeclaration() && clazz.asClassOrInterfaceDeclaration().isInterface()) kind = EntityKind.INTERFACE;

            Set<String> fields = extractFields(clazz);
            List<MemberInfo> members = new ArrayList<>();
            members.addAll(getMethods(clazz, className, sourceFile));
            members.addAll(getConstructors(clazz, sourceFile));

            Set<String> annotations = getAnnotations(clazz);
            
            Set<String> tags = new HashSet<>();
            if (annotations.stream().anyMatch(SPRING_ANNOTATIONS::contains)) {
                tags.add("spring_boot");
                tags.add("framework_root");
            }

            if (clazz.isClassOrInterfaceDeclaration()) {
                boolean hasMain = clazz.asClassOrInterfaceDeclaration().getMethodsByName("main").stream()
                        .anyMatch(m -> m.isPublic() && m.isStatic() && m.getType().isVoidType()
                                && m.getParameters().size() == 1
                                && m.getParameter(0).getType().asString().equals("String[]"));
                if (hasMain) tags.add("has_main");

                ClassOrInterfaceDeclaration ci = clazz.asClassOrInterfaceDeclaration();
                boolean extendsApp = ci.getExtendedTypes().stream()
                        .anyMatch(t -> t.getNameAsString().equals("Application") || t.getNameAsString().equals("javafx.application.Application"));
                boolean usesFXML = annotations.contains("FXML");
                boolean importsJavaFx = cu.getImports().stream()
                        .anyMatch(i -> i.getNameAsString().startsWith("javafx."));
                
                if (extendsApp || usesFXML || importsJavaFx) tags.add("javafx");
            }

            if (annotations.stream().anyMatch(a -> Set.of("Entity", "Table", "MappedSuperclass", "Embeddable").contains(a))) {
                tags.add("jpa_entity");
            }

            if (annotations.contains("RestController") || annotations.contains("RequestMapping")) {
                tags.add("rest_controller");
                tags.add("framework_root");
            }

            if (annotations.contains("Test") || annotations.contains("ParameterizedTest") || annotations.contains("SpringBootTest")) {
                tags.add("test_class");
            }

            EntityContribution contribution = new EntityContribution(tags);

            int loc = getLinesOfCode(clazz);
            int beginLine = getBeginLine(clazz);
            int beginColumn = getBeginColumn(clazz);

            boolean isAbstract = false;
            boolean isFinal = false;

            if (clazz.isClassOrInterfaceDeclaration()) {
                ClassOrInterfaceDeclaration ci = clazz.asClassOrInterfaceDeclaration();
                isAbstract = ci.isAbstract();
                isFinal = ci.isFinal();
            }

            EntityInfo info = new EntityInfo(
                    className, packageName, sourceFile, kind, "java",
                    fields, members, annotations,
                    new HashSet<>(), new HashSet<>(), new HashSet<>(), // graph fields to be filled by Engine
                    isAbstract, isFinal, clazz.isPublic(),
                    loc, beginLine, beginColumn, contribution
            );
            
            classInfoList.add(info);
        });

        return classInfoList;
    }

    @Override
    public void invalidate(Path filePath) {
        parser.invalidate(filePath);
    }

    private Set<String> extractFields(TypeDeclaration<?> typeDecl) {
        Set<String> fields = new HashSet<>();
        if (typeDecl.isClassOrInterfaceDeclaration()) {
            typeDecl.asClassOrInterfaceDeclaration().getFields().forEach(f -> f.getVariables().forEach(v -> fields.add(v.getNameAsString())));
        } else if (typeDecl.isEnumDeclaration()) {
            typeDecl.asEnumDeclaration().getFields().forEach(f -> f.getVariables().forEach(v -> fields.add(v.getNameAsString())));
        } else if (typeDecl.isRecordDeclaration()) {
            typeDecl.asRecordDeclaration().getParameters().forEach(p -> fields.add(p.getNameAsString()));
        }
        return fields;
    }

    private List<MemberInfo> getMethods(TypeDeclaration<?> clazz, String className, File file) {
        List<MemberInfo> methods = new ArrayList<>();
        if (clazz.isAnnotationDeclaration()) return methods;

        clazz.getMethods().forEach(method -> {
            String methodName = method.getNameAsString();
            String returnType = method.getTypeAsString();

            List<ParameterInfo> params = new ArrayList<>();
            method.getParameters().forEach(p -> params.add(new ParameterInfo(p.getNameAsString(), p.getTypeAsString())));

            List<MethodCallInfo> calledMethods = getCalledMethods(method, className);

            List<ModifierKind> modifiers = new ArrayList<>();
            method.getModifiers().forEach(m -> modifiers.add(ModifierKind.from(m.getKeyword().asString())));

            // Use the full method range (including body) not just the name token
            var range = method.getRange().orElse(null);
            if (range == null) return;

            int statementCount = method.getBody().map(b -> b.findAll(Statement.class).size()).orElse(0);

            methods.add(new MemberInfo(
                    MemberKind.METHOD, methodName, returnType, params, calledMethods,
                    modifiers, range.begin.line, range.end.line, range.begin.column, statementCount, file
            ));
        });
        return methods;
    }

    private List<MemberInfo> getConstructors(TypeDeclaration<?> clazz, File file) {
        List<MemberInfo> constructors = new ArrayList<>();
        if (clazz.isAnnotationDeclaration()) return constructors;

        clazz.getConstructors().forEach(c -> {
            String constructorName = c.getNameAsString();
            List<ParameterInfo> params = new ArrayList<>();
            c.getParameters().forEach(param -> params.add(new ParameterInfo(param.getNameAsString(), param.getTypeAsString())));

            List<ModifierKind> modifiers = new ArrayList<>();
            c.getModifiers().forEach(m -> modifiers.add(ModifierKind.from(m.getKeyword().asString())));

            // Use the full constructor range (including body) not just the name token
            var range = c.getRange().orElse(null);
            if (range == null) return;

            int statementCount = c.getBody().findAll(Statement.class).size();

            constructors.add(new MemberInfo(
                    MemberKind.CONSTRUCTOR, constructorName, "", params, new ArrayList<>(),
                    modifiers, range.begin.line, range.end.line, range.begin.column, statementCount, file
            ));
        });
        return constructors;
    }

    private List<MethodCallInfo> getCalledMethods(MethodDeclaration method, String callerClass) {
        List<MethodCallInfo> callInfos = new ArrayList<>();
        for (MethodCallExpr m : method.findAll(MethodCallExpr.class)) {
            String methodName = m.getNameAsString();
            Expression scope = m.getScope().orElse(null);
            String fromClass = "UNKNOWN";
            MethodCallInfo.CallType type = MethodCallInfo.CallType.EXTERNAL;

            if (scope == null) {
                fromClass = callerClass;
                type = MethodCallInfo.CallType.INTERNAL;
            } else {
                String scopeText = scope.toString();
                if (scopeText.equals("this")) {
                    fromClass = callerClass;
                    type = MethodCallInfo.CallType.INTERNAL;
                } else if (Character.isUpperCase(scopeText.charAt(0))) {
                    fromClass = scopeText;
                    type = MethodCallInfo.CallType.LIBRARY;
                }
            }
            callInfos.add(new MethodCallInfo(methodName, callerClass, fromClass, type));
        }
        return callInfos;
    }

    private Set<String> getAnnotations(TypeDeclaration<?> clazz) {
        Set<String> annotations = new HashSet<>();
        clazz.getAnnotations().forEach(a -> annotations.add(a.getNameAsString()));
        return annotations;
    }

    private int getLinesOfCode(TypeDeclaration<?> typeDecl) {
        return typeDecl.getRange().map(r -> r.end.line - r.begin.line + 1).orElse(0);
    }

    private int getBeginLine(TypeDeclaration<?> typeDecl) {
        return typeDecl.getName().getRange().map(r -> r.begin.line).orElse(0);
    }

    private int getBeginColumn(TypeDeclaration<?> typeDecl) {
        return typeDecl.getName().getRange().map(r -> r.begin.column).orElse(0);
    }
}
