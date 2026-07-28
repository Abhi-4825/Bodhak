package com.example.anuviya.ui.ProjectAnalysis.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.entity.EntityInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Comparator;

public class EntityMetricsState {

    private final ObservableList<EntityMetricItem> entities =
            FXCollections.observableArrayList();

    public void update(AnalysisContext context) {

        entities.setAll(

                context.getEntities()
                        .stream()
                        .sorted(Comparator.comparing(
                                EntityInfo::getSimpleName,
                                String.CASE_INSENSITIVE_ORDER
                        ))
                        .map(EntityMetricItem::new)
                        .toList()
        );
    }

    public ObservableList<EntityMetricItem> getEntities() {
        return entities;
    }
}
