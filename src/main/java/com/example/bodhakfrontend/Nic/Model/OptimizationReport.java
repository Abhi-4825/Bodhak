package com.example.bodhakfrontend.Nic.Model;

import java.util.List;

public class OptimizationReport {

    private final GAResult gaResult;
    private final List<RefactoringSuggestion> suggestions;


    public OptimizationReport(GAResult gaResult, List<RefactoringSuggestion> suggestions) {
        this.gaResult = gaResult;
        this.suggestions = suggestions;
    }
    public GAResult getGAResult() {
        return gaResult;
    }
    public List<RefactoringSuggestion> getSuggestions() {
        return suggestions;
    }
}
