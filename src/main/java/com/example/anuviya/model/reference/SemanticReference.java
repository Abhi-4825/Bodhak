package com.example.anuviya.model.reference;

import com.example.anuviya.compiler.symbol.Symbol;
import com.example.anuviya.compiler.symbol.SymbolId;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.model.reference.payload.ReferencePayload;
import java.io.File;

/**
 * A canonical compiler reference from a source symbol to a target symbol.
 */
public record SemanticReference(
    Symbol sourceSymbol,            // Source entity/namespace/member
    Symbol targetSymbol,            // Target entity/namespace/member
    SymbolId originNodeId,          // Exact origin IR/AST node
    ReferenceKind kind,             // Broad category (e.g. CALL)
    SemanticRole role,              // Specific role (e.g. CallReferenceRole.INVOCATION)
    ReferenceCharacteristics characteristics, // Descriptive properties
    File sourceFile,                // Physical file location
    SourceRange location,           // Code range bounds
    ReferencePayload payload        // Compiler-centric payload details
) {}
