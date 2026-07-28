package com.example.anuviya.compiler.root.build;

import com.example.anuviya.compiler.root.EvidenceAccumulator;
import com.example.anuviya.compiler.symbol.SymbolTable;
import java.io.File;

/**
 * Interface to extract evidence from a specific build system script file.
 */
public interface BuildEvidenceProvider {
    boolean matches(File projectRoot);
    void extract(File projectRoot, SymbolTable symbolTable, EvidenceAccumulator accumulator);
}
