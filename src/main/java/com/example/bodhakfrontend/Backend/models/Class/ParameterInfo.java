package com.example.bodhakfrontend.Backend.models.Class;

public class ParameterInfo {
    private String parameterName;
    private String parameterType;

    public ParameterInfo(String parameterName, String parameterType) {
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
