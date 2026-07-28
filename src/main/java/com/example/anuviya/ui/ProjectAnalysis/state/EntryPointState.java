package com.example.anuviya.ui.ProjectAnalysis.state;


import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.model.project.ProjectRootInfo;
import com.example.anuviya.model.project.ProjectSurface;
import com.example.anuviya.model.project.RootCapability;
import java.util.List;
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

        ProjectRootInfo info = context.getProjectInfo().projectRootInfo();
        List<ProjectSurface> surfaces = info.surfaces();

        ProjectSurface primary = surfaces.stream()
                .filter(s -> s.capabilities().contains(RootCapability.EXECUTABLE) || s.capabilities().contains(RootCapability.API_PROVIDER))
                .findFirst()
                .orElse(surfaces.isEmpty() ? null : surfaces.get(0));

        if (primary != null) {
            primaryName.set(getSimpleName(primary.symbol().name()));
            primaryLabel.set(primary.capabilities().toString());
        } else {
            primaryName.set("");
            primaryLabel.set("");
        }

        secondaryEntries.clear();

        for (ProjectSurface ps : surfaces) {
            if (ps == primary) continue;
            secondaryEntries.add(
                    new EntryPointItem(
                            ps.symbol().name(),
                            getSimpleName(ps.symbol().name()),
                            ps.capabilities().toString()
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
