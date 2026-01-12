package com.example.bodhakfrontend.projectAnalysis;

import com.example.bodhakfrontend.Models.*;

import java.util.*;

public class HotspotAnalyzer {

    public List<HotspotInfo> analyze(
            List<ClassInfo> classInfos,
           Map<String,ClassHealthInfo> healthInfo,
            ClassDependencyInfo dependencyInfo
    ) {

        List<HotspotInfo> hotspots = new ArrayList<>();

        for (ClassInfo cls : classInfos) {

            int score = 0;
            Set<String> reasons = new LinkedHashSet<>();

            int loc = cls.getLinesOfCode();
            int fanOut = dependencyInfo
                    .getClassDependencies()
                    .getOrDefault(cls.getName(), Set.of())
                    .size();

            int fanIn = dependencyInfo
                    .getReverseClassDependencies()
                    .getOrDefault(cls.getName(), Set.of())
                    .size();

            if (loc > 500) {
                score += 3;
                reasons.add("Large class (LOC > 500) :"+"( "+ loc + " ) lines");
            }

            if (cls.getMethodCount() > 20) {
                score += 2;
                reasons.add("Many methods ( "+ cls.getMethodCount() +")");
            }

            if (fanIn + fanOut > 10) {
                score += 3;
                reasons.add("Highly coupled");
            }

            if (dependencyInfo.getClassesInCycles().contains(cls.getName())) {
                score += 4;
                reasons.add("Circular dependency");
            }

            ClassHealthInfo health = healthInfo.get(cls.getName());

            if (score >= 5) { // threshold

                HotspotInfo hotspotInfo=new HotspotInfo(cls.getName());
                if(health!=null){
                hotspotInfo.setDependencyNode(health.getDependencyNode());}
                hotspotInfo.setScore(score);
                hotspotInfo.setLoc(loc);
                hotspotInfo.setFanIn(fanIn);
                hotspotInfo.setFanOut(fanOut);
                hotspotInfo.setReasons(reasons);

                hotspotInfo.setReasons(reasons);
                hotspots.add(hotspotInfo
                );

            }
        }

        hotspots.sort(
                Comparator.comparingInt(HotspotInfo::getScore).reversed()
        );

        return hotspots;
    }
}
