package com.example.bodhak.compiler.cache;

import com.example.bodhak.model.entity.Metrics;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Cache interface for calculated metrics.
 */
public interface MetricCache {
    Optional<Metrics> get(Path file, String fileHash);
    void put(Path file, String fileHash, Metrics metrics);
    void invalidate(Path file);
}
