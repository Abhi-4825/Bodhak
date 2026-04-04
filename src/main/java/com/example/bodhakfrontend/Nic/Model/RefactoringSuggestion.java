package com.example.bodhakfrontend.Nic.Model;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Class.MethodInfo;

public class RefactoringSuggestion{

    private final ClassInfo clazz;
    private final MethodInfo method;
    private final String reason;
    private final String operation;
    private final String suggestion;


    public RefactoringSuggestion(ClassInfo clazz, MethodInfo method, String reason, String operation, String suggestion) {
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

    public MethodInfo getMethod() {
        return method;
    }

    public ClassInfo getClazz() {
        return clazz;
    }
}
