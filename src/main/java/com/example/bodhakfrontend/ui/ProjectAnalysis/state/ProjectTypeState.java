package com.example.bodhakfrontend.ui.ProjectAnalysis.state;


import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.projectType.classification.ProjectClassificationResult;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProjectTypeState {
    private final StringProperty primaryType = new SimpleStringProperty("UNKNOWN");
    private final ObservableList<ProjectTypeItem> projectTypes = FXCollections.observableArrayList();
    private final ObservableList<FrameworkItem> detectedFrameworks = FXCollections.observableArrayList();

    public void update(AnalysisContext context) {
        ProjectClassificationResult result = context.getClassificationResult();

        projectTypes.clear();
        detectedFrameworks.clear();

        if (result != null) {
            primaryType.set(result.primaryType() != null ? result.primaryType().name() : "UNKNOWN");

            result.projectTypes().forEach((type, conf) ->
                    projectTypes.add(new ProjectTypeItem(type, conf))
            );

            result.detectedFrameworks().forEach(fw ->
                    detectedFrameworks.add(new FrameworkItem(fw.frameworkName(), fw.confidence(), fw.isDetected()))
            );
        } else {
            primaryType.set("UNKNOWN");
        }
    }

    public StringProperty primaryTypeProperty() { return primaryType; }
    public ObservableList<ProjectTypeItem> getProjectTypes() { return projectTypes; }
    public ObservableList<FrameworkItem> getDetectedFrameworks() { return detectedFrameworks; }
}