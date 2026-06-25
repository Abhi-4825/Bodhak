package com.example.bodhakfrontend.engine.analyzer;


import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.hotspot.HotspotInfo;
import com.example.bodhakfrontend.core.model.hotspot.HotspotScorer;

import java.util.Comparator;
import java.util.List;

public class HotspotAnalyzer {

    public List<HotspotInfo> analyze(
            AnalysisContext context
    ) {

        HotspotScorer scorer =
                new HotspotScorer();

        return context.getCharacteristics()
                .stream()

                .filter(c ->
                        !c.flags().isEmpty()
                )

                .map(c ->

                        new HotspotInfo(

                                c.entityInfo(),

                                c.flags(),

                                scorer.score(c)
                        )

                )

                .sorted(
                        Comparator.comparingDouble(
                                HotspotInfo::score
                        ).reversed()
                )

                .toList();
    }

}
