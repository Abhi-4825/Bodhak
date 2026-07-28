package com.example.anuviya.quality.warning;

import com.example.anuviya.quality.flag.EntityFlag;
import com.example.anuviya.model.diagnostic.WarningRule;
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

