package com.example.bodhakfrontend.core.analysis.entityflag;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;

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
            flags.add(EntityFlag.HIGH_COUPLING);
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

    private boolean isHighlyCoupled(
            EntityInfo entity
    ) {

        return entity.getDependsOn().size()
                >= Math.max(
                thresholds.minHighCoupling(),
                baselines.p90FanOut()
        );
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
