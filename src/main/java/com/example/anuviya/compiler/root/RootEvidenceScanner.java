package com.example.anuviya.compiler.root;

import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.compiler.symbol.SymbolTable;
import java.io.File;

/**
 * Contract for project root evidence scanners that consume compiler facts.
 */
public interface RootEvidenceScanner {
    void scan(File projectRoot, ReferenceDatabase database, SymbolTable symbolTable, EvidenceAccumulator accumulator);
}
