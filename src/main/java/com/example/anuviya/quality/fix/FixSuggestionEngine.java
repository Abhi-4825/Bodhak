package com.example.anuviya.quality.fix;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.quality.flag.EntityCharacteristics;
import com.example.anuviya.quality.flag.EntityFlag;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.diagnostic.FixSuggestion;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public class FixSuggestionEngine {

    private final FixSuggestionStore store = new FixSuggestionStore();
    private final Random random = new Random();

    public Optional<FixSuggestion> suggest(EntityInfo entity, AnalysisContext context) {
        List<FixSuggestion> candidates = suggestAll(entity, context);

        if (candidates.isEmpty()) return Optional.empty();

        return Optional.of(
                candidates.get(random.nextInt(candidates.size()))
        );
    }
    public List<FixSuggestion> suggestAll(EntityInfo info, AnalysisContext context) {
        if (context == null) return List.of();
        Set<EntityFlag> flags = context.findCharacteristics(info.getEntityName())
                .map(EntityCharacteristics::flags)
                .orElse(Set.of());

        return store.getAll().stream()
                .filter(fix -> FixMatcher.matches(fix, flags))
                .toList();
    }

}

