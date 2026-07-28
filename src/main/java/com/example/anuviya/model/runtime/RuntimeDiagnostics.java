package com.example.anuviya.model.runtime;

import java.util.List;

/**
 * Diagnostic warnings/errors caught during planning or initialization stages.
 */
public record RuntimeDiagnostics(
        List<DiagnosticEntry> entries
) {
    public record DiagnosticEntry(
            String code,
            String message,
            Severity severity
    ) {
    }

    public enum Severity {
        LOW,
        WARNING,
        HIGH,
        CRITICAL
    }
}
