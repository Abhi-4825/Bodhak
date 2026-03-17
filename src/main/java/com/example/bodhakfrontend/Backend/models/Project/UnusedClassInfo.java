package com.example.bodhakfrontend.Backend.models.Project;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
public class UnusedClassInfo {
    public enum UsageStatus {
        USED,
        FRAMEWORK_REACHABLE,
        SUSPICIOUS,
        UNUSED
    }

    private final ClassInfo classInfo;
    private UsageStatus status;
    private String reason;
    public UnusedClassInfo(
            ClassInfo classInfo, UsageStatus status, String reason
    ) {
        this.classInfo = classInfo;

        this.status = status;
        this.reason = reason;
    }

    // getters
    public String getReason() {
        return reason;
    }
    public UsageStatus getStatus() {
        return status;
    }
    public ClassInfo getClassInfo() {
        return classInfo;
    }
}

