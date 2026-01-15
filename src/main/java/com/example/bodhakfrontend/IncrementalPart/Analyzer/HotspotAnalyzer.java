package com.example.bodhakfrontend.IncrementalPart.Analyzer;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.Hotspots;


import java.util.*;

public class HotspotAnalyzer {
    public List<Hotspots> analyze(
            List<ClassInfo> classInfos
    ) {

        List<Hotspots> hotspots = new ArrayList<>();

        for (ClassInfo cls : classInfos) {

            int score = 0;
            Set<String> reasons = new LinkedHashSet<>();

            int loc = cls.getLinesOfCode();
            int fanOut =cls.getDependsOn().size();

            int fanIn = cls.getUsedBy().size();

            if (loc > 500) {
                score += 3;
                reasons.add("Large class (LOC > 500) :"+"( "+ loc + " ) lines");
            }

            if (cls.getMethods().size() > 20) {
                score += 2;
                reasons.add("Many methods ( "+ cls.getMethods().size() +")");
            }

            if (fanIn + fanOut > 10) {
                score += 3;
                reasons.add("Highly coupled");
            }

            if (!cls.getCircularDependencyGroups().isEmpty()) {
                score += 4;
                reasons.add("Circular dependency");
            }



            if (score >= 5) { // threshold


                hotspots.add(new Hotspots(cls,reasons,score));

            }
        }

        hotspots.sort(
                Comparator.comparingInt(Hotspots::getScore).reversed()
        );

        return hotspots;
    }
}
