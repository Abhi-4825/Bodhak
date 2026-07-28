package com.example.anuviya.analyzer.optimization.model;

import com.example.anuviya.quality.hotspot.HotspotInfo;


import java.util.List;

public class OptimizationReport {

    private final GAResult gaResult;
    private final List<RefactoringSuggestion> suggestions;
    private final double beforeScore;
    private final double afterScore;
    private final List<HotspotInfo> hotspots;


    public OptimizationReport(GAResult gaResult, List<RefactoringSuggestion> suggestions, double beforeScore, double afterScore, List<HotspotInfo> hotspots) {
        this.gaResult = gaResult;
        this.suggestions = suggestions;
        this.beforeScore = beforeScore;
        this.afterScore = afterScore;
        this.hotspots = hotspots;
    }
    public GAResult getGAResult() {
        return gaResult;
    }
    public List<RefactoringSuggestion> getSuggestions() {
        return suggestions;
    }
    public double getBeforeScore() {return beforeScore;}
    public double getAfterScore() {return afterScore;}
    public List<HotspotInfo> getHotspots() {return hotspots;}
}
