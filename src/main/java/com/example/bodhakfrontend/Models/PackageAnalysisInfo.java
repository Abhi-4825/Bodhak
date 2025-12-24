package com.example.bodhakfrontend.Models;

import java.util.List;
import java.util.Map;
import java.util.Set;

public class PackageAnalysisInfo {
    private int folderCount;
    private int fileCount;
    private Map<String, Integer> laguagesCount;
    private EntryPointInfo entryPointInfo;
    private Map<String,Integer> largetFiles;

    public PackageAnalysisInfo(int folderCount, int fileCount, Map<String, Integer> laguagesCount, EntryPointInfo entryPointInfo, Map<String,Integer> largetFiles) {
        this.folderCount = folderCount;
        this.fileCount = fileCount;
        this.laguagesCount = laguagesCount;
        this.entryPointInfo = entryPointInfo;
        this.largetFiles = largetFiles;
    }

    public int getFolderCount() {
        return folderCount;
    }

    public int getFileCount() {
        return fileCount;
    }

    public Map<String, Integer> getLaguagesCount() {
        return laguagesCount;
    }

    public EntryPointInfo getEntryPointInfo() {
        return entryPointInfo;
    }

    public Map<String,Integer> getLargetFiles() {
        return largetFiles;
    }
}
