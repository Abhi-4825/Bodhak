package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;

public class ArchitectureSummaryState implements AnalysisReportSection {

    // Placeholders for when ArchitectureGraph is added to AnalysisContext
    private int nodeCount = 0;
    private int edgeCount = 0;
    private int maxDepth = 0;

    @Override
    public void update(AnalysisContext context) {
        // TODO: Populate when ArchitectureGraph is available in AnalysisContext
        this.nodeCount = 0; 
        this.edgeCount = 0;
        this.maxDepth = 0;
    }

    public int getNodeCount() {
        return nodeCount;
    }

    public int getEdgeCount() {
        return edgeCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }
}
