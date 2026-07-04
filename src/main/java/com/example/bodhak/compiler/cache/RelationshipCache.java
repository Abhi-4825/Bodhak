package com.example.bodhak.compiler.cache;

import com.example.bodhak.model.entity.Relationships;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Cache interface for resolved relationships.
 */
public interface RelationshipCache {
    Optional<Relationships> get(Path file, String fileHash);
    void put(Path file, String fileHash, Relationships relationships);
    void invalidate(Path file);
}
