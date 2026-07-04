package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;

/**
 * Common contract for every Analysis Report section.
 *
 * Each section observes AnalysisContext and updates only its own
 * observable state.
 */
public interface AnalysisReportSection {

    /**
     * Refresh this section from the latest AnalysisContext.
     */
    void update(AnalysisContext context);
}
