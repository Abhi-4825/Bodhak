package com.example.anuviya.model.project;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record ProjectInfo(
        String projectName,
        Path projectRoot,
        Map<String, Set<Path>> languageCountMap,
        List<LargestFileInfo> largestFiles,
        Set<Path> knownFolders,
        Set<Path> knownFiles,
        ProjectRootInfo projectRootInfo,
        int totalEntities
) {}
