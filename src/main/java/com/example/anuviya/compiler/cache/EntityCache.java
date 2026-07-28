package com.example.anuviya.compiler.cache;

import com.example.anuviya.model.entity.EntityInfo;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/**
 * Cache interface for mapped EntityInfo list.
 */
public interface EntityCache {
    Optional<List<EntityInfo>> get(Path file, String fileHash);
    void put(Path file, String fileHash, List<EntityInfo> entities);
    void invalidate(Path file);
}
