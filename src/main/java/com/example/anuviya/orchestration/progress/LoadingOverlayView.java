package com.example.anuviya.orchestration.progress;

import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

public class LoadingOverlayView extends StackPane {

    private final LoadingProgressModel model;
    private final LoadingPipelineView pipelineView = new LoadingPipelineView();
    private final LoadingActivityFeed feedView;

    public LoadingOverlayView(LoadingProgressModel model, String projectName) {
        this.model = model;
        this.feedView = new LoadingActivityFeed(model);

        // Blocking mouse clicks from propagating underneath
        setPickOnBounds(true);
        setStyle("-fx-background-color: rgba(5, 11, 21, 0.65);"); // Darken overlay

        VBox card = new VBox(20);
        card.setMaxSize(820, Region.USE_PREF_SIZE);
        card.setPadding(new Insets(32));
        card.setStyle("-fx-background-color: rgba(13, 20, 26, 0.92); " +
                "-fx-border-color: rgba(0, 229, 255, 0.15); " +
                "-fx-border-radius: 16; " +
                "-fx-background-radius: 16; " +
                "-fx-effect: dropshadow(three-pass-box, rgba(0, 229, 255, 0.15), 32, 0.1, 0, 0);");

        getChildren().add(card);
        setAlignment(Pos.CENTER);

        buildHeader(card, projectName);
        buildProgressBars(card);
        buildContentGrid(card);
    }

    private void buildHeader(VBox container, String projectName) {
        VBox header = new VBox(2);
        header.setAlignment(Pos.CENTER);

        Label title = new Label("ANUVIYA");
        title.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 26px; -fx-font-weight: 900; -fx-text-fill: #00e5ff; -fx-letter-spacing: 0.2em;");

        Label sub = new Label("Understand Code, Build better");
        sub.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 9px; -fx-text-fill: #849396; -fx-letter-spacing: 0.15em;");

        javafx.scene.image.ImageView loadingIcon = com.example.anuviya.ui.helper.IconHelper.createLogoImageView(40);
        if (loadingIcon != null) {
            header.getChildren().add(loadingIcon);
        }
        header.getChildren().addAll(title, sub);

        VBox details = new VBox(4);
        details.setAlignment(Pos.CENTER);
        details.setPadding(new Insets(10, 0, 5, 0));

        Label openingLbl = new Label("Opening Project");
        openingLbl.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 10px; -fx-text-fill: #607274; -fx-letter-spacing: 0.05em;");

        Label projectLbl = new Label(projectName);
        projectLbl.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #c3f5ff;");

        details.getChildren().addAll(openingLbl, projectLbl);

        container.getChildren().addAll(header, createSeparator(), details);
    }

    private void buildProgressBars(VBox container) {
        VBox box = new VBox(12);

        // Overall progress
        HBox overallRow = new HBox(10);
        overallRow.setAlignment(Pos.CENTER_LEFT);
        Label overallTitle = new Label("Overall Progress");
        overallTitle.setStyle("-fx-font-family: 'Inter'; -fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: #dce3ec;");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label overallPercent = new Label();
        overallPercent.textProperty().bind(Bindings.createStringBinding(
            () -> String.format("%.0f%%", model.overallProgressProperty().get() * 100),
            model.overallProgressProperty()
        ));
        overallPercent.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #00e5ff;");
        overallRow.getChildren().addAll(overallTitle, spacer, overallPercent);

        ProgressBar overallBar = new ProgressBar();
        overallBar.setMaxWidth(Double.MAX_VALUE);
        overallBar.progressProperty().bind(model.overallProgressProperty());
        overallBar.setStyle("-fx-accent: #00e5ff; -fx-control-inner-background: rgba(0,0,0,0.3);");

        // Current stage progress
        HBox stageRow = new HBox(10);
        stageRow.setAlignment(Pos.CENTER_LEFT);
        Label stageTitle = new Label();
        stageTitle.textProperty().bind(Bindings.concat("Stage: ", model.currentStageNameProperty()));
        stageTitle.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 10px; -fx-text-fill: #849396;");

        Region spacer2 = new Region();
        HBox.setHgrow(spacer2, Priority.ALWAYS);
        Label currentFile = new Label();
        currentFile.textProperty().bind(model.currentFileNameProperty());
        currentFile.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 10px; -fx-text-fill: #607274;");
        stageRow.getChildren().addAll(stageTitle, spacer2, currentFile);

        ProgressBar stageBar = new ProgressBar();
        stageBar.setMaxWidth(Double.MAX_VALUE);
        stageBar.progressProperty().bind(model.stageProgressProperty());
        stageBar.setStyle("-fx-accent: #fec931; -fx-control-inner-background: rgba(0,0,0,0.3);");

        box.getChildren().addAll(overallRow, overallBar, stageRow, stageBar);
        container.getChildren().addAll(box, createSeparator());

        VBox errorBox = new VBox(8);
        errorBox.setAlignment(Pos.CENTER);
        errorBox.setStyle("-fx-background-color: rgba(255, 75, 75, 0.1); -fx-border-color: #ff4b4b; -fx-border-radius: 8; -fx-padding: 12;");

        Label errorTitle = new Label("ANALYSIS PIPELINE FAILURE");
        errorTitle.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #ff4b4b;");

        Label errorMsg = new Label();
        errorMsg.textProperty().bind(model.errorMessageProperty());
        errorMsg.setWrapText(true);
        errorMsg.setStyle("-fx-font-family: 'Inter'; -fx-font-size: 11px; -fx-text-fill: #bac9cc;");

        javafx.scene.control.Button dismissBtn = new javafx.scene.control.Button("DISMISS ENGINE OVERLAY");
        dismissBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ff4b4b; -fx-text-fill: #ff4b4b; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 16; -fx-background-radius: 6; -fx-border-radius: 6; -fx-cursor: hand;");
        dismissBtn.setOnAction(e -> LoadingOverlayManager.getInstance().hideOverlay());

        dismissBtn.setOnMouseEntered(e -> dismissBtn.setStyle("-fx-background-color: rgba(255, 75, 75, 0.15); -fx-border-color: #ff4b4b; -fx-text-fill: #ff4b4b; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 16; -fx-background-radius: 6; -fx-border-radius: 6;"));
        dismissBtn.setOnMouseExited(e -> dismissBtn.setStyle("-fx-background-color: transparent; -fx-border-color: #ff4b4b; -fx-text-fill: #ff4b4b; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 6 16; -fx-background-radius: 6; -fx-border-radius: 6;"));

        errorBox.getChildren().addAll(errorTitle, errorMsg, dismissBtn);
        errorBox.setVisible(false);
        errorBox.setManaged(false);

        container.getChildren().addAll(errorBox);

        model.failedProperty().addListener((obs, oldVal, failedVal) -> {
            if (failedVal) {
                errorBox.setVisible(true);
                errorBox.setManaged(true);
            }
        });
    }

