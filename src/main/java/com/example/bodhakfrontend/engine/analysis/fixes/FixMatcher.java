package com.example.bodhakfrontend.engine.analysis.fixes;

import com.example.bodhakfrontend.core.analysis.entityflag.EntityFlag;
import com.example.bodhakfrontend.core.model.warning.FixSuggestion;
import java.util.Set;

public class FixMatcher {

    public static boolean matches(
            FixSuggestion fix,
            Set<EntityFlag> flags
    ) {
        if (fix.getRequiredIssues() != null && !flags.containsAll(fix.getRequiredIssues())) {
            return false;
        }

        if (fix.getForbiddenIssues() != null) {
            for (EntityFlag f : fix.getForbiddenIssues()) {
                if (flags.contains(f)) return false;
            }
        }

        return true;
    }
}
