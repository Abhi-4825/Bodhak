package com.example.anuviya.orchestration.progress;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LoadingProgressModel {
    private final IntegerProperty totalFiles = new SimpleIntegerProperty(0);
    private final IntegerProperty parsedFiles = new SimpleIntegerProperty(0);
    private final IntegerProperty totalEntities = new SimpleIntegerProperty(0);
    private final IntegerProperty totalNamespaces = new SimpleIntegerProperty(0);
    private final IntegerProperty totalDependencies = new SimpleIntegerProperty(0);
    private final IntegerProperty totalReferences = new SimpleIntegerProperty(0);
    private final IntegerProperty totalFrameworks = new SimpleIntegerProperty(0);
    private final IntegerProperty totalMetrics = new SimpleIntegerProperty(0);

    private final DoubleProperty overallProgress = new SimpleDoubleProperty(0.0);
    private final DoubleProperty stageProgress = new SimpleDoubleProperty(-1.0);
    
    private final StringProperty currentStageName = new SimpleStringProperty("Discovering Project...");
    private final StringProperty currentFileName = new SimpleStringProperty("");
    private final BooleanProperty completed = new SimpleBooleanProperty(false);

    private final ObservableList<String> activityFeed = FXCollections.observableArrayList();

    private final BooleanProperty failed = new SimpleBooleanProperty(false);
    private final StringProperty errorMessage = new SimpleStringProperty("");

    public LoadingProgressModel() {}

    public void addFeedMessage(String message) {
        if (activityFeed.size() >= 8) {
            activityFeed.remove(0);
        }
        activityFeed.add(message);
    }

    public void reset() {
        totalFiles.set(0);
        parsedFiles.set(0);
        totalEntities.set(0);
        totalNamespaces.set(0);
        totalDependencies.set(0);
        totalReferences.set(0);
        totalFrameworks.set(0);
        totalMetrics.set(0);
        overallProgress.set(0.0);
        stageProgress.set(-1.0);
        currentStageName.set("Discovering Project...");
        currentFileName.set("");
        completed.set(false);
        failed.set(false);
        errorMessage.set("");
        activityFeed.clear();
    }

    public IntegerProperty totalFilesProperty() { return totalFiles; }
    public IntegerProperty parsedFilesProperty() { return parsedFiles; }
    public IntegerProperty totalEntitiesProperty() { return totalEntities; }
    public IntegerProperty totalNamespacesProperty() { return totalNamespaces; }
    public IntegerProperty totalDependenciesProperty() { return totalDependencies; }
    public IntegerProperty totalReferencesProperty() { return totalReferences; }
    public IntegerProperty totalFrameworksProperty() { return totalFrameworks; }
    public IntegerProperty totalMetricsProperty() { return totalMetrics; }
    public BooleanProperty failedProperty() { return failed; }
    public StringProperty errorMessageProperty() { return errorMessage; }

    public DoubleProperty overallProgressProperty() { return overallProgress; }
    public DoubleProperty stageProgressProperty() { return stageProgress; }

    public StringProperty currentStageNameProperty() { return currentStageName; }
    public StringProperty currentFileNameProperty() { return currentFileName; }
    public BooleanProperty completedProperty() { return completed; }

    public ObservableList<String> getActivityFeed() { return activityFeed; }
}
