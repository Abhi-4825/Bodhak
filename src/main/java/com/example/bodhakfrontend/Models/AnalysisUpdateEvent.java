package com.example.bodhakfrontend.Models;


import java.util.Set;

public class AnalysisUpdateEvent {

    private final Set<String> affectedClasses;

    public AnalysisUpdateEvent(Set<String> affectedClasses) {
        this.affectedClasses = affectedClasses;
    }

    public Set<String> affectedClasses() {
        return affectedClasses;
    }
}

