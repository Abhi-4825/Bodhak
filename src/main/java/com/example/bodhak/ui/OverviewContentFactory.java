package com.example.bodhak.ui;

import com.example.bodhak.model.entity.EntityInfo;
import javafx.scene.Node;
@FunctionalInterface
public interface OverviewContentFactory {
    Node build(EntityInfo classInfo);
}
