package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.reference.ReferenceKind;
import com.example.bodhak.model.reference.SemanticReference;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.List;

public class ReferenceDatabaseSummaryState implements AnalysisReportSection {

    public record ReferenceSummaryEntry(String kind, int totalUses, long distinctSources, long distinctTargets) {}

    private final ObservableList<ReferenceSummaryEntry> summaries = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        summaries.clear();
        if (context == null || context.getReferenceDatabase() == null) return;
        
        var db = context.getReferenceDatabase();
        for (ReferenceKind kind : ReferenceKind.values()) {
            List<SemanticReference> refs = db.getByKind(kind);
            if (!refs.isEmpty()) {
                long distinctSources = refs.stream().map(r -> r.sourceSymbol().id()).distinct().count();
                long distinctTargets = refs.stream().map(r -> r.targetSymbol().id()).distinct().count();
                summaries.add(new ReferenceSummaryEntry(
                        kind.name(),
                        refs.size(),
                        distinctSources,
                        distinctTargets
                ));
            }
        }
    }

    public ObservableList<ReferenceSummaryEntry> getSummaries() {
        return summaries;
    }
}
