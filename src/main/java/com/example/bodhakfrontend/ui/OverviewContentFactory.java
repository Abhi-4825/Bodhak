package com.example.bodhakfrontend.ui;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import javafx.scene.Node;
@FunctionalInterface
public interface OverviewContentFactory {
    Node build(EntityInfo classInfo);
}
