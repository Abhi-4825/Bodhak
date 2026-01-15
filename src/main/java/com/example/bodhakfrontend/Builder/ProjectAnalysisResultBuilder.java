//package com.example.bodhakfrontend.Builder;
//
//import com.example.bodhakfrontend.Models.*;
//import com.example.bodhakfrontend.Models.Incremental.FileChangeEvent;
//import com.example.bodhakfrontend.Models.PackageAnalysis.PackageAnalysisInfo;
//import com.example.bodhakfrontend.Models.PackageAnalysis.PackageInfo;
//import com.example.bodhakfrontend.Models.PackageAnalysis.ProjectAnalysisResult;
//import com.example.bodhakfrontend.Parser.javaParser.JavaFileParser;
//import com.example.bodhakfrontend.dependency.PackageDependency;
//import com.example.bodhakfrontend.projectAnalysis.*;
//
//import java.nio.file.Path;
//import java.util.List;
//import java.util.Map;
//
//public class ProjectAnalysisResultBuilder {
//
//   private final FileAnalyzer fileAnalyzer;
//   private final ClassHealthAnalyzer analyzer;
//   private final PackageDependency pkgDpn;
//   private final ClassInfoBuilder classInfoBuilder;
//   private final ProjectHealthAnalyzer projectHealthAnalyzer;
//   private final HotspotAnalyzer hotspotAnalyzer;
//   private final UnusedClassAnalyzer unusedClassAnalyzer;
//   private PackageAnalysisInfo packageAnalysisInfo;
//
//    public ProjectAnalysisResultBuilder(FileAnalyzer fileAnalyzer, ClassHealthAnalyzer analyzer, PackageDependency pkgDpn, ClassInfoBuilder classInfoBuilder, ProjectHealthAnalyzer projectHealthAnalyzer, HotspotAnalyzer hotspotAnalyzer, UnusedClassAnalyzer unusedClassAnalyzer) {
//        this.fileAnalyzer = fileAnalyzer;
//        this.analyzer = analyzer;
//        this.pkgDpn = pkgDpn;
//        this.classInfoBuilder = classInfoBuilder;
//        this.projectHealthAnalyzer = projectHealthAnalyzer;
//        this.hotspotAnalyzer = hotspotAnalyzer;
//        this.unusedClassAnalyzer = unusedClassAnalyzer;
//    }
//
//    public ProjectAnalysisResult build(Path projectFolder, ClassDependencyInfo classDependencyInfo){
//        packageAnalysisInfo=fileAnalyzer.buildPackageAnalysisInfo(projectFolder);
//        Map<String, PackageInfo> packageInfoMap=pkgDpn.buildPackageInfo(classDependencyInfo);
//        List<ClassInfo> classInfos=classInfoBuilder.buildFull(projectFolder);
//        Map<String, ClassHealthInfo> classHealthInfoMap=analyzer.analyze(classInfos,classDependencyInfo);
//        List<HotspotInfo> hotspotInfos=hotspotAnalyzer.analyze(classInfos,classHealthInfoMap,classDependencyInfo);
//        List<UnusedClassInfo> unusedClassInfos=unusedClassAnalyzer.analyze(classInfos,classDependencyInfo,packageAnalysisInfo.getEntryPointInfo());
//        ProjectHealthSummary projectHealthSummary=projectHealthAnalyzer.summarize(classHealthInfoMap);
//        return new ProjectAnalysisResult(packageAnalysisInfo,packageInfoMap,classInfos,classHealthInfoMap,hotspotInfos,unusedClassInfos,projectHealthSummary);
//    }
//    // Incremental part
//
//    public void onFileCreate(Path path, FileAnalyzer fileAnalyzer){
//        packageAnalysisInfo.onFileOrFolderChange(path, FileChangeEvent.ChangeType.FILE_CREATED,fileAnalyzer.analyzeEntryContribution(path));
//
//
//    }
//    public void onFileDelete(Path path){
//        packageAnalysisInfo.onFileOrFolderChange(path, FileChangeEvent.ChangeType.FILE_DELETED,null);
//
//    }
//    public void onFileChange(Path path,FileAnalyzer fileAnalyzer){
//        packageAnalysisInfo.onFileOrFolderChange(path, FileChangeEvent.ChangeType.FILE_MODIFIED,fileAnalyzer.analyzeEntryContribution(path));
//    }
//    public void onDirCreate(Path path)
//    {packageAnalysisInfo.onFileOrFolderChange(path, FileChangeEvent.ChangeType.DIR_CREATED,null);
//    }
//    public void onDirDelete(Path path){
//        packageAnalysisInfo.onFileOrFolderChange(path, FileChangeEvent.ChangeType.DIR_DELETED,null);
//
//
//
//
//
//    }
//
//
//
//
//}
