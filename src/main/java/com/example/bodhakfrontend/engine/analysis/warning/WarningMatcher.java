package com.example.bodhakfrontend.engine.analysis.warning;

import com.example.bodhakfrontend.core.model.entity.IssueType;
import com.example.bodhakfrontend.core.model.warning.WarningRule;
import java.util.Set;
public class WarningMatcher {
    public static Boolean matches(WarningRule rule, Set<IssueType> issues)
    {
        if(!issues.containsAll(rule.getRequiredIssues())){return false;}
        if(rule.getForbiddenIssues()!=null){
            for(IssueType i: rule.getForbiddenIssues()){
                if(issues.contains(i)){return false;}
            }
        }
        return true;
    }

}

