package com.example.bodhak.ui.analysisReport.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.reference.ReferenceKind;
import javafx.beans.property.LongProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * State for the Semantic Flow donut chart.
 * Groups all references from the ReferenceDatabase into broad display categories,
 * computing absolute counts per segment.
 */
public class SemanticFlowState implements AnalysisReportSection {

    public record FlowSegment(String label, long count, String color) {}

    private final ObservableList<FlowSegment> segments = FXCollections.observableArrayList();
    private final LongProperty totalRefs = new SimpleLongProperty(0);

    @Override
    public void update(AnalysisContext context) {
        segments.clear();
        totalRefs.set(0);
        if (context == null || context.getReferenceDatabase() == null) return;

        var db = context.getReferenceDatabase();

        long typeCount    = count(db, ReferenceKind.TYPE);
        long memberCount  = count(db, ReferenceKind.MEMBER);
        long callCount    = count(db, ReferenceKind.CALL);
        long importCount  = count(db, ReferenceKind.IMPORT);
        long annoCount    = count(db, ReferenceKind.ANNOTATION);
        long dataCount    = count(db, ReferenceKind.DATA_FLOW);
        long otherCount   = count(db, ReferenceKind.CONTROL_FLOW)
                          + count(db, ReferenceKind.CONCURRENCY)
                          + count(db, ReferenceKind.FRAMEWORK);

        long total = typeCount + memberCount + callCount + importCount
                   + annoCount + dataCount + otherCount;

        totalRefs.set(total);
        if (total == 0) return;

        // Map to the three display segments shown in the reference design
        long typeDisplay   = typeCount + importCount + annoCount;
        long callDisplay   = callCount;
        long fieldDisplay  = memberCount + dataCount;
        long otherDisplay  = otherCount;

        if (typeDisplay  > 0) segments.add(new FlowSegment("Type References",  typeDisplay,  "#00daf3"));
        if (callDisplay  > 0) segments.add(new FlowSegment("Call References",  callDisplay,  "#bbc8d8"));
        if (fieldDisplay > 0) segments.add(new FlowSegment("Field References", fieldDisplay, "#fec931"));
        if (otherDisplay > 0) segments.add(new FlowSegment("Other",            otherDisplay, "#56d69b"));
    }

    private long count(com.example.bodhak.context.db.ReferenceDatabase db, ReferenceKind kind) {
        var refs = db.getByKind(kind);
        return refs == null ? 0 : refs.size();
    }

    public ObservableList<FlowSegment> getSegments() {
        return segments;
    }

    public LongProperty totalRefsProperty() {
        return totalRefs;
    }

    public long getTotalRefs() {
        return totalRefs.get();
    }
}
