package com.example.bodhakfrontend.IncrementalPart.Analyzer;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.Hotspots;


import java.util.*;

public class HotspotAnalyzer {


    private final List<Hotspots> hotspots = new ArrayList<>();
    public List<Hotspots> analyzeAll(
            List<ClassInfo> classInfos
    ) {
        for (ClassInfo cls : classInfos) {

            updateHotspot(cls);

        }

     return getHotspots();
    }

    public void updateHotspot(ClassInfo cls) {
        Set<String> reasons = new HashSet<>();
        int score = 0;
        int loc = cls.getLinesOfCode();
        int fanOut =cls.getDependsOn().size();
        int fanIn = cls.getUsedBy().size();
        if (loc > 300) {
            score += 3;
            reasons.add("Large class (LOC > 300) :"+"( "+ loc + " ) lines");
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


    public List<Hotspots> getHotspots() {
        hotspots.sort(
                Comparator.comparingInt(Hotspots::getScore).reversed()
        );

        return hotspots;
    }

}
