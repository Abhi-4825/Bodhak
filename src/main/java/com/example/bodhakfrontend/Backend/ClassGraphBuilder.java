package com.example.bodhakfrontend.Backend;

import com.example.bodhakfrontend.Backend.Factory.ClassDependsOnFactory;
import com.example.bodhakfrontend.Backend.Factory.ParserFactory;
import com.example.bodhakfrontend.Backend.dependency.CircularDependency;
import com.example.bodhakfrontend.Backend.interfaces.ClassDependenciesBuilder;
import com.example.bodhakfrontend.Backend.interfaces.Parser;
import com.example.bodhakfrontend.LanguageDetector;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ClassGraphBuilder {
    private final ParserFactory parserFactory;
    private  ClassDependsOnFactory classDependsOnFactory;
    private ClassDependenciesBuilder classDependenciesBuilder;
    private final LanguageDetector languageDetector=new LanguageDetector();
    private final Map<String,Set<String>> reverseClassDependencies =new ConcurrentHashMap<>();
    private final Map<Path,Map<String,Set<String>>> classDependencies=new HashMap<>();
    Map<String,Set<String>> classDependenciesGroups =new ConcurrentHashMap<>();

    public ClassGraphBuilder(ParserFactory parserFactory) {
       this. parserFactory = parserFactory;
        classDependsOnFactory=new ClassDependsOnFactory(parserFactory);
    }


    public Map<Path,Map<String, Set<String>>> buildDependsOnGraph (Path projectPath,Set<String> sourceClasses){
        classDependencies.clear();
        try {
            Files.walk(projectPath).filter(Files::isRegularFile).forEach(path -> {

                getDependesOnMap(path,sourceClasses);

            });


        }catch (Exception e){
            System.out.println("Error reading project path"+e.getMessage());;
        }

        getAffectedClassDependencies();
        return classDependencies;

    }
    private void getDependesOnMap(Path path,Set<String> sourceClasses){
        Path normalizedPath = path.toAbsolutePath().normalize();
        String language=languageDetector.detectFileType(path.toFile()).toLowerCase();
        if(!language.equals("java")){return;}

        classDependenciesBuilder=classDependsOnFactory.getBuilder(language);
        Map<String, Set<String>> dependsOnMap= classDependenciesBuilder.getDependencies(path,sourceClasses);
        classDependencies.computeIfAbsent(normalizedPath,key -> new ConcurrentHashMap<>()).putAll(dependsOnMap);
    }



    public Map<String, Set<String>> getAffectedClassDependencies(
          ) {

        reverseClassDependencies.clear();

        classDependencies.forEach((path, classMap) -> {

            classMap.forEach((fromClass, dependsOnSet) -> {

                for (String toClass : dependsOnSet) {

                    reverseClassDependencies
                            .computeIfAbsent(toClass, k -> ConcurrentHashMap.newKeySet())
                            .add(fromClass);
                }
            });
        });

        return reverseClassDependencies;
    }





    public Path normalize(Path filePath) {
        return filePath.toAbsolutePath().normalize();
    }
    // update the classDependency on File create
    public void onFileCreate(Path filePath,Set<String> sourceClasses){
        getDependesOnMap(filePath,sourceClasses);
        getAffectedClassDependencies();
    }

    // update classDependency on File modify
    public void onFileModify(Path filePath,Set<String> sourceClasses){

        getDependesOnMap(filePath,sourceClasses);
        getAffectedClassDependencies();
    }

    // update on file Delete
    public void onFileDelete(Path filePath){
        Path normalizedPath = normalize(filePath);
        classDependencies.remove(normalizedPath);
        getAffectedClassDependencies();
    }
    // get the maps
    public Map<Path,Map<String, Set<String>>> getClassDependenciesToPath() {
        return classDependencies;
    }
    public Map<String, Set<String>> getReverseClassDependencies() {
        return reverseClassDependencies;
    }

    public Map<String, Set<String>> getDependsOn() {
        Map<String, Set<String>> dependsOn = new HashMap<>();
        for (Map<String, Set<String>> perFileDeps : classDependencies.values()) {
            for (Map.Entry<String, Set<String>> entry : perFileDeps.entrySet()) {
                String className = entry.getKey();
                Set<String> deps = entry.getValue();
                dependsOn
                        .computeIfAbsent(className, k -> new HashSet<>())
                        .addAll(deps);
            }
        }
        return dependsOn;
    }


    // Circular - dependency group
    CircularDependency circularDependency=new CircularDependency();
    // first get the required Map

    private Set<Set<String>> updateClassDependencies(){
        classDependenciesGroups.clear();
        Map<Path,Map<String, Set<String>>>perFileDeps = getClassDependenciesToPath();
        for(Map.Entry<Path,Map<String, Set<String>>> entry:perFileDeps.entrySet()){
            for (Map.Entry<String, Set<String>> e : entry.getValue().entrySet()) {
                classDependenciesGroups
                        .computeIfAbsent(e.getKey(), k -> new HashSet<>())
                        .addAll(e.getValue());
            }}
        return circularDependency.findCircularDependency(classDependenciesGroups);
    }
    // get Cirulardependencies graph
    public Set<Set<String>> getClassDependenciesGroups() {
        return updateClassDependencies();
    }

}
