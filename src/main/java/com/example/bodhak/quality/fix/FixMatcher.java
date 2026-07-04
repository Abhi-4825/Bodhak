package com.example.bodhak.quality.fix;

import com.example.bodhak.quality.flag.EntityFlag;
import com.example.bodhak.model.diagnostic.FixSuggestion;
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
