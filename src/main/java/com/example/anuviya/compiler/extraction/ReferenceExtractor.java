package com.example.anuviya.compiler.extraction;

import com.example.anuviya.ir.IRNode;
import com.example.anuviya.compiler.symbol.SymbolTable;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.compiler.CompilationUnit;

/**
 * Interface for decoupled reference extractors.
 */
public interface ReferenceExtractor {
    void extract(IRNode node, SymbolTable symbolTable, ReferenceDatabase database, CompilationUnit cu);
}
