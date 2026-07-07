package com.example.bodhak.orchestration.progress;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.effect.BoxBlur;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class LoadingOverlayManager implements ProgressSubscriber {

    private static final LoadingOverlayManager INSTANCE = new LoadingOverlayManager();

    private final LoadingProgressModel model = new LoadingProgressModel();
    private LoadingOverlayView currentView = null;
    private StackPane windowStack = null;
    private Node mainContent = null;
    private ParallelTransition activeTransition = null;

    private LoadingOverlayManager() {
        ProgressPublisher.subscribe(this);
    }

    public static LoadingOverlayManager getInstance() {
        return INSTANCE;
    }

    public void setWindowStack(StackPane stack, Node mainContent) {
        this.windowStack = stack;
        this.mainContent = mainContent;
    }

    @Override
    public void onProgress(AnalysisProgressEvent event) {
        Platform.runLater(() -> {
            if (currentView == null && !(event instanceof AnalysisProgressEvents.ProjectDiscoveryStarted)) {
                return;
            }

            if (event instanceof AnalysisProgressEvents.ProjectDiscoveryStarted pds) {
                handleProjectDiscoveryStarted(pds);
            } else if (event instanceof AnalysisProgressEvents.FilesDiscovered fd) {
                handleFilesDiscovered(fd);
            } else if (event instanceof AnalysisProgressEvents.ParsingStarted ps) {
                handleParsingStarted(ps);
            } else if (event instanceof AnalysisProgressEvents.CompilationUnitParsed cup) {
                handleCompilationUnitParsed(cup);
            } else if (event instanceof AnalysisProgressEvents.EntityExtracted ee) {
                handleEntityExtracted(ee);
            } else if (event instanceof AnalysisProgressEvents.ReferenceDatabaseBuilt rdb) {
                handleReferenceDatabaseBuilt(rdb);
            } else if (event instanceof AnalysisProgressEvents.DependencyGraphBuilt dgb) {
                handleDependencyGraphBuilt(dgb);
            } else if (event instanceof AnalysisProgressEvents.GraphIndexBuilt gib) {
                handleGraphIndexBuilt(gib);
            } else if (event instanceof AnalysisProgressEvents.TechnologyDetectionStarted tds) {
                handleTechnologyDetectionStarted(tds);
            } else if (event instanceof AnalysisProgressEvents.MetricsComputed mc) {
                handleMetricsComputed(mc);
            } else if (event instanceof AnalysisProgressEvents.AnalysisCompleted ac) {
                handleAnalysisCompleted(ac);
            } else if (event instanceof AnalysisProgressEvents.AnalysisFailed af) {
                handleAnalysisFailed(af);
            }
        });
    }

    private void handleProjectDiscoveryStarted(AnalysisProgressEvents.ProjectDiscoveryStarted event) {
        if (windowStack == null) return;
        model.reset();

        String projName = event.projectPath().getFileName() != null 
                ? event.projectPath().getFileName().toString() 
                : event.projectPath().toString();

        currentView = new LoadingOverlayView(model, projName);
        currentView.setOpacity(0.0);

        if (!windowStack.getChildren().contains(currentView)) {
            windowStack.getChildren().add(currentView);
        }

        // Apply Blur to Main Layout
        BoxBlur blur = new BoxBlur(0, 0, 3);
        mainContent.setEffect(blur);

        // Animate Blur and Fade In Overlay
        FadeTransition scrimFade = new FadeTransition(Duration.millis(200), currentView);
        scrimFade.setFromValue(0.0);
        scrimFade.setToValue(1.0);

        Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(blur.widthProperty(), 0), new KeyValue(blur.heightProperty(), 0)),
                new KeyFrame(Duration.millis(200), new KeyValue(blur.widthProperty(), 10), new KeyValue(blur.heightProperty(), 10))
        );

        ParallelTransition anim = new ParallelTransition(scrimFade, blurTimeline);
        anim.play();

        model.currentStageNameProperty().set("Discover Project");
        model.stageProgressProperty().set(-1.0);
        model.overallProgressProperty().set(0.05);
        model.addFeedMessage("• Discovering project roots...");
    }

    private void handleFilesDiscovered(AnalysisProgressEvents.FilesDiscovered event) {
        model.totalFilesProperty().set(event.files().size());
        model.currentStageNameProperty().set("Scan Files");
        currentView.getPipelineView().setStage("Scan Files");
        model.overallProgressProperty().set(0.12);
        model.addFeedMessage(String.format("✓ Scanned directory: %d source files found", event.files().size()));
    }

    private void handleParsingStarted(AnalysisProgressEvents.ParsingStarted event) {
        model.currentStageNameProperty().set("Parse Sources");
        currentView.getPipelineView().setStage("Parse Sources");
        model.overallProgressProperty().set(0.15);
        model.addFeedMessage("• Compiling syntax tree parsing...");
    }

    private void handleCompilationUnitParsed(AnalysisProgressEvents.CompilationUnitParsed event) {
        model.parsedFilesProperty().set(event.parsedCount());
        model.currentFileNameProperty().set(event.name());

        double parseFraction = (double) event.parsedCount() / event.totalFiles();
        model.stageProgressProperty().set(parseFraction);

        // Map overall parsing stage progress (allocating 15% -> 45%)
        double overall = 0.15 + (parseFraction * 0.30);
        model.overallProgressProperty().set(overall);

        if (event.parsedCount() == 1 || event.parsedCount() == event.totalFiles() || event.parsedCount() % 8 == 0) {
            model.addFeedMessage(String.format("✓ Parsed: %s", event.name()));
        }
    }

    private void handleEntityExtracted(AnalysisProgressEvents.EntityExtracted event) {
        model.totalEntitiesProperty().set(event.count());
        model.currentStageNameProperty().set("Resolve Symbols");
        currentView.getPipelineView().setStage("Resolve Symbols");
        
        // Map overall extracting stage progress (allocating 45% -> 60%)
        double progress = 0.45 + Math.min(0.15, (event.count() / 400.0) * 0.15);
        model.overallProgressProperty().set(progress);

        if (event.count() == 1 || event.count() % 12 == 0) {
            model.addFeedMessage(String.format("✓ Extracted Entity: %s", event.entityName()));
        }
    }

    private void handleReferenceDatabaseBuilt(AnalysisProgressEvents.ReferenceDatabaseBuilt event) {
        model.totalReferencesProperty().set(event.totalReferences());
        model.currentStageNameProperty().set("Build Reference Database");
        currentView.getPipelineView().setStage("Build Reference Database");
        model.overallProgressProperty().set(0.70);
        model.addFeedMessage(String.format("✓ Indexed %d semantic references", event.totalReferences()));
    }

    private void handleDependencyGraphBuilt(AnalysisProgressEvents.DependencyGraphBuilt event) {
        model.totalDependenciesProperty().set(event.totalDependencies());
        model.currentStageNameProperty().set("Build Dependency Graph");
        currentView.getPipelineView().setStage("Build Dependency Graph");
        model.overallProgressProperty().set(0.80);
        model.addFeedMessage(String.format("✓ Dependency graph built (%d links)", event.totalDependencies()));
    }

    private void handleGraphIndexBuilt(AnalysisProgressEvents.GraphIndexBuilt event) {
        model.currentStageNameProperty().set("Build Graph Index");
        currentView.getPipelineView().setStage("Build Graph Index");
        model.overallProgressProperty().set(0.85);
        model.addFeedMessage("✓ Strongly Connected Components (SCC) resolved");
    }

    private void handleTechnologyDetectionStarted(AnalysisProgressEvents.TechnologyDetectionStarted event) {
        model.currentStageNameProperty().set("Detect Technologies");
        currentView.getPipelineView().setStage("Detect Technologies");
        model.overallProgressProperty().set(0.90);
        model.addFeedMessage("• Scanning technology registry signatures...");
    }

    private void handleMetricsComputed(AnalysisProgressEvents.MetricsComputed event) {
        model.currentStageNameProperty().set("Compute Metrics");
        currentView.getPipelineView().setStage("Compute Metrics");
        model.overallProgressProperty().set(0.95);
        model.addFeedMessage(String.format("✓ Metrics computed for %d entities", event.totalEntities()));
    }

    private void handleAnalysisCompleted(AnalysisProgressEvents.AnalysisCompleted event) {
        model.totalFilesProperty().set(event.totalFiles());
        model.totalEntitiesProperty().set(event.totalEntities());
        model.totalReferencesProperty().set(event.totalReferences());
        model.totalNamespacesProperty().set(event.totalNamespaces());
        model.totalFrameworksProperty().set(event.totalFrameworks());
        model.totalMetricsProperty().set(event.totalMetrics());

        model.currentStageNameProperty().set("Finalize Analysis");
        currentView.getPipelineView().completeAll();
        model.stageProgressProperty().set(1.0);
        model.overallProgressProperty().set(1.0);
        model.completedProperty().set(true);

        model.addFeedMessage("✓ Analysis completed successfully.");

        // Clean UI removal with transition after 750ms
        Timeline exitTimeline = new Timeline(new KeyFrame(Duration.millis(750), e -> hideOverlay()));
        exitTimeline.play();
    }

    private void handleAnalysisFailed(AnalysisProgressEvents.AnalysisFailed event) {
        model.failedProperty().set(true);
        model.errorMessageProperty().set(event.errorMessage());
        model.currentStageNameProperty().set("Analysis Failed");
        model.stageProgressProperty().set(0.0);
        model.addFeedMessage("✖ Error: " + event.errorMessage());
    }

    public void hideOverlay() {
        if (currentView == null || windowStack == null) return;

        BoxBlur blur = (BoxBlur) mainContent.getEffect();

        // Closing animations (200ms)
        FadeTransition scrimFade = new FadeTransition(Duration.millis(200), currentView);
        scrimFade.setToValue(0.0);

        Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.millis(200), 
                        new KeyValue(blur.widthProperty(), 0), 
                        new KeyValue(blur.heightProperty(), 0))
        );

        ParallelTransition exitAnim = new ParallelTransition(scrimFade, blurTimeline);
        exitAnim.setOnFinished(e -> {
            windowStack.getChildren().remove(currentView);
            mainContent.setEffect(null);
            currentView = null;
        });
        exitAnim.play();
    }
}
