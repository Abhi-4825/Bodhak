package com.example.bodhakfrontend.core.plugin;

import com.example.bodhakfrontend.core.model.ast.GenericAstNode;

import java.nio.file.Path;

/**
 * Parses a source file into a language-neutral GenericAstNode tree.
 *
 * Each language plugin provides its own implementation:
 *   Java   → uses JavaParser CompilationUnit
 *   Python → uses Tree-sitter TSTree
 *   (any future language just implements this interface)
 *
 * Returns null if the file cannot be parsed (e.g. syntax errors).
 */
public interface AstProvider {
    GenericAstNode parse(Path file);
}
