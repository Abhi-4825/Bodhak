package com.example.anuviya.ui.analysisReport.state;

import com.example.anuviya.context.AnalysisContext;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class DiagnosticsSummaryState implements AnalysisReportSection {

    public record DiagnosticEntry(String severity, String message, String file, int line) {}
    private final ObservableList<DiagnosticEntry> diagnostics = FXCollections.observableArrayList();

    private final IntegerProperty totalErrors = new SimpleIntegerProperty(0);
    private final IntegerProperty totalWarnings = new SimpleIntegerProperty(0);
    private final IntegerProperty filesWithIssues = new SimpleIntegerProperty(0);

    @Override
    public void update(AnalysisContext context) {
        diagnostics.clear();
        if (context == null || context.getCompilationUnits() == null) {
            totalErrors.set(0);
            totalWarnings.set(0);
            filesWithIssues.set(0);
            return;
        }

        int errors = 0;
        int warnings = 0;
        int files = 0;

        for (var cu : context.getCompilationUnits()) {
            if (cu.getDiagnostics() != null && !cu.getDiagnostics().isEmpty()) {
                files++;
                String fileName = cu.getFilePath().getFileName().toString();
                for (var diag : cu.getDiagnostics()) {
                    if ("ERROR".equalsIgnoreCase(diag.severity())) {
                        errors++;
                    } else if ("WARNING".equalsIgnoreCase(diag.severity())) {
                        warnings++;
                    }
                    diagnostics.add(new DiagnosticEntry(
                            diag.severity(),
                            diag.message(),
                            fileName,
                            diag.line()
                    ));
                }
            }
        }

        totalErrors.set(errors);
        totalWarnings.set(warnings);
        filesWithIssues.set(files);
    }

    public IntegerProperty totalErrorsProperty() {
        return totalErrors;
    }

    public IntegerProperty totalWarningsProperty() {
        return totalWarnings;
    }

    public IntegerProperty filesWithIssuesProperty() {
        return filesWithIssues;
    }

    public ObservableList<DiagnosticEntry> getDiagnostics() {
        return diagnostics;
    }
}
