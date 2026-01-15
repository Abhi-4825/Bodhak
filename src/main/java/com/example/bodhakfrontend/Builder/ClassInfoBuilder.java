//package com.example.bodhakfrontend.Builder;
//
//import com.example.bodhakfrontend.Models.ClassInfo;
//import com.example.bodhakfrontend.Models.ClassKind;
//import com.example.bodhakfrontend.util.ParseCache;
//import com.github.javaparser.ast.CompilationUnit;
//import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
//import com.github.javaparser.ast.body.EnumDeclaration;
//import com.github.javaparser.ast.body.RecordDeclaration;
//import com.github.javaparser.ast.expr.AnnotationExpr;
//
//import java.io.File;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.util.*;
//import java.util.concurrent.ConcurrentHashMap;
//
//public class ClassInfoBuilder {
//    private final ParseCache cache;
//
//    // 🔥 Index: file → classes in that file
//    private final Map<Path, List<ClassInfo>> fileIndex =
//            new ConcurrentHashMap<>();
//
//    public ClassInfoBuilder(ParseCache cache) {
//        this.cache = cache;
//    }
//
//
//
//    /* =====================================================
//       INITIAL FULL BUILD
//       ===================================================== */
//
//    public List<ClassInfo> buildFull(Path sourceRoot) {
//        fileIndex.clear();
//
//        try {
//            Files.walk(sourceRoot)
//                    .filter(p -> p.toString().endsWith(".java"))
//                    .forEach(this::scanAndIndexFile);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return getAllClasses();
//    }
//
//    /* =====================================================
//       INCREMENTAL OPERATIONS
//       ===================================================== */
//
//    public void onFileCreated(Path file) {
//        if (!isJava(file)) return;
//        scanAndIndexFile(file);
//    }
//
//    public void onFileModified(Path file) {
//        if (!isJava(file)) return;
//        scanAndIndexFile(file); // overwrite old entry
//    }
//
//    public void onFileDeleted(Path file) {
//        fileIndex.remove(file);
//    }
//
//    /* =====================================================
//       QUERY
//       ===================================================== */
//
//    public List<ClassInfo> getAllClasses() {
//        return fileIndex.values()
//                .stream()
//                .flatMap(List::stream)
//                .toList();
//    }
//
//    public List<ClassInfo> getClassesForFile(Path file) {
//        return fileIndex.getOrDefault(file, List.of());
//    }
//
//    /* =====================================================
//       INTERNAL LOGIC
//       ===================================================== */
//
//    private boolean isJava(Path p) {
//        return p.toString().endsWith(".java");
//    }
//
//    private void scanAndIndexFile(Path path) {
//        try {
//            CompilationUnit cu = cache.get(path);
//            if (cu == null) {
//                fileIndex.remove(path);
//                return;
//            }
//
//            List<ClassInfo> classes = extractClasses(cu, path);
//            fileIndex.put(path, classes);
//
//        } catch (Exception e) {
//            System.err.println("Failed to parse: " + path);
//            e.printStackTrace();
//        }
//    }
//
//    private List<ClassInfo> extractClasses(
//            CompilationUnit cu,
//            Path path
//    ) {
//        List<ClassInfo> result = new ArrayList<>();
//
//        String pkg = cu.getPackageDeclaration()
//                .map(p -> p.getNameAsString())
//                .orElse("default");
//
//        File sourceFile = path.toFile();
//
//        // ---------- Classes & Interfaces ----------
//        cu.findAll(ClassOrInterfaceDeclaration.class)
//                .forEach(c -> {
//                    var range = c.getName().getRange().orElse(null);
//                    if (range == null) return;
//
//                    result.add(
//                            buildClassInfo(
//                                    c.getNameAsString(),
//                                    pkg,
//                                    c.isInterface()
//                                            ? ClassKind.INTERFACE
//                                            : ClassKind.CLASS,
//                                    c.getMethods().size(),
//                                    c.getFields().size(),
//                                    c.getConstructors().size(),
//                                    c.getAnnotations(),
//                                    c.isAbstract(),
//                                    c.isFinal(),
//                                    c.isPublic(),
//                                    c.getRange().get(),
//                                    sourceFile
//                            )
//                    );
//                });
//
//        // ---------- Enums ----------
//        cu.findAll(EnumDeclaration.class)
//                .forEach(e -> {
//                    var range = e.getName().getRange().orElse(null);
//                    if (range == null) return;
//
//                    result.add(
//                            buildClassInfo(
//                                    e.getNameAsString(),
//                                    pkg,
//                                    ClassKind.ENUM,
//                                    e.getMethods().size(),
//                                    e.getFields().size(),
//                                    0,
//                                    e.getAnnotations(),
//                                    false,
//                                    false,
//                                    e.isPublic(),
//                                    e.getRange().get(),
//                                    sourceFile
//                            )
//                    );
//                });
//
//        // ---------- Records ----------
//        cu.findAll(RecordDeclaration.class)
//                .forEach(r -> {
//                    var range = r.getName().getRange().orElse(null);
//                    if (range == null) return;
//
//                    result.add(
//                            buildClassInfo(
//                                    r.getNameAsString(),
//                                    pkg,
//                                    ClassKind.RECORD,
//                                    r.getMethods().size(),
//                                    r.getFields().size(),
//                                    r.getConstructors().size(),
//                                    r.getAnnotations(),
//                                    false,
//                                    r.isFinal(),
//                                    r.isPublic(),
//                                    r.getRange().get(),
//                                    sourceFile
//                            )
//                    );
//                });
//
//        return result;
//    }
//
//    private ClassInfo buildClassInfo(
//            String name,
//            String pkg,
//            ClassKind kind,
//            int methodCount,
//            int fieldCount,
//            int constructorCount,
//            List<AnnotationExpr> annotations,
//            boolean isAbstract,
//            boolean isFinal,
//            boolean isPublic,
//            com.github.javaparser.Range range,
//            File sourceFile
//    ) {
//        Set<String> ann = new HashSet<>();
//        annotations.forEach(a ->
//                ann.add(a.getName().getIdentifier())
//        );
//
//        int lines =
//                range.end.line - range.begin.line + 1;
//
//        return new ClassInfo(
//                name,
//                pkg,
//                kind,
//                methodCount,
//                fieldCount,
//                constructorCount,
//                ann,
//                isAbstract,
//                isFinal,
//                isPublic,
//                lines,
//                sourceFile,
//                range.begin.line,
//                range.begin.column
//        );
//    }
//
//
//
//
//}
