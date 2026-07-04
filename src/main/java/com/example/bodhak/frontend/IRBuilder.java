package com.example.bodhak.frontend;

import com.example.bodhak.ir.IRNode;
import java.nio.file.Path;

/**
 * Interface for building a language-independent IRNode tree from a language-specific AST.
 */
public interface IRBuilder<T> {
    
    /**
     * Translates a parsed AST into a semantic IRNode tree.
     */
    IRNode build(Path filePath, T languageAst);
}
