package com.example.bodhak.compiler.root.build;

import com.example.bodhak.compiler.root.EvidenceAccumulator;
import com.example.bodhak.compiler.symbol.SymbolTable;
import java.io.File;

/**
 * Interface to extract evidence from a specific build system script file.
 */
public interface BuildEvidenceProvider {
    boolean matches(File projectRoot);
    void extract(File projectRoot, SymbolTable symbolTable, EvidenceAccumulator accumulator);
}
