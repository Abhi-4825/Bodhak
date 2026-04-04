package com.example.bodhakfrontend.ui;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import javafx.scene.Node;
@FunctionalInterface
public interface OverviewContentFactory {
    Node build(ClassInfo classInfo);
}
