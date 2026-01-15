package com.example.bodhakfrontend.IncrementalPart.model.Class;

public class ClassContribution {
    private final boolean hasSpring;
    private final boolean hasJavaFx;
    private final boolean hasMain;
    private final boolean hasJPA;
    private final boolean isRestController;
    private final boolean isTestClass;


    public ClassContribution(boolean hasSpring, boolean hasJavaFx, boolean hasMain, boolean hasJPA, boolean isRestController, boolean isTestClass) {
        this.hasSpring = hasSpring;
        this.hasJavaFx = hasJavaFx;
        this.hasMain = hasMain;
        this.hasJPA = hasJPA;
        this.isRestController = isRestController;
        this.isTestClass = isTestClass;
    }
    public boolean hasSpring() {
        return hasSpring;
    }
    public boolean hasJavaFx() {
        return hasJavaFx;
    }
    public boolean hasMain() {
        return hasMain;
    }
    public boolean hasJPA() {
        return hasJPA;
    }
    public boolean isRestController() {
        return isRestController;
    }
    public boolean isTestClass() {
        return isTestClass;
    }

}
