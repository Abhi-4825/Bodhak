package com.example.bodhak.compiler.root;

import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.compiler.symbol.*;
import com.example.bodhak.model.project.Evidence;
import com.example.bodhak.model.project.RootCapability;

import java.io.File;

/**
 * Scans structural AST/IR evidence (like main execution entrypoints and public exports) from the SymbolTable.
 */
public class IRScanner implements RootEvidenceScanner {

    @Override
    public void scan(File projectRoot, ReferenceDatabase database, SymbolTable symbolTable, EvidenceAccumulator accumulator) {
        for (Symbol sym : symbolTable.getAllSymbols()) {
            if (sym instanceof MemberSymbol mem) {
                if ("main".equalsIgnoreCase(mem.name())) {
                    accumulator.contribute(
                        mem.parent(),
                        RootCapability.EXECUTABLE,
                        new Evidence("IRScanner", "Declared public static main method", 0.6),
                        "CLI App"
                    );
                }
            }
            if (sym instanceof EntitySymbol ent) {
                accumulator.contribute(
                    ent,
                    RootCapability.LIBRARY_EXPORT,
                    new Evidence("IRScanner", "Public class export candidate", 0.4),
                    "Library"
                );
            }
        }
    }
}
