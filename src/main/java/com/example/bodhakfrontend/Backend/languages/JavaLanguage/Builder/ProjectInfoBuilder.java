package com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder;
import com.example.bodhakfrontend.Backend.Analyzer.EntryClassAnalyzer;
import com.example.bodhakfrontend.Backend.Analyzer.HotspotAnalyzer;
import com.example.bodhakfrontend.Backend.Analyzer.UnusedClassAnalyzer;
import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Class.IssueType;
import com.example.bodhakfrontend.Backend.models.Package.PackageInfo;
import com.example.bodhakfrontend.Backend.models.Project.EntryPointInfo;
import com.example.bodhakfrontend.Backend.models.Project.Hotspots;
import com.example.bodhakfrontend.Backend.models.Project.ProjectInfo;
import com.example.bodhakfrontend.Backend.models.Project.UnusedClassInfo;
import com.example.bodhakfrontend.LanguageDetector;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

public class ProjectInfoBuilder {
    private final LanguageDetector languageDetector=new LanguageDetector();
    private final HotspotAnalyzer hotspotAnalyzer=new HotspotAnalyzer();
    private final EntryClassAnalyzer entryClassAnalyzer=new  EntryClassAnalyzer();
    private final UnusedClassAnalyzer  unusedClassAnalyzer=new UnusedClassAnalyzer();
    private Map<String,PackageInfo> packageInfoMap;
    private List<ClassInfo> classInfos;
    private List<Hotspots>  hotspots;
    private EntryPointInfo entryPointInfo;
    private Set<UnusedClassInfo> unusedClassInfos;
    private final List<ProjectInfo.LargestFileInfo> largestFiles=new ArrayList<>();
    private Map<String,Set<Path>> laguageCountMap=new HashMap<>();
   private  List<ProjectInfo.LargestFileInfo> top5=new ArrayList<>();
    // Folders in Project
    private Set<Path> knownFolders=new HashSet<>();
    // files
    private Set<Path> knownFiles=new HashSet<>();
    // total classes
    int totalClasses;
    //total healthy classes
    int healthyClasses=0;
    int warningClasses=0;
    int godClasses=0;
    int circularClasses=0;
    int highlyCoupledClasses=0;





    public ProjectInfo buildAll(Path projectPath,List<ClassInfo> classInfoList,Map<String,PackageInfo> packageInfo) {
        largestFiles.clear();
        laguageCountMap.clear();
        knownFolders.clear();
        knownFiles.clear();

        //Classinfos

        classInfos=classInfoList;

        //package info

        packageInfoMap=packageInfo;


        // get List of Hotspot
        hotspots=hotspotAnalyzer.analyzeAll(classInfos);

        //Entry point

        entryPointInfo=entryClassAnalyzer.build(classInfos);

        //unused Class

        unusedClassInfos=unusedClassAnalyzer.analyze(classInfos,entryPointInfo, UnusedClassAnalyzer.Mode.DEV);

         recomputeClassMetrics();

       try {
           Files.walk(projectPath)
                   .forEach(path -> {
                        updateProjectInfo(path);
                   });
       } catch (Exception e) {
           e.printStackTrace();
       }

      return getProjectInfo();

    }
    private boolean isCodeLine(String line) {
        return !line.isEmpty()
                && !line.startsWith("//")
                && !line.startsWith("/*")
                && !line.startsWith("*")
                && !line.startsWith("*/");
    }


    private void recomputeClassMetrics() {
        healthyClasses=0;
        warningClasses=0;
        godClasses=0;
        circularClasses=0;
        highlyCoupledClasses=0;

        totalClasses = classInfos.size();
        for (ClassInfo classInfo : classInfos) {
            Set<IssueType> issues = classInfo.getIssueType();
            if (issues.isEmpty()) healthyClasses++;
            else warningClasses++;
            if (issues.contains(IssueType.GOD_CLASS)) godClasses++;
            if (issues.contains(IssueType.CIRCULAR_DEPENDENCY)) circularClasses++;
            if (issues.contains(IssueType.HIGH_COUPLING)) highlyCoupledClasses++;
        }
    }


