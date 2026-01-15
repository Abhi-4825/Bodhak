package com.example.bodhakfrontend.projectAnalysis;

import com.example.bodhakfrontend.LanguageDetector;
import com.example.bodhakfrontend.Models.PackageAnalysis.EntryPointInfo;
import com.example.bodhakfrontend.Models.PackageAnalysis.FileEntryContribution;
import com.example.bodhakfrontend.Models.PackageAnalysis.PackageAnalysisInfo;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.util.ParseCache;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class FileAnalyzer {

    private final LanguageDetector languageDetector;
    private  final JavaFileParser parser;
    private final ParseCache cache;

    public FileAnalyzer(LanguageDetector languageDetector, JavaFileParser parser, ParseCache cache) {
        this.languageDetector = languageDetector;
        this.parser = parser;
        this.cache = cache;
    }

    /* =========================================================
       PHASE A — PROJECT STRUCTURE OVERVIEW
       ========================================================= */

    public PackageAnalysisInfo buildPackageAnalysisInfo(Path rootFolder) {

        Set<Path> folders=new HashSet<>();
        Set<Path> files=new HashSet<>();
        Map<String, Set<Path>> languageCount = new HashMap<>();

        try {
            Files.walk(rootFolder)
                    .filter(this::isRelevantPath)
                    .forEach(path -> {
                        File file = path.toFile();

                        if (file.isDirectory()) {
                            folders.add(path.toAbsolutePath().normalize());
                        } else if (file.isFile()) {
                            files.add(path.toAbsolutePath().normalize());

                            String language =
                                    languageDetector.detectFileType(file);

                            languageCount
                                    .computeIfAbsent(language, k -> new HashSet<>())
                                    .add(path.toAbsolutePath().normalize());
                        }
                    });

        } catch (Exception e) {
            throw new RuntimeException("Failed to analyze project structure", e);
        }

        return new PackageAnalysisInfo(
                folders,
                files,
                languageCount,
                analyzeEntryPoints(rootFolder),
                analyzeLargestFiles(rootFolder)
        );
    }

    /* =========================================================
       ENTRY POINT ANALYSIS
       ========================================================= */
    private Map<Path,FileEntryContribution> analyzeEntryPoints(Path projectRoot) {

       Map<Path,FileEntryContribution> map = new HashMap<>();


        try {
            Files.walk(projectRoot)
                    .filter(this::isJavaFile)
                    .forEach(path -> {
                        try {
                            FileEntryContribution contribution=analyzeEntryContribution(path);
                            if (contribution != null) {
                                map.put(
                                        path.toAbsolutePath().normalize(),
                                        contribution
                                );
                            }
                        } catch (Exception ignored) {}
                    });

        } catch (Exception ignored) {}

      return map;
    }



public FileEntryContribution analyzeEntryContribution(Path javaFile){
       CompilationUnit cu = cache.get(javaFile);
       if (cu == null) {return null;}
       FileEntryContribution c = new FileEntryContribution();
       for (ClassOrInterfaceDeclaration cls : cu.findAll(ClassOrInterfaceDeclaration.class)) {
           String name=cls.getNameAsString();
           c.addClassName(name);
           /* ---------- SPRING BOOT ---------- */
           boolean isSpringBoot =
                   cls.getAnnotations()
                           .stream()
                           .anyMatch(a ->
                                   a.getNameAsString()
                                           .equals("SpringBootApplication")
                           );
           if(isSpringBoot){
               c.setHasSpringBoot(true);
           }  /* ---------- JAVAFX ---------- */
           boolean isJavaFx =
                   cls.getExtendedTypes()
                           .stream()
                           .anyMatch(t ->
                                   t.getNameAsString()
                                           .equals("Application")
                           );
           if(isJavaFx){
               c.setHasJavaFx(true);
           }
           /* ---------- MAIN METHOD ---------- */
           boolean hasMain =
                   cls.getMethodsByName("main")
                           .stream()
                           .anyMatch(m ->
                                   m.isPublic() && m.isStatic()
                           );

           if(hasMain){
               c.setHasMain(true);
           }
           if(!c.hasMain() && !c.hasSpringBoot()&& c.hasJavaFx()){
               return null;
           }
       }


    return c;

}




    /* =========================================================
       LARGEST FILES (LOC)
       ========================================================= */

    private List<PackageAnalysisInfo.LargestFileInfo> analyzeLargestFiles(Path root) {

      List<PackageAnalysisInfo.LargestFileInfo> largestFiles = new ArrayList<>();

        try {
            Files.walk(root)
                    .filter(this::isJavaFile)
                    .forEach(p -> {
                        try {
                            int loc = (int) Files.lines(p)
                                    .map(String::trim)
                                    .filter(this::isCodeLine)
                                    .count();
                           largestFiles.add(new PackageAnalysisInfo.LargestFileInfo(p.getFileName().toString(),p.toFile(),loc));
                        } catch (Exception ignored) {}
                    });

        } catch (Exception ignored) {}

        List<PackageAnalysisInfo.LargestFileInfo> top5 =
                largestFiles.stream()
                        .sorted(
                                Comparator.comparingInt(
                                        PackageAnalysisInfo.LargestFileInfo::getLoc
                                ).reversed()
                        )
                        .limit(5)
                        .toList();
        return top5;
    }

    /* =========================================================
       HELPERS
       ========================================================= */

    private boolean isRelevantPath(Path path) {
        String p = path.toString();
        return !p.contains("target")
                && !p.contains(".git")
                && !p.contains(".idea");
    }

    private boolean isJavaFile(Path path) {
        return isRelevantPath(path)
                && path.toString().endsWith(".java");
    }

    private boolean isCodeLine(String line) {
        return !line.isEmpty()
                && !line.startsWith("//")
                && !line.startsWith("/*")
                && !line.startsWith("*")
                && !line.startsWith("*/");
    }

    public class EntryState {
        EntryPointInfo.ProjectType projectType =
                EntryPointInfo.ProjectType.PLAIN_JAVA;
        String primary = null;
        Set<String> secondary = new LinkedHashSet<>();
    }




}
