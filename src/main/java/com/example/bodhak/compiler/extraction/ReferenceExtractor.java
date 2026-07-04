package com.example.bodhak.compiler.extraction;

import com.example.bodhak.ir.IRNode;
import com.example.bodhak.compiler.symbol.SymbolTable;
import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.compiler.CompilationUnit;

/**
 * Interface for decoupled reference extractors.
 */
public interface ReferenceExtractor {
    void extract(IRNode node, SymbolTable symbolTable, ReferenceDatabase database, CompilationUnit cu);
}
