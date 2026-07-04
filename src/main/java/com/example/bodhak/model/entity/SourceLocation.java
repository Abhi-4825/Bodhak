package com.example.bodhak.model.entity;

import java.io.File;

/**
 * File mapping and lines/columns location descriptor.
 */
public record SourceLocation(
    File sourceFile,
    int beginLine,
    int beginColumn,
    int endLine,
    int endColumn,
    int linesOfCode
) {}
