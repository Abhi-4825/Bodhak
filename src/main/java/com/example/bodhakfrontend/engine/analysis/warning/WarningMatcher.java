package com.example.bodhakfrontend.engine.analysis.warning;

import com.example.bodhakfrontend.core.analysis.entityflag.EntityFlag;
import com.example.bodhakfrontend.core.model.warning.WarningRule;
import java.util.Set;

public class WarningMatcher {
    public static Boolean matches(WarningRule rule, Set<EntityFlag> flags)
    {
        if(!flags.containsAll(rule.getRequiredIssues())){return false;}
        if(rule.getForbiddenIssues()!=null){
            for(EntityFlag f: rule.getForbiddenIssues()){
                if(flags.contains(f)){return false;}
            }
        }
        return true;
    }
}

