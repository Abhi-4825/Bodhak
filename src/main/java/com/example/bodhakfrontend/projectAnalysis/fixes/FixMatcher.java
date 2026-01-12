package com.example.bodhakfrontend.projectAnalysis.fixes;

import com.example.bodhakfrontend.Models.FixSuggestion;
import com.example.bodhakfrontend.Models.IssueType;

import java.util.Set;

public class FixMatcher {

    public static boolean matches(
            FixSuggestion fix,
            Set<IssueType> issues
    ) {
        if (!issues.containsAll(fix.getRequiredIssues())) return false;

        if (fix.getForbiddenIssues() != null) {
            for (IssueType i : fix.getForbiddenIssues()) {
                if (issues.contains(i)) return false;
            }
        }

        return true;
    }
}
