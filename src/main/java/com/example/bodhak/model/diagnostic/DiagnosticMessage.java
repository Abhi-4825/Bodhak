package com.example.bodhak.model.diagnostic;

/**
 * Represents a compilation error, warning, or static analysis alert.
 */
public record DiagnosticMessage(String severity, String message, int line, int column) {}