    private void updateProjectInfo(Path file){




    Path normalizedPath=file.toAbsolutePath().normalize();
    File f=normalizedPath.toFile();

    try {

        //Largest files
        if (Files.isRegularFile(normalizedPath) && Files.isReadable(normalizedPath)) {

            String name = normalizedPath.getFileName().toString();

            // skip hidden / git / binaries
            if (name.startsWith(".") || name.endsWith(".class")) return;

            try (var lines = Files.lines(normalizedPath)) {
                int loc = (int) lines
                        .map(String::trim)
                        .filter(this::isCodeLine)
                        .count();
                if(normalizedPath.toString().endsWith(".java")){

                    largestFiles.add(
                            new ProjectInfo.LargestFileInfo(
                                    name,
                                    normalizedPath.toFile(),
                                    loc
                            )

                    );



                }
                top5 = largestFiles.stream()
                        .sorted(
                                Comparator.comparingInt(
                                        ProjectInfo.LargestFileInfo::getLoc
                                ).reversed()
                        )
                        .limit(5)
                        .toList();

            } catch (Exception ignored) {
                // silently skip unreadable / binary files
            }
        }

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

    } catch (Exception e) {
        e.printStackTrace();
    }

}


// get projectINfo
    public ProjectInfo getProjectInfo() {
        return new ProjectInfo(laguageCountMap,top5,knownFolders,knownFiles,classInfos,packageInfoMap,
                hotspots,entryPointInfo,unusedClassInfos,totalClasses,healthyClasses,warningClasses,godClasses,circularClasses,highlyCoupledClasses);
    }




// on File Create
    public void onFileCreated(Path path,List<ClassInfo> newClassInfos) {
       updateProjectInfo(path);

        recomputeClassMetrics();

        // update Hotspot
        for(ClassInfo classInfo:newClassInfos){
            hotspotAnalyzer.updateHotspot(classInfo);
        }
        hotspots=hotspotAnalyzer.getHotspots();

        //update EntryPoint
        entryPointInfo=entryClassAnalyzer.build(classInfos);

        //update unused
        unusedClassInfos=unusedClassAnalyzer.analyze(classInfos,entryPointInfo, UnusedClassAnalyzer.Mode.DEV);
    }

    //on File Delete
    public void onFileDelete(Path path) {
        Path normalizedPath=path.toAbsolutePath().normalize();
        knownFiles.remove(normalizedPath);

    
        recomputeClassMetrics();

        // remove from largestFiles
        largestFiles.removeIf(
                lf -> lf.getSourceFile().toPath().toAbsolutePath().normalize().equals(normalizedPath)
        );

        // recompute top5 safely
        top5 = largestFiles.stream()
                .sorted(Comparator.comparingInt(ProjectInfo.LargestFileInfo::getLoc).reversed())
                .limit(5)
                .toList();

        //language count update

        String language=languageDetector.detectFileType(normalizedPath.toFile());
        laguageCountMap.computeIfPresent(language, (k, v) -> {
            v.remove(normalizedPath);
            return v.isEmpty() ? null : v; // remove key if empty
        });


        //hotspot
        hotspots.removeIf(h->h.getClassInfo().getSourceFile().toString().equals(path.toString()));

        //EntryPointinfo
        entryPointInfo=entryClassAnalyzer.build(classInfos);

        //unusedClass
        unusedClassInfos=unusedClassAnalyzer.analyze(classInfos,entryPointInfo, UnusedClassAnalyzer.Mode.DEV);





    }

    public void onFileUpdate(Path path,List<ClassInfo> newClassInfos) {
        onFileDelete(path);
        onFileCreated(path,newClassInfos);

    }

    public void onFolderCreated(Path createdFolder){
        knownFolders.add(createdFolder.toAbsolutePath().normalize());
    }
    public void onFolderDeleted(Path deletedFolder){
        knownFolders.remove(deletedFolder.toAbsolutePath().normalize());
    }
















}
