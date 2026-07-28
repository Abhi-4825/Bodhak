package com.example.anuviya.frontend;

import java.nio.file.Path;

/**
 * Standardized parsing service for extracting an AST from source code files.
 * Reuses caches to prevent repeated parsing.
 */
public interface ParserService<T> {
    
    /**
     * Parses the given file and returns its AST tree.
     */
    T parse(Path filePath);

    /**
     * Invalidates cache entry for the file.
     */
    void invalidate(Path filePath);
}
