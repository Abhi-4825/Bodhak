package com.example.bodhakfrontend.ui.ProjectAnalysis.state;


import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class EntryPointState {

    private final StringProperty primaryName = new SimpleStringProperty("");
    private final StringProperty primaryLabel = new SimpleStringProperty("");

    private final ObservableList<EntryPointItem> secondaryEntries =
            FXCollections.observableArrayList();

    public void update(AnalysisContext context) {

        EntryPointInfo info = context.getProjectInfo().entryPointInfo();

        EntryPointInfo.Entry primary = info.getPrimaryEntry();

        if (primary != null) {
            primaryName.set(getSimpleName(primary.entityName()));
            primaryLabel.set(primary.label());
        } else {
            primaryName.set("");
            primaryLabel.set("");
        }

        secondaryEntries.clear();

        for (EntryPointInfo.Entry entry : info.getSecondaryEntries()) {

            secondaryEntries.add(
                    new EntryPointItem(
                            entry.entityName(),
                            getSimpleName(entry.entityName()),
                            entry.label()
                    )
            );
        }
    }

    private String getSimpleName(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1) : name;
    }

    public StringProperty primaryNameProperty() {
        return primaryName;
    }

    public StringProperty primaryLabelProperty() {
        return primaryLabel;
    }

    public ObservableList<EntryPointItem> getSecondaryEntries() {
        return secondaryEntries;
    }
}
