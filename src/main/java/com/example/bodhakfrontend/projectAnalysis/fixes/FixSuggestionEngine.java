package com.example.bodhakfrontend.projectAnalysis.fixes;

import com.example.bodhakfrontend.Models.ClassHealthInfo;
import com.example.bodhakfrontend.Models.FixSuggestion;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class FixSuggestionEngine {

    private final FixSuggestionStore store = new FixSuggestionStore();
    private final Random random = new Random();

    public Optional<FixSuggestion> suggest(ClassHealthInfo health) {

        List<FixSuggestion> candidates =
                store.getAll()
                        .stream()
                        .filter(f -> FixMatcher.matches(f, health.getIssueTypes()))
                        .toList();

        if (candidates.isEmpty()) return Optional.empty();

        return Optional.of(
                candidates.get(random.nextInt(candidates.size()))
        );
    }
    public List<FixSuggestion> suggestAll(ClassHealthInfo info) {
        return store.getAll().stream()
                .filter(rule ->
                        info.getIssueTypes().containsAll(rule.getRequiredIssues())
                )
                .toList();
    }

}

