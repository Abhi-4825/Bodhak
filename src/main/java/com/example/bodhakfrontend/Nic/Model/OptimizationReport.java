package com.example.bodhakfrontend.Nic.Model;

import java.util.List;

public class OptimizationReport {

    private final GAResult gaResult;
    private final List<RefactoringSuggestion> suggestions;
    private final double beforeScore;
    private final double afterScore;


    public OptimizationReport(GAResult gaResult, List<RefactoringSuggestion> suggestions, double beforeScore, double afterScore) {
        this.gaResult = gaResult;
        this.suggestions = suggestions;
        this.beforeScore = beforeScore;
        this.afterScore = afterScore;
    }
    public GAResult getGAResult() {
        return gaResult;
    }
    public List<RefactoringSuggestion> getSuggestions() {
        return suggestions;
    }
    public double getBeforeScore() {return beforeScore;}
    public double getAfterScore() {return afterScore;}
}
