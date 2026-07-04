package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

public class AiReadinessSummaryState implements AnalysisReportSection {

    // Placeholders
    private final DoubleProperty completeness = new SimpleDoubleProperty(0.0);
    private final IntegerProperty resolvableSymbols = new SimpleIntegerProperty(0);
    private final IntegerProperty missingEvidence = new SimpleIntegerProperty(0);

    @Override
    public void update(AnalysisContext context) {
        // Placeholder values until AI Evidence logic is exposed
        completeness.set(0.95);
        resolvableSymbols.set(8432);
        missingEvidence.set(12);
    }

    public DoubleProperty completenessProperty() {
        return completeness;
    }

    public IntegerProperty resolvableSymbolsProperty() {
        return resolvableSymbols;
    }

    public IntegerProperty missingEvidenceProperty() {
        return missingEvidence;
    }
}
