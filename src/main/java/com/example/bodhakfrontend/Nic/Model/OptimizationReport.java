package com.example.bodhakfrontend.Nic.Model;

import com.example.bodhakfrontend.IncrementalPart.model.Project.Hotspots;

import java.util.List;

public class OptimizationReport {

    private final GAResult gaResult;
    private final List<RefactoringSuggestion> suggestions;
    private final double beforeScore;
    private final double afterScore;
    private final List<Hotspots> hotspots;


    public OptimizationReport(GAResult gaResult, List<RefactoringSuggestion> suggestions, double beforeScore, double afterScore, List<Hotspots> hotspots) {
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
    public List<Hotspots> getHotspots() {return hotspots;}
}
