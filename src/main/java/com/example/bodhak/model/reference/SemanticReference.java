package com.example.bodhak.model.reference;

import com.example.bodhak.compiler.symbol.Symbol;
import com.example.bodhak.compiler.symbol.SymbolId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.model.reference.payload.ReferencePayload;
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
