package com.example.bodhakfrontend.projectAnalysis.warning;
import com.example.bodhakfrontend.Models.IssueType;
import com.example.bodhakfrontend.Models.WarningRule;

import javax.print.attribute.standard.Severity;
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

