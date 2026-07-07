package com.example.bodhak.quality.flag;
import com.example.bodhak.model.entity.EntityInfo;

import java.util.EnumSet;
import java.util.Set;

public class EntityFlagAnalyzer {

    private final ArchitectureThresholds thresholds;
    private final ProjectBaselines baselines;

    public EntityFlagAnalyzer(
            ArchitectureThresholds thresholds,
            ProjectBaselines baselines
    ) {
        this.thresholds = thresholds;
        this.baselines = baselines;
    }

    public Set<EntityFlag> analyze(
            EntityInfo entity
    ) {

        Set<EntityFlag> flags =
                EnumSet.noneOf(EntityFlag.class);

        if (isOversized(entity)) {
            flags.add(EntityFlag.OVERSIZED_ENTITY);
        }

        if (isHighlyCoupled(entity)) {
            flags.add(EntityFlag.HIGH_INSTABILITY);
        }

        if (!entity.getCircularGroups().isEmpty()) {
            flags.add(EntityFlag.CIRCULAR_DEPENDENCY);
        }

        if (isHighFanIn(entity)) {
            flags.add(EntityFlag.HIGH_FAN_IN);
        }

        if (isHighFanOut(entity)) {
            flags.add(EntityFlag.HIGH_FAN_OUT);
        }

        return flags;
    }

    private boolean isOversized(
            EntityInfo entity
    ) {

        boolean absolute =

                entity.getLinesOfCode()
                        >= thresholds.minGodClassLoc()

                        ||

                        entity.getMethodCount()
                                >= thresholds.minGodClassMethods()

                        ||

                        entity.getFields().size()
                                >= thresholds.minGodClassFields();

        boolean relative =

                entity.getLinesOfCode()
                        >= baselines.p90Loc()

                        ||

                        entity.getMethodCount()
                                >= baselines.p90Methods()

                        ||

                        entity.getFields().size()
                                >= baselines.p90Fields();

        return absolute && relative;
    }

    private boolean isHighlyCoupled(EntityInfo entity) {
        int fanOut = entity.getDependsOn().size();
        int fanIn = entity.getUsedBy().size();
        int totalCoupling = fanIn + fanOut;

        // Prevent division by zero for isolated classes
        if (totalCoupling == 0) {
            return false;
        }

        // Instability ranges from 0.0 (completely stable) to 1.0 (completely unstable)
        double instability = (double) fanOut / totalCoupling;

        // Define coupling as: high absolute outgoing dependencies combined with high instability
        boolean isFragile = fanOut >= Math.max(thresholds.minFanOut(), baselines.p90FanOut());
        boolean isHighlyUnstable = instability > 0.7; // 70%+ of its connections are outgoing

        return isFragile && isHighlyUnstable;
    }


    private boolean isHighFanIn(
            EntityInfo entity
    ) {

        return entity.getUsedBy().size()
                >= Math.max(
                thresholds.minFanIn(),
                baselines.p90FanIn()
        );
    }

    private boolean isHighFanOut(
            EntityInfo entity
    ) {

        return entity.getDependsOn().size()
                >= Math.max(
                thresholds.minFanOut(),
                baselines.p90FanOut()
        );
    }
}
