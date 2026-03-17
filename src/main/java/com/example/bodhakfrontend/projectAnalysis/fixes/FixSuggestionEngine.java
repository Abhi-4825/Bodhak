package com.example.bodhakfrontend.projectAnalysis.fixes;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Models.FixSuggestion;

import java.util.List;
import java.util.Optional;
import java.util.Random;

public class FixSuggestionEngine {

    private final FixSuggestionStore store = new FixSuggestionStore();
    private final Random random = new Random();

    public Optional<FixSuggestion> suggest(ClassInfo health) {

        List<FixSuggestion> candidates =
                store.getAll()
                        .stream()
                        .filter(f -> FixMatcher.matches(f, health.getIssueType()))
                        .toList();

        if (candidates.isEmpty()) return Optional.empty();

        return Optional.of(
                candidates.get(random.nextInt(candidates.size()))
        );
    }
    public List<FixSuggestion> suggestAll(ClassInfo info) {
        return store.getAll().stream()
                .filter(rule ->
                        info.getIssueType().containsAll(rule.getRequiredIssues())
                )
                .toList();
    }

}

