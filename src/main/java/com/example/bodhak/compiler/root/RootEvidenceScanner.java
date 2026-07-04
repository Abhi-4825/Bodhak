package com.example.bodhak.compiler.root;

import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.compiler.symbol.SymbolTable;
import java.io.File;

/**
 * Contract for project root evidence scanners that consume compiler facts.
 */
public interface RootEvidenceScanner {
    void scan(File projectRoot, ReferenceDatabase database, SymbolTable symbolTable, EvidenceAccumulator accumulator);
}
