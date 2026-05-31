package com.example.bodhakfrontend.core.model.entity;

/**
 * Represents a call-site reference from one member to another.
 * Language-neutral: covers Java method calls, Python function calls, etc.
 */
public class MethodCallInfo {

    public enum CallType {
        /** Call to another member within the same entity. */
        INTERNAL,
        /** Call to a member of a known project entity. */
        EXTERNAL,
        /** Call to a library / stdlib. */
        LIBRARY,
        UNKNOWN
    }

    private final String methodName;
    private final String callerEntity;
    private final String calledEntity;
    private final CallType type;

    public MethodCallInfo(String methodName, String callerEntity, String calledEntity, CallType type) {
        this.methodName   = methodName;
        this.callerEntity = callerEntity;
        this.calledEntity = calledEntity;
        this.type         = type;
    }

    public String getMethodName()   { return methodName;   }
    public String getCallerEntity() { return callerEntity; }
    public String getCalledEntity() { return calledEntity; }
    public CallType getType()       { return type;         }
}
