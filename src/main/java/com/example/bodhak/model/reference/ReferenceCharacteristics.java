package com.example.bodhak.model.reference;

import com.example.bodhak.compiler.symbol.Symbol;
import com.example.bodhak.compiler.symbol.SymbolId;
import com.example.bodhak.ir.SourceRange;
import com.example.bodhak.model.reference.payload.ReferencePayload;
import java.io.File;

/**
 * Descriptive properties of a semantic reference.
 */
public record ReferenceCharacteristics(
    boolean isCompileTime,
    boolean isRuntime,
    boolean isTransitive,
    boolean isOptional
) {}
