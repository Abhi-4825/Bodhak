package com.example.bodhakfrontend.engine.analysis.fixes;

import com.example.bodhakfrontend.core.model.entity.IssueType;
import com.example.bodhakfrontend.core.model.warning.FixSuggestion;


import java.util.Set;

public class FixMatcher {

    public static boolean matches(
            FixSuggestion fix,
            Set<IssueType> issues
    ) {
        if (fix.getRequiredIssues() != null && !issues.containsAll(fix.getRequiredIssues())) {
            return false;
        }

        if (fix.getForbiddenIssues() != null) {
            for (IssueType i : fix.getForbiddenIssues()) {
                if (issues.contains(i)) return false;
            }
        }

        return true;
    }
}
