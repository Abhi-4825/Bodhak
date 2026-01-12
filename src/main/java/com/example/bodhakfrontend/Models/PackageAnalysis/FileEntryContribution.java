package com.example.bodhakfrontend.Models.PackageAnalysis;

import java.util.HashSet;
import java.util.Set;
public class FileEntryContribution {
    boolean hasSpringBoot;
    boolean hasJavaFx;
    boolean hasMain;
    Set<String> classNames = new HashSet<>();

    public void addClassName(String className) {
        classNames.add(className);
    }
    public boolean hasSpringBoot() {
        return hasSpringBoot;
    }

    public void setHasSpringBoot(boolean hasSpringBoot) {
        this.hasSpringBoot = hasSpringBoot;
    }

    public boolean hasJavaFx() {
        return hasJavaFx;
    }

    public void setHasJavaFx(boolean hasJavaFx) {
        this.hasJavaFx = hasJavaFx;
    }

    public boolean hasMain() {
        return hasMain;
    }

    public void setHasMain(boolean hasMain) {
        this.hasMain = hasMain;
    }

    public Set<String> getClassNames() {
        return classNames;
    }

    public void setClassNames(Set<String> classNames) {
        this.classNames = classNames;
    }
}
