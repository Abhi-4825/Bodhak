package com.example.anuviya.ui.dependencyExplorer.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.entity.EntityInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

public class EntityBrowserState {

    private final ObservableList<EntityInfo> allEntities = FXCollections.observableArrayList();
    private final FilteredList<EntityInfo> filteredEntities = new FilteredList<>(allEntities);
    private final StringProperty searchQuery = new SimpleStringProperty("");

    public EntityBrowserState() {
        searchQuery.addListener((obs, old, query) -> {
            if (query == null || query.isBlank()) {
                filteredEntities.setPredicate(entity -> true);
            } else {
                String lower = query.toLowerCase();
                filteredEntities.setPredicate(entity -> 
                    entity.getEntityName().toLowerCase().contains(lower) ||
                    entity.getSimpleName().toLowerCase().contains(lower)
                );
            }
        });
    }

    public void update(AnalysisContext context) {
        allEntities.setAll(context.getEntities());
    }

    public ObservableList<EntityInfo> getFilteredEntities() {
        return filteredEntities;
    }

    public StringProperty searchQueryProperty() {
        return searchQuery;
    }
}
