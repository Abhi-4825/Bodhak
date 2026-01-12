package com.example.bodhakfrontend.Models.PackageAnalysis;

import com.example.bodhakfrontend.LanguageDetector;
import com.example.bodhakfrontend.Models.Incremental.FileChangeEvent;

import java.io.File;
import java.nio.file.Path;
import java.util.*;

public class PackageAnalysisInfo {
    private LanguageDetector languageDetector=new LanguageDetector();




    public static class LargestFileInfo{
        String name;
        File sourceFile;
        int loc;
        public LargestFileInfo(String name,File sourceFile,int loc){
            this.name=name;
            this.loc=loc;
            this.sourceFile=sourceFile;
        }
        public String getName() {
            return name;
        }
        public int getLoc() {
            return loc;
        }
        public File getSourceFile() {return sourceFile;}
    }

    private Map<String, Set<Path>> laguagesCount;
    private List<LargestFileInfo> largetFiles;
    private Map<Path,FileEntryContribution> entryContributions;
    // for stateChange
    private final Set<Path> knownFolders;
    private final Set<Path> knownFiles;


    public PackageAnalysisInfo(Set<Path> knownFolders, Set<Path> knownFiles, Map<String, Set<Path>> laguagesCount, Map<Path,FileEntryContribution> entryContributions, List<LargestFileInfo> largetFiles) {
        this.knownFolders = knownFolders;
        this.knownFiles = knownFiles;
        this.laguagesCount = laguagesCount;
        this.entryContributions = entryContributions;
        this.largetFiles = largetFiles;

    }

    private Path normalize(File file) {
        return file.toPath().toAbsolutePath().normalize();
    }

    public synchronized void onFolderCreated(File folder) {
        knownFolders.add(normalize(folder));
    }

    public synchronized void onFolderDeleted(File folder) {
        Path dir = normalize(folder);

        knownFolders.remove(dir);

        Set<Path> toRemove = new HashSet<>();
        for (Path file : knownFiles) {
            if (file.startsWith(dir)) {
                toRemove.add(file);
            }
        }

        for (Path file : toRemove) {
            decrementLanguage(file.toFile());
            knownFiles.remove(file);
        }
    }


    public synchronized void onFileCreated(File file) {
        Path p = normalize(file);
        if (knownFiles.add(p)) {
            incrementLanguage(file);
        }
    }

    public synchronized void onFileDeleted(File file) {
        Path p = normalize(file);
        if (knownFiles.remove(p)) {
            decrementLanguage(file);
        }
    }
    private void incrementLanguage(File file) {
        String lang = languageDetector.detectFileType(file);
        Path path = normalize(file);

        laguagesCount
                .computeIfAbsent(lang, k -> new HashSet<>())
                .add(path);
    }

    private void decrementLanguage(File file) {
        String lang = languageDetector.detectFileType(file);
        Path path = normalize(file);

        laguagesCount.computeIfPresent(lang, (k, set) -> {
            set.remove(path);
            return set.isEmpty() ? null : set;
        });
    }


    public int getFolderCount() {
        return knownFolders.size();
    }

    public int getFileCount() {
        return knownFiles.size();
    }

    public Map<String, Set<Path>> getLaguagesCount() {
        return laguagesCount;
    }

    public EntryPointInfo getEntryPointInfo() {
        return computeEntryPointInfo();
    }

    public List<LargestFileInfo> getLargetFiles() {
        return largetFiles;
    }



    // calculate Entry point
    private EntryPointInfo computeEntryPointInfo() {

        EntryPointInfo.ProjectType type =
                EntryPointInfo.ProjectType.PLAIN_JAVA;

        String primary = null;
        Set<String> secondary = new LinkedHashSet<>();

        // -------- PHASE 1: Detect project type --------
        for (FileEntryContribution c : entryContributions.values()) {
            if (c.hasSpringBoot()) {
                type = EntryPointInfo.ProjectType.SPRING_BOOT;
                break; // strongest signal, no need to continue
            }
            if (c.hasJavaFx()) {
                type = EntryPointInfo.ProjectType.JAVAFX;
            }
        }

        // -------- PHASE 2: Select PRIMARY by priority --------
        // 1️⃣ Spring Boot
        if (type == EntryPointInfo.ProjectType.SPRING_BOOT) {
            for (FileEntryContribution c : entryContributions.values()) {
                if (c.hasSpringBoot()) {
                    primary = c.getClassNames().iterator().next();
                    break;
                }
            }
        }

        // 2️⃣ JavaFX
        if (primary == null && type == EntryPointInfo.ProjectType.JAVAFX) {
            for (FileEntryContribution c : entryContributions.values()) {
                if (c.hasJavaFx()) {
                    primary = c.getClassNames().iterator().next();
                    break;
                }
            }
        }

        // 3️⃣ main()
        if (primary == null) {
            for (FileEntryContribution c : entryContributions.values()) {
                if (c.hasMain()) {
                    primary = c.getClassNames().iterator().next();
                    break;
                }
            }
        }

        // -------- PHASE 3: Collect SECONDARY --------
        for (FileEntryContribution c : entryContributions.values()) {
            if (c.hasSpringBoot() || c.hasJavaFx() || c.hasMain()) {
                for (String cls : c.getClassNames()) {
                    if (!cls.equals(primary)) {
                        secondary.add(cls);
                    }
                }
            }
        }

        return new EntryPointInfo(
                type,
                primary,
                new ArrayList<>(secondary)
        );
    }
    public synchronized void onEntryFileChanged(Path path,FileEntryContribution contribution) {
        if(contribution!=null) {
            entryContributions.put(path, contribution);
        }else
            entryContributions.remove(path);

    }
    public synchronized void onEntryFileDeleted(Path path) {
        entryContributions.remove(path);
    }


    public synchronized void onFileOrFolderChange(
            Path path,
            FileChangeEvent.ChangeType changeType,
            FileEntryContribution contribution
    ) {
        Path p = path.toAbsolutePath().normalize();
        File f = p.toFile();

        switch (changeType) {

            case FILE_CREATED -> {
                onFileCreated(f);

                if (contribution != null) {
                    entryContributions.put(p, contribution);
                }
            }

            case FILE_MODIFIED -> {
                // counts do NOT change
                // entry contribution MAY change
                if (contribution != null) {
                    entryContributions.put(p, contribution);
                } else {
                    entryContributions.remove(p);
                }
            }

            case FILE_DELETED -> {
                onFileDeleted(f);
                entryContributions.remove(p);
            }

            case DIR_CREATED -> {
                onFolderCreated(f);
            }

            case DIR_DELETED -> {
                onFolderDeleted(f);

                // 🔥 cascade entry-point cleanup
                entryContributions.keySet()
                        .removeIf(ep -> ep.startsWith(p));
            }
        }
    }



}




