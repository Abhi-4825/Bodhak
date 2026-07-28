package com.example.anuviya.workspace;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

public final class CacheManager {
    public record CacheEntry(String filePath, String sha256, long lastModified, Instant recorded) {}

    private final File workspaceDir;
    private final ObjectMapper mapper;

    public CacheManager(File workspaceDir) {
        this.workspaceDir = workspaceDir;
        this.mapper = new ObjectMapper();
        this.mapper.findAndRegisterModules();
    }

    public boolean isCacheValid(UUID projectId, File projectRoot, String cacheKey) {
        File cacheFile = new File(workspaceDir, "projects/" + projectId.toString() + "/cache/" + cacheKey + ".cache");
        if (!cacheFile.exists()) {
            return false;
        }

        long projectDirTimestamp = getLastModifiedRecursive(projectRoot);
        try {
            Map<String, Object> cacheData = mapper.readValue(cacheFile, new TypeReference<Map<String, Object>>() {});
            long recordedTimestamp = ((Number) cacheData.get("projectLastModified")).longValue();
            if (recordedTimestamp == projectDirTimestamp) {
                return true; // Fast path: Unchanged!
            }

            // Changed: Hash only modified files vs stored entries
            List<Map<String, Object>> rawEntries = (List<Map<String, Object>>) cacheData.get("entries");
            if (rawEntries == null) return false;

            for (Map<String, Object> entryMap : rawEntries) {
                String relativePath = (String) entryMap.get("filePath");
                String recordedHash = (String) entryMap.get("sha256");
                long recordedFileModified = ((Number) entryMap.get("lastModified")).longValue();

                File file = new File(projectRoot, relativePath);
                if (!file.exists()) return false; // File deleted!

                if (file.lastModified() != recordedFileModified) {
                    String fileHash = calculateSHA256(file);
                    if (!fileHash.equals(recordedHash)) {
                        return false; // Content changed!
                    }
                }
            }

            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public void writeCacheEntry(UUID projectId, File projectRoot, String cacheKey) {
        File cacheDir = new File(workspaceDir, "projects/" + projectId.toString() + "/cache");
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        File cacheFile = new File(cacheDir, cacheKey + ".cache");

        try {
            List<CacheEntry> entries = new ArrayList<>();
            collectCacheEntries(projectRoot, projectRoot, entries);

            Map<String, Object> cacheData = new HashMap<>();
            cacheData.put("projectLastModified", getLastModifiedRecursive(projectRoot));
            cacheData.put("entries", entries);
            cacheData.put("cacheVersion", "v2");

            mapper.writeValue(cacheFile, cacheData);
        } catch (IOException e) {
            System.err.println("[CacheManager] Failed to write cache: " + e.getMessage());
        }
    }

    public void invalidateCache(UUID projectId) {
        File cacheDir = new File(workspaceDir, "projects/" + projectId.toString() + "/cache");
        if (cacheDir.exists()) {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
        }
    }

    private void collectCacheEntries(File projectRoot, File current, List<CacheEntry> entries) {
        File[] files = current.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                collectCacheEntries(projectRoot, f, entries);
            } else if (f.getName().endsWith(".java") || f.getName().endsWith(".kt")) {
                String relPath = projectRoot.toURI().relativize(f.toURI()).getPath();
                String hash = calculateSHA256(f);
                entries.add(new CacheEntry(relPath, hash, f.lastModified(), Instant.now()));
            }
        }
    }

    private long getLastModifiedRecursive(File file) {
        long max = file.lastModified();
        File[] files = file.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    max = Math.max(max, getLastModifiedRecursive(f));
                } else if (f.getName().endsWith(".java") || f.getName().endsWith(".kt")) {
                    max = Math.max(max, f.lastModified());
                }
            }
        }
        return max;
    }

    private final com.example.anuviya.orchestration.incremental.engine.FileContentHasher hasher = new com.example.anuviya.orchestration.incremental.engine.FileContentHasher();

    private String calculateSHA256(File file) {
        if (file == null || !file.exists()) return "";
        return hasher.computeHash(file.toPath());
    }
}
