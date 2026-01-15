package com.example.bodhakfrontend.IncrementalPart.Builder;
import com.example.bodhakfrontend.IncrementalPart.Analyzer.EntryClassAnalyzer;
import com.example.bodhakfrontend.IncrementalPart.Analyzer.HotspotAnalyzer;
import com.example.bodhakfrontend.IncrementalPart.Analyzer.UnusedClassAnalyzer;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.IssueType;
import com.example.bodhakfrontend.IncrementalPart.model.Package.PackageInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.EntryPointInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.Hotspots;
import com.example.bodhakfrontend.IncrementalPart.model.Project.ProjectInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.UnusedClassInfo;
import com.example.bodhakfrontend.LanguageDetector;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProjectInfoBuilder {
    LanguageDetector languageDetector=new LanguageDetector();
    private final HotspotAnalyzer hotspotAnalyzer=new HotspotAnalyzer();
    private final EntryClassAnalyzer entryClassAnalyzer=new  EntryClassAnalyzer();
    private final UnusedClassAnalyzer  unusedClassAnalyzer=new UnusedClassAnalyzer();

    private final ClassInfoBuilder classInfoToPathMapBuilder;
    private final PackageInfoBuilder packageInfoBuilder;

    public ProjectInfoBuilder(ClassInfoBuilder classInfoToPathMapBuilder, PackageInfoBuilder packageInfoBuilder) {
        this.classInfoToPathMapBuilder = classInfoToPathMapBuilder;
        this.packageInfoBuilder = packageInfoBuilder;
    }

    public ProjectInfo getPackageInfo(Path projectPath) {

        //language counter map
        Map<String, Set<Path>> laguageCountMap=new HashMap<>();
        // largest Files
        List<ProjectInfo.LargestFileInfo> largestFiles=new ArrayList<>();

        // Folders in Project
        Set<Path> knownFolders=new HashSet<>();
        // files
        Set<Path> knownFiles=new HashSet<>();

        //Classinfos
        List<ClassInfo> classInfos=classInfoToPathMapBuilder.getListOfClassInfo();
        //package info
        List<PackageInfo> packageInfos= packageInfoBuilder.build(classInfos);
        // total classes
        int totalClasses=classInfos.size();
        //total healty classes
        int healthyClasses=0;
        int warningClasses=0;
        int godClasses=0;
        int circularClasses=0;
        int highlyCoupledClasses=0;

        for(ClassInfo classInfo:classInfos){
            Set<IssueType> issues=classInfo.getIssueType();
            if(issues.isEmpty()) healthyClasses++;
            else warningClasses++;
            if(issues.contains(IssueType.GOD_CLASS))godClasses++;
            if(issues.contains(IssueType.CIRCULAR_DEPENDENCY))circularClasses++;
            if (issues.contains(IssueType.HIGH_COUPLING))highlyCoupledClasses++;
        }

        // get List of Hotspot
        List<Hotspots>  hotspots=hotspotAnalyzer.analyze(classInfos);

        //Entry point

        EntryPointInfo entryPointInfo=entryClassAnalyzer.build(classInfos);

        //unused Class

        Set<UnusedClassInfo> unusedClassInfos=unusedClassAnalyzer.analyze(classInfos,entryPointInfo, UnusedClassAnalyzer.Mode.DEV);


       try {
           Files.walk(projectPath)
                   .forEach(path -> {
                       Path normalizedPath=path.toAbsolutePath().normalize();
                       File f=normalizedPath.toFile();

                       try {

                           //Largest files
                           int loc = (int) Files.lines(normalizedPath)
                                   .map(String::trim)
                                   .filter(this::isCodeLine)
                                   .count();
                           largestFiles.add(new ProjectInfo.LargestFileInfo(normalizedPath.getFileName().toString(),normalizedPath.toFile(),loc));

                           // count folders and Files
                           if(f.isDirectory()){
                               knownFolders.add(normalizedPath);
                           }
                           else if(f.isFile()){
                               knownFiles.add(normalizedPath);
                               //detect language
                               String language=languageDetector.detectFileType(normalizedPath.toFile());
                               laguageCountMap.computeIfAbsent(language,k->new HashSet<>()).add(normalizedPath);

                           }



                       } catch (IOException e) {
                           e.printStackTrace();
                       }


                   });
       } catch (Exception e) {
           e.printStackTrace();
       }
        // top 5 largest Files
        List<ProjectInfo.LargestFileInfo> top5 =
                largestFiles.stream()
                        .sorted(
                                Comparator.comparingInt(
                                        ProjectInfo.LargestFileInfo::getLoc
                                ).reversed()
                        )
                        .limit(5)
                        .toList();






       return new ProjectInfo(laguageCountMap,top5,knownFolders,knownFiles,classInfos,packageInfos,
               hotspots,entryPointInfo,unusedClassInfos,totalClasses,healthyClasses,warningClasses,godClasses,circularClasses,highlyCoupledClasses);


    }
    private boolean isCodeLine(String line) {
        return !line.isEmpty()
                && !line.startsWith("//")
                && !line.startsWith("/*")
                && !line.startsWith("*")
                && !line.startsWith("*/");
    }






}
