package com.example.bodhakfrontend.projectAnalysis;

import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
import com.example.bodhakfrontend.dependency.PackageDependency;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class ProjectAnalysisResultBuilder {
    FileAnalyzer fileAnalyzer=new FileAnalyzer();
    ClassHealthAnalyzer analyzer=new ClassHealthAnalyzer();
    PackageDependency pkgDpn=new PackageDependency();
    JavaFileParser javaFileParser=new JavaFileParser();
    ProjectHealthAnalyzer projectHealthAnalyzer=new ProjectHealthAnalyzer();
    HotspotAnalyzer hotspotAnalyzer=new HotspotAnalyzer();
    UnusedClassAnalyzer unusedClassAnalyzer=new UnusedClassAnalyzer();
    public ProjectAnalysisResult build(Path projectFolder,ClassDependencyInfo classDependencyInfo){
        PackageAnalysisInfo packageAnalysisInfo=fileAnalyzer.buildPackageAnalysisInfo(projectFolder);
        Map<String, PackageInfo> packageInfoMap=pkgDpn.buildPackageInfo(classDependencyInfo);
        List<ClassInfo> classInfos=javaFileParser.extractClassInfo(projectFolder);
        Map<String, ClassHealthInfo> classHealthInfoMap=analyzer.analyze(classInfos,classDependencyInfo);
        List<HotspotInfo> hotspotInfos=hotspotAnalyzer.analyze(classInfos,classHealthInfoMap,classDependencyInfo);
        List<UnusedClassInfo> unusedClassInfos=unusedClassAnalyzer.analyze(classInfos,classDependencyInfo,packageAnalysisInfo.getEntryPointInfo());
        ProjectHealthSummary projectHealthSummary=projectHealthAnalyzer.summarize(classHealthInfoMap);
        return new ProjectAnalysisResult(packageAnalysisInfo,packageInfoMap,classInfos,classHealthInfoMap,hotspotInfos,unusedClassInfos,projectHealthSummary);
    }




}
