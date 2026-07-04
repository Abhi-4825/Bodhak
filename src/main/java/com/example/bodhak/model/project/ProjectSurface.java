package com.example.bodhak.model.project;

import com.example.bodhak.compiler.symbol.Symbol;
import java.util.EnumSet;
import java.util.Set;

/**
 * An externally visible surface or boundary of a project.
 */
public record ProjectSurface(
    Symbol symbol,
    EnumSet<RootCapability> capabilities,
    Set<Evidence> evidence,
    double confidence
) {}
