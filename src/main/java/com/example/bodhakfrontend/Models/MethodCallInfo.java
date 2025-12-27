package com.example.bodhakfrontend.Models;

public class MethodCallInfo {
    private final String methodName;
    private final String className;
    private final CallType type;
   public enum CallType{
       INTERNAL,EXTERNAL,LIBRARY
   }
    public MethodCallInfo(String methodName, String className, CallType type) {
        this.methodName = methodName;
        this.className = className;
        this.type = type;
    }

    public String getMethodName() {
        return methodName;
    }

    public String getClassName() {
        return className;
    }

   public CallType getType() {
       return type;
   }




}
