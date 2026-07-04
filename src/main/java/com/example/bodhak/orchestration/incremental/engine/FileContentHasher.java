package com.example.bodhak.orchestration.incremental.engine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FileContentHasher {

    private final Map<Path, String> hashCache = new ConcurrentHashMap<>();

    public String computeHash(Path file) {
        try {
            if (!Files.exists(file) || Files.isDirectory(file)) {
                hashCache.remove(file);
                return "";
            }
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            try (InputStream is = Files.newInputStream(file)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    digest.update(buffer, 0, read);
                }
            }
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            String hash = sb.toString();
            hashCache.put(file, hash);
            return hash;
        } catch (NoSuchAlgorithmException | IOException e) {
            System.err.println("[Hasher] Error hashing file " + file + ": " + e.getMessage());
            return "";
        }
    }

    public String getCachedHash(Path file) {
        return hashCache.getOrDefault(file, "");
    }

    public void remove(Path file) {
        hashCache.remove(file);
    }

    public void clear() {
        hashCache.clear();
    }
}