    private void buildContentGrid(VBox container) {
        HBox columns = new HBox(20);
        VBox.setVgrow(columns, Priority.ALWAYS);

        // Left Column: Compiler Stages View
        pipelineView.setMinWidth(260);
        pipelineView.setPrefWidth(260);

        // Right Column: Stats + Activity Log
        VBox rightColumn = new VBox(16);
        HBox.setHgrow(rightColumn, Priority.ALWAYS);

        GridPane statsGrid = new GridPane();
        statsGrid.setHgap(10);
        statsGrid.setVgap(10);
        for (int i = 0; i < 3; i++) {
            ColumnConstraints cc = new ColumnConstraints();
            cc.setPercentWidth(100.0 / 3.0);
            statsGrid.getColumnConstraints().add(cc);
        }

        statsGrid.add(createStatTile("FILES SCANNED", Bindings.concat(model.parsedFilesProperty(), " / ", model.totalFilesProperty())), 0, 0);
        statsGrid.add(createStatTile("ENTITIES EXTRACTED", model.totalEntitiesProperty().asString()), 1, 0);
        statsGrid.add(createStatTile("NAMESPACES", model.totalNamespacesProperty().asString()), 2, 0);

        statsGrid.add(createStatTile("REFERENCES", model.totalReferencesProperty().asString()), 0, 1);
        statsGrid.add(createStatTile("DEPENDENCIES", model.totalDependenciesProperty().asString()), 1, 1);
        statsGrid.add(createStatTile("FRAMEWORKS", model.totalFrameworksProperty().asString()), 2, 1);

        Label activityLabel = new Label("LIVE COMPILER ACTIVITY");
        activityLabel.setStyle("-fx-font-family: 'Epilogue'; -fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #00e5ff; -fx-letter-spacing: 0.15em;");

        VBox.setVgrow(feedView, Priority.ALWAYS);
        rightColumn.getChildren().addAll(statsGrid, activityLabel, feedView);

        columns.getChildren().addAll(pipelineView, rightColumn);
        container.getChildren().add(columns);
    }

    private VBox createStatTile(String label, javafx.beans.value.ObservableValue<String> propertyValue) {
        VBox tile = new VBox(2);
        tile.setStyle("-fx-background-color: rgba(21, 28, 35, 0.4); " +
                "-fx-padding: 10; " +
                "-fx-background-radius: 8; " +
                "-fx-border-color: rgba(132, 147, 150, 0.05);");

        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 8px; -fx-text-fill: #849396; -fx-letter-spacing: 0.05em;");

        Label lblValue = new Label();
        lblValue.textProperty().bind(propertyValue);
        lblValue.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #00e5ff;");

        tile.getChildren().addAll(lblLabel, lblValue);
        return tile;
    }

    private Separator createSeparator() {
        Separator sep = new Separator();
        sep.setStyle("-fx-background-color: transparent; -fx-border-color: rgba(132, 147, 150, 0.1); -fx-border-width: 1 0 0 0;");
        return sep;
    }

    public LoadingPipelineView getPipelineView() { return pipelineView; }
}
