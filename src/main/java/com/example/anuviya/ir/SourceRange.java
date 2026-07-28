package com.example.anuviya.ir;

/**
 * Represents a text range inside a source code file.
 * Line and column numbers are 1-based.
 */
public record SourceRange(int startLine, int startColumn, int endLine, int endColumn) {
    public static final SourceRange UNKNOWN = new SourceRange(0, 0, 0, 0);
}
