package com.example.bodhakfrontend.Models.PackageAnalysis;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.Models.*;
import java.util.List;
import java.util.Map;

public class ProjectAnalysisResult {

   private final PackageAnalysisInfo packageAnalysisInfo;
   private final Map<String, PackageInfo> packageInfoMap;
   private final List<ClassInfo> classInfos;
    private final Map<String, ClassHealthInfo> classHealthInfoMap;
    private final ProjectHealthSummary projectHealthSummary;
    private final List<HotspotInfo> hotspotInfos;
    private final List<UnusedClassInfo> unusedClassInfos;

    public ProjectAnalysisResult(PackageAnalysisInfo packageAnalysisInfo, Map<String, PackageInfo> packageInfoMap, List<ClassInfo> classInfos, Map<String, ClassHealthInfo> classHealthInfoMap, List<HotspotInfo> hotspotInfos, List<UnusedClassInfo> unusedClassInfos, ProjectHealthSummary projectHealthSummary) {
        this.packageAnalysisInfo = packageAnalysisInfo;
        this.packageInfoMap = packageInfoMap;
        this.classInfos = classInfos;
        this.classHealthInfoMap = classHealthInfoMap;
        this.hotspotInfos = hotspotInfos;
        this.unusedClassInfos = unusedClassInfos;
        this.projectHealthSummary = projectHealthSummary;
    }

    public PackageAnalysisInfo getPackageAnalysisInfo() {
        return packageAnalysisInfo;
    }

    public Map<String, PackageInfo> getPackageInfoMap() {
        return packageInfoMap;
    }

    public List<ClassInfo> getClassInfos() {
        return classInfos;
    }

    public Map<String, ClassHealthInfo> getClassHealthInfoMap() {
        return classHealthInfoMap;
    }

    public List<HotspotInfo> getHotspotInfos() {
        return hotspotInfos;
    }

    public List<UnusedClassInfo> getUnusedClassInfos() {
        return unusedClassInfos;
    }
    public ProjectHealthSummary getProjectHealthSummary() {
           return projectHealthSummary;
    }










}



