package com.example.bodhakfrontend.Backend.models.Project;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Package.PackageInfo;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class ProjectInfo {
    public static class LargestFileInfo {
        String name;
        File sourceFile;
        int loc;

        public LargestFileInfo(String name, File sourceFile, int loc) {
            this.name = name;
            this.loc = loc;
            this.sourceFile = sourceFile;
        }

        public String getName() {
            return name;
        }

        public int getLoc() {
            return loc;
        }

        public File getSourceFile() {
            return sourceFile;
        }
    }


    private final Map<String, Set<Path>> laguageCountMap;
    private final List<LargestFileInfo> largetFiles;

    private final Set<Path> knownFolders;
    private final Set<Path> knownFiles;
    private final Map<String,PackageInfo> packageInfos;
    private final List<ClassInfo> classInfos;

    //sates that can be found though classInfos
    private final int totalClasses;
    private final int healthyClasses;
    private final int classesWithWarnings;
    private final int godClasses;
    private final int circularClasses;
    private final int highlyCoupledClasses;
    private final List<Hotspots> hotspotClasses;
    // Entry Point
    private final EntryPointInfo entryPointInfo;
    //UnusedClass
    private final Set<UnusedClassInfo> unusedClassInfos;
    public ProjectInfo(Map<String, Set<Path>> laguageCountMap, List<LargestFileInfo> top5, Set<Path> knownFolders, Set<Path> knownFiles, List<ClassInfo> classInfos, Map<String,PackageInfo> packageInfos, List<Hotspots> hotspots, EntryPointInfo entryPointInfo, Set<UnusedClassInfo> unusedClassInfos
            ,int totalClasses,int healthyClasses,int classesWithWarnings,int godClasses,int circularClasses,int highlyCoupledClasses ) {
        this.laguageCountMap = laguageCountMap;
        this.largetFiles = top5;
        this.knownFolders = knownFolders;
        this.knownFiles = knownFiles;
        this.classInfos = classInfos;
        this.packageInfos = packageInfos;
        this.hotspotClasses = hotspots;
        this.entryPointInfo = entryPointInfo;
        this.unusedClassInfos = unusedClassInfos;
        this.totalClasses = totalClasses;
        this.healthyClasses = healthyClasses;
        this.classesWithWarnings = classesWithWarnings;
        this.circularClasses = circularClasses;
        this.highlyCoupledClasses = highlyCoupledClasses;
        this.godClasses=godClasses;
    }
    public Map<String, Set<Path>> getLaguageCountMap() {
        return laguageCountMap;
    }
    public List<LargestFileInfo> getLargetFiles() {
        return largetFiles;
    }
    public Set<Path> getKnownFolders() {
        return knownFolders;
    }
    public Set<Path> getKnownFiles() {
        return knownFiles;
    }
    public Map<String,PackageInfo> getPackageInfos() {
        return packageInfos;
    }
    public List<ClassInfo> getClassInfos() {
        return classInfos;
    }
    public int getTotalClasses() {
        return totalClasses;
    }
    public int getHealthyClasses() {
        return healthyClasses;
    }
    public int getClassesWithWarnings() {
        return classesWithWarnings;
    }
    public int getGodClasses() {
        return godClasses;
    }
    public int getCircularClasses() {
        return circularClasses;
    }
    public int getHighlyCoupledClasses() {
        return highlyCoupledClasses;
    }
    public List<Hotspots> getHotspotClasses() {
        return hotspotClasses;
    }
    public EntryPointInfo getEntryPointInfo() {
        return entryPointInfo;
    }
    public Set<UnusedClassInfo> getUnusedClassInfos() {
        return unusedClassInfos;
    }
    public List<ClassInfo> getClassesInCycles(){
        List<ClassInfo> classesInCycles = new ArrayList<>();
        for(ClassInfo classInfo : classInfos){
            if(!classInfo.getCircularDependencyGroups().isEmpty()){
                classesInCycles.add(classInfo);
            }
        }
        return classesInCycles;
    }
    public Map<String,ClassInfo> getClassInfoMap(){
        Map<String,ClassInfo> classInfoMap =new HashMap<>();
        for(ClassInfo classInfo : classInfos){
            classInfoMap.put(classInfo.getClassName(), classInfo);
        }
        return classInfoMap;
    }
}
