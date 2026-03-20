package com.example.bodhakfrontend.Backend.Analysis.Engine;

import com.example.bodhakfrontend.Backend.Factory.ClassInfoBuilderFactory;
import com.example.bodhakfrontend.Backend.Factory.ClassNameExtractorFactory;
import com.example.bodhakfrontend.Backend.Factory.ParserFactory;
import com.example.bodhakfrontend.Backend.interfaces.ClassInfoBuilder;
import com.example.bodhakfrontend.Backend.interfaces.ClassNameExtractor;
import com.example.bodhakfrontend.Backend.ClassGraphBuilder;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder.PackageInfoBuilder;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder.ProjectInfoBuilder;
import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Package.PackageInfo;
import com.example.bodhakfrontend.Backend.models.Project.ProjectInfo;
import com.example.bodhakfrontend.LanguageDetector;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class AnalysisEngine {
    ProjectInfoBuilder projectInfoBuilder= new ProjectInfoBuilder();
    private final LanguageDetector languageDetector=new LanguageDetector();
    private final PackageInfoBuilder packageInfoBuilder=new PackageInfoBuilder();
    //states
    private final Map<Path,List<ClassInfo>> classInfoToPathMap=new HashMap<>();
    private final List<ClassInfo> classInfos=new ArrayList<>();
    private final Map<Path,Set<String>> sourceClasses=new HashMap<>();



    private final ParserFactory parserFactory;
    private final ClassGraphBuilder classGraphBuilder;
    private final ClassNameExtractorFactory classNameExtractorFactory;
    private ClassNameExtractor classNameExtractor;
    private final ClassInfoBuilderFactory classInfoBuilderFactory;
    private ClassInfoBuilder classInfoBuilder;

    public AnalysisEngine(ParserFactory parserFactory,ClassGraphBuilder classGraphBuilder) {
        this.parserFactory=parserFactory;
        this.classGraphBuilder=classGraphBuilder;
        this.classNameExtractorFactory=new ClassNameExtractorFactory(parserFactory);
        classInfoBuilderFactory=new ClassInfoBuilderFactory(parserFactory,classGraphBuilder);

    }
    public void analyse(Path projectPath) {



        // find all classes
        try {
            Files.walk(projectPath).filter(Files::isRegularFile).forEach(path -> {
                getClassName(path);

            });

        }catch (Exception e){}


        // first add all dependencies
        classGraphBuilder.buildDependsOnGraph(projectPath,getClassNameList());

        try {
            Files.walk(projectPath).filter(Files::isRegularFile).forEach(path -> {
                String fileLanguage= languageDetector.detectFileType(path.toFile()).toLowerCase();
                if(!fileLanguage.equals("java")) return;
                classInfoBuilder=classInfoBuilderFactory.getBuilder(fileLanguage);
                List<ClassInfo> classes=classInfoBuilder.getClassInfos(path);
                classInfos.addAll(classes);
                // update the class->lang map
                classInfoToPathMap.computeIfAbsent(getNormalizedPath(path),newLan -> new ArrayList<>()).addAll(classes);
            });
        } catch (Exception e) {
            e.printStackTrace();

        }

        // get the package dependencies

        packageInfoBuilder.buildAll(classInfos);
        Map<String,PackageInfo> packageInfos=packageInfoBuilder.getPackageMap();



        projectInfoBuilder.buildAll(projectPath,classInfos,packageInfos);



        }



        private void getClassName(Path path){
            String lang=languageDetector.detectFileType(path.toFile()).toLowerCase();
            classNameExtractor=classNameExtractorFactory.getClassNameExtractor(lang);
            Set<String> className=classNameExtractor.getClassNames(path);
            sourceClasses.computeIfAbsent(getNormalizedPath(path),newPath-> new HashSet<>()).addAll(className);
        }
        private Path getNormalizedPath(Path path){
        return path.toAbsolutePath().normalize();
        }

        private Set<String> getClassNameList(){
        Set<String> classNameList=new HashSet<>();
        for(Path filePath:sourceClasses.keySet()){
            classNameList.addAll(sourceClasses.get(filePath));
        }
        return classNameList;
        }

        public Map<Path, List<ClassInfo>> getClassInfoToPathMap() {return classInfoToPathMap;}

        public void onFileCreate(Path filePath){
        String lang=languageDetector.detectFileType(filePath.toFile()).toLowerCase();
        if(lang.equals("unknown")) return;
        classInfoBuilder=classInfoBuilderFactory.getBuilder(lang);
         getClassName(filePath);
         classGraphBuilder.onFileCreate(filePath,getClassNameList());
         List<ClassInfo> createdClasses=classInfoBuilder.getClassInfos(filePath);
         classInfos.addAll(createdClasses);
         classInfoToPathMap.computeIfAbsent(getNormalizedPath(filePath),newPath -> new ArrayList<>()).addAll(createdClasses);
         packageInfoBuilder.onFileCreate(classInfos);
         projectInfoBuilder.onFileCreated(filePath,createdClasses);
        }

        public void onFileDelete(Path filePath){
        sourceClasses.remove(getNormalizedPath(filePath));
        List<ClassInfo> removedClasses=classInfoToPathMap.get(getNormalizedPath(filePath));
        classInfos.removeAll(removedClasses);
        classInfoToPathMap.remove(getNormalizedPath(filePath));
        packageInfoBuilder.onFileDelete(removedClasses, classInfos);
        projectInfoBuilder.onFileDelete(filePath);
        }

        public void onFileModify(Path filePath){
        onFileDelete(filePath);
        onFileCreate(filePath);
        }

        public void onFolderCreate(Path folderPath){
        projectInfoBuilder.onFolderCreated(folderPath);
        }
        public void onFolderDelete(Path folderPath){
        projectInfoBuilder.onFolderDeleted(folderPath);
        }


        public ProjectInfo getProjectInfo(){return projectInfoBuilder.getProjectInfo();}





}
