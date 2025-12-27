package com.example.bodhakfrontend.Models;

public class MethodParameterInfo {
    private String parameterName;
    private String parameterType;

    public MethodParameterInfo(String parameterName, String parameterType) {
        this.parameterName = parameterName;
        this.parameterType = parameterType;
    }
    public String getParameterName() {
        return parameterName;
    }
    public String getParameterType() {
        return parameterType;
    }
}
