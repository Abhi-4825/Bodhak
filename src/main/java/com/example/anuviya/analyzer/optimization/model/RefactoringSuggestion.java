package com.example.anuviya.analyzer.optimization.model;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.ir.declaration.CallableDeclaration;

public class RefactoringSuggestion{

    private final EntityInfo clazz;
    private final CallableDeclaration method;
    private final String reason;
    private final String operation;
    private final String suggestion;


    public RefactoringSuggestion(EntityInfo clazz, CallableDeclaration method, String reason, String operation, String suggestion) {
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

    public CallableDeclaration getMethod() {
        return method;
    }

    public EntityInfo getClazz() {
        return clazz;
    }
}
