package com.example.bodhakfrontend.Backend.models.Project;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;

import java.util.Set;

public class Hotspots {
    private final ClassInfo classInfo;
    private final int score;
    private final Set<String> reasons;
    public Hotspots(ClassInfo classInfo, Set<String> reasons, int score) {
        this.classInfo = classInfo;
        this.reasons = reasons;
        this.score = score;
    }
    public ClassInfo getClassInfo() {
        return classInfo;
    }
    public Set<String> getReasons() {
        return reasons;
    }
    public int getScore() {
        return score;
    }
}
