package com.example.bodhak.quality.warning;

import com.example.bodhak.quality.flag.EntityFlag;
import com.example.bodhak.model.diagnostic.WarningRule;
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

