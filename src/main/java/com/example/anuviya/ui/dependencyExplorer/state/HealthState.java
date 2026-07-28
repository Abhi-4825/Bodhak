package com.example.anuviya.ui.dependencyExplorer.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.context.GraphSnapshot;
import com.example.anuviya.model.entity.EntityInfo;
import javafx.beans.property.*;

import java.util.Set;

public class HealthState {

    private final IntegerProperty cyclesCount = new SimpleIntegerProperty(0);
    private final BooleanProperty isHub = new SimpleBooleanProperty(false);
    private final BooleanProperty isOrphan = new SimpleBooleanProperty(false);
    private final DoubleProperty instability = new SimpleDoubleProperty(0.0);
    private final StringProperty healthRating = new SimpleStringProperty("Excellent");

    public void clear() {
        cyclesCount.set(0);
        isHub.set(false);
        isOrphan.set(false);
        instability.set(0.0);
        healthRating.set("Excellent");
    }

    public void update(AnalysisContext context, EntityInfo entity) {
        clear();
        if (context == null || entity == null) return;

        String name = entity.getEntityName();

        int ce = entity.getDependsOn() != null ? entity.getDependsOn().size() : 0;
        int ca = entity.getUsedBy() != null ? entity.getUsedBy().size() : 0;

        // Instability
        if (ca + ce > 0) {
            instability.set((double) ce / (ca + ce));
        } else {
            instability.set(0.0);
        }

        // Hub status (> 25 combined coupling)
        isHub.set((ca + ce) > 25);

        // Orphan status (isolated)
        isOrphan.set(ca == 0 && ce == 0);

        // Cycle counting
        if (context.getDependencyGraph() != null) {
            GraphSnapshot snapshot = context.getDependencyGraph().snapshot();
            if (snapshot != null) {
                int cycleMembership = 0;
                for (Set<String> group : snapshot.circularGroups()) {
                    if (group.contains(name)) {
                        cycleMembership++;
                    }
                }
                cyclesCount.set(cycleMembership);
            }
        }

        // Determine health rating
        if (cyclesCount.get() > 0) {
            healthRating.set("Critical");
        } else if (isHub.get() || ca > 15 || ce > 15) {
            healthRating.set("Warning");
        } else {
            healthRating.set("Excellent");
        }
    }

    public ReadOnlyIntegerProperty cyclesCountProperty() { return cyclesCount; }
    public ReadOnlyBooleanProperty isHubProperty() { return isHub; }
    public ReadOnlyBooleanProperty isOrphanProperty() { return isOrphan; }
    public ReadOnlyDoubleProperty instabilityProperty() { return instability; }
    public ReadOnlyStringProperty healthRatingProperty() { return healthRating; }
}
