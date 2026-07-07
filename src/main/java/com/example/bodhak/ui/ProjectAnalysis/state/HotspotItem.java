package com.example.bodhak.ui.ProjectAnalysis.state;

import com.example.bodhak.quality.flag.EntityFlag;
import com.example.bodhak.quality.hotspot.HotspotInfo;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

public record HotspotItem(

        String qualifiedName,

        String simpleName,

        String riskLabel,

        String riskCss,

        int loc,

        int fanIn,

        int fanOut,

        int beginLine,

        int beginColumn,

        File sourceFile,

        Set<EntityFlag> flags

) {

    public HotspotItem(HotspotInfo info) {

        this(

                info.entity().getEntityName(),

                info.entity().getSimpleName(),

                getRiskLabel(info.score()),

                getRiskCss(info.score()),

                info.entity().getLinesOfCode(),

                info.entity().getUsedBy().size(),

                info.entity().getDependsOn().size(),

                info.entity().getBeginLine(),

                info.entity().getBeginColumn(),

                info.entity().getSourceFile(),

                info.flags()

        );
    }

    private static String getRiskCss(double score) {

        if (score >= 9)
            return "risk-critical";

        if (score >= 6)
            return "risk-high";

        return "risk-medium";
    }

    private static String getRiskLabel(double score) {

        if (score >= 9)
            return "CRITICAL";

        if (score >= 6)
            return "HIGH";

        return "MEDIUM";
    }
    public String description() {
        return flags.stream()
                .map(flag -> switch (flag) {
                    case HIGH_COUPLING -> "High Coupling";
                    case HIGH_INSTABILITY -> "High Instability";
                    case HIGH_FAN_IN -> "High Fan-In";
                    case HIGH_FAN_OUT -> "High Fan-Out";
                    case OVERSIZED_ENTITY -> "Oversized Entity";
                    case CIRCULAR_DEPENDENCY -> "Circular Dependency";
                    case ANEMIC_DOMAIN -> "Anemic Domain";
                })
                .collect(Collectors.joining(" • "));
    }
}
