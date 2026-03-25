package com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder;

import com.example.bodhakfrontend.Backend.ClassGraphBuilder;
import com.example.bodhakfrontend.Backend.Factory.ParserFactory;
import com.example.bodhakfrontend.Backend.interfaces.ClassInfoBuilder;
import com.example.bodhakfrontend.Backend.interfaces.Parser;
import com.example.bodhakfrontend.Backend.models.Class.*;
import com.example.bodhakfrontend.projectAnalysis.warning.WarningBuilder;
import com.example.bodhakfrontend.util.ClassNameResolver;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.*;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.Statement;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


import java.util.*;

import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class javaClassInfoBuilder implements ClassInfoBuilder {
    private static final Set<String> SPRING_ANNOTATIONS = Set.of(
            "SpringBootApplication",
            "Configuration",
            "Component",
            "Service",
            "Repository",
            "Controller",
            "RestController",
            "ComponentScan",
            "EnableAutoConfiguration"
    );

    private final WarningBuilder warningBuilder = new WarningBuilder();
    private final ParserFactory parserFactory;

    private final Parser<CompilationUnit> cache;
    private final ClassGraphBuilder classGraphBuilder;



    private Map<Path, List<ClassInfo>> classInfoMap = new ConcurrentHashMap<>();
    private Set<String> srcClasses;


    private Path normalize(Path path) {
        return path.toAbsolutePath().normalize();
    }

    public javaClassInfoBuilder(ParserFactory parserFactory, ClassGraphBuilder classGraphBuilder) {
        this.parserFactory = parserFactory;
        this.cache =parserFactory.getParser("java");
        this.classGraphBuilder = classGraphBuilder;

    }
    public Map<Path, List<ClassInfo>> buildAll(Path projectPath,Set<String> sourceClasses) {
        classInfoMap.clear();
        srcClasses = sourceClasses;
        classGraphBuilder.buildDependsOnGraph(projectPath,srcClasses);
        try {
            Files.walk(projectPath)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(path -> {

                        List<ClassInfo> classInfos = getClassInfos(path);
                        classInfoMap.put(normalize(path), classInfos);


                    });


        } catch (Exception e) {
            System.out.println("Error building classInfoMap" + e.getMessage());
        }


        return classInfoMap;
    }


    public List<ClassInfo> getClassInfos(Path path) {
        Path normalizedPath = normalize(path);
        List<ClassInfo> classInfoList = new ArrayList<>();
        Set<Set<String>> AllCircularGroups = classGraphBuilder.getClassDependenciesGroups();

        if(!normalizedPath.toString().endsWith(".java")) {return classInfoList;}
        try {
            File sourceFile = path.toFile();
            CompilationUnit cu = cache.parse(normalizedPath);
            cu.findAll(TypeDeclaration.class).forEach(clazz -> {
                // Package
                String packageName = cu.getPackageDeclaration()
                        .map(p -> p.getNameAsString())
                        .orElse("");
                //get Full Class name With Pkg
                String className = ClassNameResolver.resolveFqn(cu, clazz);
                ClassInfo.Kind kind =
                        clazz.isEnumDeclaration()
                                ? ClassInfo.Kind.ENUM
                                : clazz.isRecordDeclaration()
                                ? ClassInfo.Kind.RECORDS
                                : clazz.isClassOrInterfaceDeclaration()
                                && clazz.asClassOrInterfaceDeclaration().isInterface()
                                ? ClassInfo.Kind.INTERFACE
                                : ClassInfo.Kind.CLASS;


                // find out the Fields
                Set<String> fields = extractFields(clazz);


                // find out methods
                List<MethodInfo> methods = getMethodInfo(clazz, className, sourceFile);

                //find out Constructor
                List<ConstructorInfo> construcors = getConstructors(clazz,sourceFile);

                //Annotations in the class
                Set<String> annotations = getAnnotations(clazz);

                boolean hasSpring =
                        annotations.stream().anyMatch(SPRING_ANNOTATIONS::contains);

                // contains main
                boolean hasMain = false;
                if (clazz.isClassOrInterfaceDeclaration()) {
                    hasMain = clazz.asClassOrInterfaceDeclaration()
                            .getMethodsByName("main")
                            .stream()
                            .anyMatch(m ->
                                    m.isPublic()
                                            && m.isStatic()
                                            && m.getType().isVoidType()
                                            && m.getParameters().size() == 1
                                            && m.getParameter(0).getType().asString().equals("String[]")
                            );
                }

                // contains javaFx
                boolean isJavaFx = false;
                if (clazz.isClassOrInterfaceDeclaration()) {
                    ClassOrInterfaceDeclaration ci = clazz.asClassOrInterfaceDeclaration();

                    boolean extendsApplication =
                            ci.getExtendedTypes().stream()
                                    .anyMatch(t ->
                                            t.getNameAsString().equals("Application") ||
                                                    t.getNameAsString().equals("javafx.application.Application")
                                    );

                    boolean usesFXML =
                            annotations.contains("FXML");

                    boolean importsJavaFx =
                            cu.getImports().stream()
                                    .anyMatch(i -> i.getNameAsString().startsWith("javafx."));

                    isJavaFx = extendsApplication || usesFXML || importsJavaFx;
                }

                //has jpa
                boolean hasJPA = annotations.stream()
                        .anyMatch(a -> Set.of(
                                "Entity",
                                "Table",
                                "MappedSuperclass",
                                "Embeddable"
                        ).contains(a));


                // is restController
                boolean isRestController =
                        annotations.contains("RestController") ||
                                annotations.contains("RequestMapping");

                //is test Class
                boolean isTestClass =
                        annotations.contains("Test")
                                || annotations.contains("ParameterizedTest")
                                || annotations.contains("SpringBootTest");


                // classContributions
                ClassContribution classContribution = new ClassContribution(hasSpring, isJavaFx, hasMain, hasJPA, isRestController, isTestClass);


                //Set<String> Class depends on
                Set<String> dependsOn =
                        classGraphBuilder
                                .getClassDependenciesToPath()
                                .getOrDefault(normalizedPath, Map.of())
                                .getOrDefault(className, Set.of());

                Set<String> usedBy =
                        classGraphBuilder
                                .getReverseClassDependencies()
                                .getOrDefault(className, Set.of());


                //find circular dependency group
                Set<Set<String>> myCycles = new HashSet<>();

                for (Set<String> cycle : AllCircularGroups) {
                    if (cycle.contains(className)) {
                        myCycles.add(cycle);
                    }
                }
                //get loc
                int loc = getLinesOfCode(clazz);
                //get beginLine
                int beginLine = getBeginLine(clazz);
                //get beginColumn
                int beginColumn = getBeginColumn(clazz);

                boolean isAbstract = false;
                boolean isFinal = false;

                if (clazz.isClassOrInterfaceDeclaration()) {
                    ClassOrInterfaceDeclaration ci = clazz.asClassOrInterfaceDeclaration();
                    isAbstract = ci.isAbstract();
                    isFinal = ci.isFinal();
                }

                ClassInfo cls=new ClassInfo(className, packageName, path.toFile(), kind, fields, methods, construcors, annotations, dependsOn, usedBy, myCycles, isAbstract, isFinal, clazz.isPublic(), loc, beginLine, beginColumn,classContribution);
                cls.getWarnings().clear();
                cls.setWarnings(warningBuilder.buildWarnings(cls));
                classInfoList.add(cls);



            });


        } catch (Exception e) {
            System.out.println("Error building classInfoMap" + e.getMessage() + path.toString());
        }
        return classInfoList;

    }


    // to find out fields
    private Set<String> extractFields(TypeDeclaration<?> typeDecl) {

        Set<String> fields = new HashSet<>();

        if (typeDecl.isClassOrInterfaceDeclaration()) {
            typeDecl.asClassOrInterfaceDeclaration()
                    .getFields()
                    .forEach(f ->
                            f.getVariables().forEach(v ->
                                    fields.add(v.getNameAsString())
                            )
                    );
        } else if (typeDecl.isEnumDeclaration()) {
            typeDecl.asEnumDeclaration()
                    .getFields()
                    .forEach(f ->
                            f.getVariables().forEach(v ->
                                    fields.add(v.getNameAsString())
                            )
                    );
        } else if (typeDecl.isRecordDeclaration()) {
            typeDecl.asRecordDeclaration()
                    .getParameters()
                    .forEach(p -> fields.add(p.getNameAsString()));
        }

        return fields;
    }


    // to find out methods
    private List<MethodInfo> getMethodInfo(
            TypeDeclaration<?> clazz,
            String className,
            File file
    ) {
        List<MethodInfo> methods = new ArrayList<>();
        if (clazz.isAnnotationDeclaration()) {
            return List.of(); // skip annotations
        }

        clazz.getMethods().forEach(method -> {
            // method name
            String methodName = method.getNameAsString();
            // return type
            String returnType = method.getTypeAsString();

            //  Parameters
            List<ParameterInfo> params = new ArrayList<>();
            method.getParameters().forEach(p ->
                    params.add(new ParameterInfo(p.getNameAsString(), p.getTypeAsString()))
            );


            // Called methods
            List<MethodCallInfo> calledMethods = getCalledMethods(method, className);


            //  Modifiers
            List<ModifierKind> modifiers = new ArrayList<>();
            method.getModifiers().forEach(m ->
                    modifiers.add(
                            ModifierKind.from(m.getKeyword())
                    )
            );

            // Line numbers
            var range = method.getName().getRange().orElse(null);
            if (range == null) return;

            int startLine = range.begin.line;
            int startColumn = range.begin.column;
            int endLine = range.end.line;

            boolean hasBody = method.getBody().isPresent();
            int statementCount = method.getBody()
                    .map(b -> b.findAll(Statement.class).size())
                    .orElse(0);



            MethodInfo info = new MethodInfo(methodName, returnType, params, calledMethods, modifiers, startLine, startColumn, endLine, hasBody, statementCount,file);

            methods.add(info);
        });

        return methods;
    }

    // to find out methods called by methods
    private List<MethodCallInfo> getCalledMethods(MethodDeclaration method, String callerClass) {
        List<MethodCallInfo> callInfos = new ArrayList<>();
        for (MethodCallExpr m : method.findAll(MethodCallExpr.class)) {
            //method name
            String methodName = m.getNameAsString();
            //caller class==CallerClass
            //from class
            Expression scope = m.getScope().orElse(null);
            String fromClass = "UNKNOWN";
            MethodCallInfo.CallType type = MethodCallInfo.CallType.EXTERNAL;
            if (scope == null) {
                // e.g. foo();
                fromClass = callerClass;
                type = MethodCallInfo.CallType.INTERNAL;

            } else {
                String scopeText = scope.toString();

                if (scopeText.equals("this")) {
                    fromClass = callerClass;
                    type = MethodCallInfo.CallType.INTERNAL;

                } else if (Character.isUpperCase(scopeText.charAt(0))) {
                    // Likely static call or library
                    fromClass = scopeText;
                    type = MethodCallInfo.CallType.LIBRARY;

                } else {
                    // obj.method()
                    fromClass = "UNKNOWN";
                    type = MethodCallInfo.CallType.EXTERNAL;
                }
            }

            callInfos.add(new MethodCallInfo(methodName, callerClass, fromClass, type));

        }

        return callInfos;

    }


    // to find out constructors
    private List<ConstructorInfo> getConstructors(TypeDeclaration<?> clazz,File file) {
        List<ConstructorInfo> constructors = new ArrayList<>();
        if (clazz.isAnnotationDeclaration()) {
            return List.of();
        }
        clazz.getConstructors().forEach(c -> {
            // String constructor name
            String constructorName = c.getNameAsString();
            //parameters
            List<ParameterInfo> params = new ArrayList<>();
            c.getParameters().forEach(param -> {
                params.add(new ParameterInfo(param.getNameAsString(), param.getTypeAsString()));
            });


            //  Modifiers
            List<ModifierKind> modifiers = new ArrayList<>();
            c.getModifiers().forEach(m ->
                    modifiers.add(
                            ModifierKind.from(m.getKeyword())
                    )
            );

            // Line numbers
            var range = c.getName().getRange().orElse(null);
            if (range == null) return;

            int startLine = range.begin.line;
            int startColumn = range.begin.column;
            int endLine = range.end.line;

            constructors.add(new ConstructorInfo(constructorName, params, modifiers, startLine, startColumn, endLine,file));


        });
        return constructors;
    }

    private Set<String> getAnnotations(TypeDeclaration<?> clazz) {
        Set<String> annotations = new HashSet<>();
        clazz.getAnnotations().forEach(a -> {
            annotations.add(a.getNameAsString());
        });
        return annotations;
    }


    // to find loc
    private int getLinesOfCode(TypeDeclaration<?> typeDecl) {
        return typeDecl.getRange()
                .map(r -> r.end.line - r.begin.line + 1)
                .orElse(0);
    }

    private int getBeginLine(TypeDeclaration<?> typeDecl) {
        return typeDecl.getName().getRange()
                .map(r -> r.begin.line)
                .orElse(0);
    }

    private int getBeginColumn(TypeDeclaration<?> typeDecl) {
        return typeDecl.getName().getRange()
                .map(r -> r.begin.column)
                .orElse(0);
    }

    // has


    //Incremental part




    // get the classInfoMap
    public Map<Path, List<ClassInfo>> getClassInfoMap() {
        return classInfoMap;
    }

    // get List<ClassInfo>
    public List<ClassInfo> getListOfClassInfo() {
        List<ClassInfo> result = new ArrayList<>();
        classInfoMap.values().forEach(result::addAll);
        return result;
    }
    // classname to classinfo
    public Map<String,ClassInfo> getClassMap(List<ClassInfo> classInfoList) {
        Map<String,ClassInfo> result = new HashMap<>();
        for (ClassInfo classInfo : classInfoList) {
            result.put(classInfo.getClassName(), classInfo);
        }
        return result;
    }

    public Map<String,Set<ClassInfo>> getPkgToClassInfo(List<ClassInfo> classInfoList) {
        Map<String,Set<ClassInfo>> result = new HashMap<>();
        for (ClassInfo classInfo : classInfoList) {
            String packageName = classInfo.getPackageName();
            result.computeIfAbsent(packageName, k -> new HashSet<>()).add(classInfo);
        }
        return result;

    }
    public Set<String> collectSourceClasses(Path projectPath) {
        Set<String> result = new HashSet<>();
        try {
            Files.walk(projectPath)
                    .filter(p -> p.toString().endsWith(".java"))
                    .forEach(p -> {
                        CompilationUnit cu = cache.parse(p);
                        cu.findAll(TypeDeclaration.class)
                                .forEach(td ->
                                        result.add(ClassNameResolver.resolveFqn(cu, td))
                                );
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }


    //for file create
    public void onFileCreate(Path filePath) {
        Path p = normalize(filePath);
        CompilationUnit cu = cache.parse(p);
        cu.findAll(TypeDeclaration.class).forEach(td ->
                srcClasses.add(ClassNameResolver.resolveFqn(cu, td))
        );
        classGraphBuilder.onFileCreate(filePath, srcClasses);
        List<ClassInfo> classes = getClassInfos(filePath);
        classInfoMap.put(normalize(filePath), classes);
    }
    // on file deletion

    public void onFileDelete(Path filePath) {
        cache.invalidate(filePath.toAbsolutePath().normalize());
        List<ClassInfo> classes = classInfoMap.get(normalize(filePath));
        for (ClassInfo classInfo : classes) {
            srcClasses.remove(classInfo.getClassName());
        }
        classGraphBuilder.onFileDelete(filePath);
        classInfoMap.remove(normalize(filePath));
    }


    //on file update
    public void onFileModify(Path filePath) {
        Path p = normalize(filePath);
        cache.invalidate(p);
        //  remove old classes
        List<ClassInfo> oldClasses = classInfoMap.getOrDefault(p, List.of());
        for (ClassInfo ci : oldClasses) {
            srcClasses.remove(ci.getClassName());
        }
// just finding new class names
        CompilationUnit cu = cache.parse(p);
        cu.findAll(TypeDeclaration.class).forEach(td ->
                srcClasses.add(ClassNameResolver.resolveFqn(cu, td))
        );
        // update dependency graph
        classGraphBuilder.onFileModify(filePath, srcClasses);

        //  re-scan file
        List<ClassInfo> newClasses = getClassInfos(filePath);




        //  replace class info
        classInfoMap.put(p, newClasses);
    }


    public void invalidateFile(Path filePath) {
        cache.invalidate(filePath.toAbsolutePath().normalize());
    }





}
