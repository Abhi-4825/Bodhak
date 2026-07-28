package com.example.anuviya.ui;

import com.example.anuviya.model.entity.EntityInfo;
import javafx.scene.Node;
@FunctionalInterface
public interface OverviewContentFactory {
    Node build(EntityInfo classInfo);
}
