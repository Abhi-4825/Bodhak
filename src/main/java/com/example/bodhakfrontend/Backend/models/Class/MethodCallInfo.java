package com.example.bodhakfrontend.Backend.models.Class;

public class MethodCallInfo {
    private  String methodName;
    private  String callerClass;
    private  String fromClass;
    private CallType type;
    public enum CallType{
        INTERNAL,EXTERNAL,LIBRARY
    }
    public MethodCallInfo(String methodName,String callerClass,String fromClass,CallType type){
        this.methodName=methodName;
        this.callerClass=callerClass;
        this.fromClass=fromClass;
        this.type=type;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getCallerClass() {
        return callerClass;
    }

    public String getFromClass() {
        return fromClass;
    }

    public CallType getType() {
        return type;
    }
}
