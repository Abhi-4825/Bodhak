package com.example.bodhak.compiler.cache;

import com.example.bodhak.ir.IRNode;
import java.nio.file.Path;
import java.util.Optional;

/**
 * Cache interface for resolved IRNode trees.
 */
public interface IRCache {
    Optional<IRNode> get(Path file, String fileHash);
    void put(Path file, String fileHash, IRNode node);
    void invalidate(Path file);
}
