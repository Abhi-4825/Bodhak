package com.example.bodhakfrontend.engine.optimization.Model;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.entity.MemberInfo;

public class RefactoringSuggestion{

    private final EntityInfo clazz;
    private final MemberInfo method;
    private final String reason;
    private final String operation;
    private final String suggestion;


    public RefactoringSuggestion(EntityInfo clazz, MemberInfo method, String reason, String operation, String suggestion) {
        this.clazz = clazz;
        this.method = method;
        this.reason = reason;
        this.operation = operation;
        this.suggestion = suggestion;
    }

    public String getSuggestion() {
        return suggestion;
    }

    public String getOperation() {
        return operation;
    }

    public String getReason() {
        return reason;
    }

    public MemberInfo getMethod() {
        return method;
    }

    public EntityInfo getClazz() {
        return clazz;
    }
}
