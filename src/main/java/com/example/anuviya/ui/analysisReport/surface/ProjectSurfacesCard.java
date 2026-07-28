package com.example.anuviya.ui.analysisReport.surface;

import com.example.anuviya.ui.analysisReport.state.ProjectSurfacesState;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.*;

public class ProjectSurfacesCard extends VBox {

    private final ProjectSurfacesState state;
    private final FlowPane surfacesGrid = new FlowPane();

    // Top Header components
    private final Label lblTitle = new Label("PROJECT SURFACES");
    private final Label lblSubtitle = new Label("External touchpoints and interaction surfaces detected in your project");
    private final Button btnDetails = new Button("View Details ↗");

    // Metrics Overview components
    private final Label valTotal = new Label("0");
    private final Label valDetected = new Label("0");
    private final Label valPlaceholders = new Label("0");
    private final Label valCoverage = new Label("0%");

    public ProjectSurfacesCard(ProjectSurfacesState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        setSpacing(20);
        setPadding(new Insets(24));
        getStyleClass().add("dashboard-card");
        setStyle(
            "-fx-background-color: #0b1015; " +
            "-fx-border-color: #1a2630; " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 12; " +
            "-fx-background-radius: 12;"
        );

        // 1. Build Header Section
        HBox headerBox = new HBox(12);
        headerBox.setAlignment(Pos.CENTER_LEFT);

        // Circular Glowing Target Icon
        Label targetIcon = new Label("🎯");
        targetIcon.setStyle("-fx-font-size: 18px;");
        StackPane iconCircle = new StackPane(targetIcon);
        iconCircle.setPrefSize(36, 36);
        iconCircle.setMaxSize(36, 36);
        iconCircle.setStyle(
            "-fx-background-color: rgba(0, 218, 243, 0.05); " +
            "-fx-border-color: rgba(0, 218, 243, 0.3); " +
            "-fx-border-width: 1.5; " +
            "-fx-border-radius: 18; " +
            "-fx-background-radius: 18; " +
            "-fx-effect: dropshadow(three-pass-box, rgba(0, 218, 243, 0.25), 10, 0.2, 0, 0);"
        );

        VBox titleCol = new VBox(2);
        lblTitle.setStyle("-fx-text-fill: #00daf3; -fx-font-weight: bold; -fx-font-size: 15px; -fx-font-family: 'Epilogue'; -fx-letter-spacing: 0.5;");
        lblSubtitle.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-family: 'Inter';");
        titleCol.getChildren().addAll(lblTitle, lblSubtitle);

        Pane spacer = new Pane();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        btnDetails.setStyle(
            "-fx-background-color: #12181f; " +
            "-fx-text-fill: #9ab2b5; " +
            "-fx-border-color: rgba(154, 178, 181, 0.15); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 6 14; " +
            "-fx-font-size: 11px; " +
            "-fx-font-family: 'Inter'; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        );
        btnDetails.setOnMouseEntered(e -> btnDetails.setStyle(
            "-fx-background-color: #1a232b; " +
            "-fx-text-fill: #00daf3; " +
            "-fx-border-color: rgba(0, 218, 243, 0.4); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 6 14; " +
            "-fx-font-size: 11px; " +
            "-fx-font-family: 'Inter'; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        ));
        btnDetails.setOnMouseExited(e -> btnDetails.setStyle(
            "-fx-background-color: #12181f; " +
            "-fx-text-fill: #9ab2b5; " +
            "-fx-border-color: rgba(154, 178, 181, 0.15); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 6; " +
            "-fx-background-radius: 6; " +
            "-fx-padding: 6 14; " +
            "-fx-font-size: 11px; " +
            "-fx-font-family: 'Inter'; " +
            "-fx-font-weight: bold; " +
            "-fx-cursor: hand;"
        ));
        // Trigger parent card click so it displays inspector
        btnDetails.setOnAction(e -> {
            this.fireEvent(new javafx.scene.input.MouseEvent(
                javafx.scene.input.MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0,
                javafx.scene.input.MouseButton.PRIMARY, 1, true, true, true, true,
                true, true, true, true, true, true, null
            ));
        });

        Button btnOptions = new Button("⋮");
        btnOptions.setStyle("-fx-background-color: transparent; -fx-text-fill: #72848a; -fx-font-size: 16px; -fx-padding: 2 6; -fx-cursor: hand;");

        headerBox.getChildren().addAll(iconCircle, titleCol, spacer, btnDetails, btnOptions);

        // 2. Build Metrics Overview Row
        HBox metricsRow = new HBox();
        metricsRow.setAlignment(Pos.CENTER_LEFT);
        metricsRow.setStyle(
            "-fx-background-color: rgba(18, 24, 31, 0.4); " +
            "-fx-border-color: rgba(26, 38, 48, 0.5); " +
            "-fx-border-width: 1; " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8; " +
            "-fx-padding: 12 16;"
        );

        Node mTotal = createMetricBox("Total Surfaces", valTotal, "🎯", "#00daf3");
        Node mDetected = createMetricBox("Detected", valDetected, "•", "#2ecc71");
        Node mPlaceholders = createMetricBox("Placeholders", valPlaceholders, "•", "#e67e22");
        Node mCoverage = createMetricBox("Coverage", valCoverage, "•", "#9b59b6");

        metricsRow.getChildren().addAll(
            mTotal, createMetricDivider(),
            mDetected, createMetricDivider(),
            mPlaceholders, createMetricDivider(),
            mCoverage
        );

        // 3. Grid Panel configurations
        surfacesGrid.setHgap(16);
        surfacesGrid.setVgap(16);
        surfacesGrid.setAlignment(Pos.TOP_LEFT);

        getChildren().addAll(headerBox, metricsRow, surfacesGrid);

        // Listen to state changes
        state.getSurfaces().addListener((ListChangeListener<ProjectSurfacesState.SurfaceEntry>) c -> rebuildGrid());
        rebuildGrid();
    }

    private Node createMetricBox(String labelText, Label valLabel, String prefixIcon, String colorHex) {
        HBox box = new HBox(12);
        box.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(box, Priority.ALWAYS);

        // Metric circle indicator
        Label icon = new Label(prefixIcon);
        if (prefixIcon.equals("•")) {
            icon.setStyle("-fx-font-size: 32px; -fx-text-fill: " + colorHex + "; -fx-padding: 0 0 4 0;");
        } else {
            icon.setStyle("-fx-font-size: 16px; -fx-text-fill: " + colorHex + ";");
        }
        
        StackPane iconContainer = new StackPane(icon);
        iconContainer.setPrefSize(28, 28);
        iconContainer.setMaxSize(28, 28);
        iconContainer.setAlignment(Pos.CENTER);
        iconContainer.setStyle(
            "-fx-background-color: rgba(255,255,255,0.01); " +
            "-fx-border-radius: 14; " +
            "-fx-background-radius: 14;"
        );

        VBox texts = new VBox(2);
        texts.setAlignment(Pos.CENTER_LEFT);

        valLabel.setStyle("-fx-text-fill: #e6f1f3; -fx-font-weight: bold; -fx-font-size: 18px; -fx-font-family: 'Epilogue';");
        Label lbl = new Label(labelText);
        lbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-family: 'Inter';");
        texts.getChildren().addAll(valLabel, lbl);

        box.getChildren().addAll(iconContainer, texts);
        return box;
    }

    private Node createMetricDivider() {
        Separator s = new Separator(javafx.geometry.Orientation.VERTICAL);
        s.setStyle("-fx-background-color: rgba(26, 38, 48, 0.4); -fx-pref-width: 1; -fx-pref-height: 24; -fx-padding: 0 10;");
        return s;
    }

    private void rebuildGrid() {
        surfacesGrid.getChildren().clear();

        int totalCount = state.getSurfaces().size();
        int detectedCount = 0;
        int placeholderCount = 0;

        for (ProjectSurfacesState.SurfaceEntry entry : state.getSurfaces()) {
            if (entry.detected()) {
                detectedCount++;
            } else {
                placeholderCount++;
            }

            VBox card = new VBox(12);
            card.setAlignment(Pos.TOP_CENTER);
            card.setPadding(new Insets(16));
            card.setPrefSize(148, 164);
            card.setMaxSize(148, 164);
            card.setStyle(
                "-fx-background-color: rgba(18, 24, 31, 0.6); " +
                "-fx-border-color: rgba(26, 38, 48, 0.8); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;"
            );
            
            // Hover effect
            card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: rgba(26, 38, 48, 0.4); " +
                "-fx-border-color: rgba(0, 218, 243, 0.25); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;"
            ));
            card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: rgba(18, 24, 31, 0.6); " +
                "-fx-border-color: rgba(26, 38, 48, 0.8); " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 10; " +
                "-fx-background-radius: 10;"
            ));

            // Status Indicator (top right)
            HBox topRow = new HBox();
            topRow.setAlignment(Pos.CENTER_RIGHT);
            topRow.setPrefWidth(120);

            Label badgeIndicator = new Label();
            if (entry.detected()) {
                badgeIndicator.setText("✓");
                badgeIndicator.setStyle(
                    "-fx-text-fill: #2ecc71; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 8px; " +
                    "-fx-background-color: rgba(46, 204, 113, 0.1); " +
                    "-fx-border-color: rgba(46, 204, 113, 0.3); " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-alignment: center; " +
                    "-fx-pref-width: 12; " +
                    "-fx-pref-height: 12;"
                );
            } else {
                badgeIndicator.setText("•••");
                badgeIndicator.setStyle(
                    "-fx-text-fill: #e67e22; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-size: 5px; " +
                    "-fx-background-color: rgba(230, 126, 34, 0.1); " +
                    "-fx-border-color: rgba(230, 126, 34, 0.3); " +
                    "-fx-border-radius: 6; " +
                    "-fx-background-radius: 6; " +
                    "-fx-alignment: center; " +
                    "-fx-pref-width: 12; " +
                    "-fx-pref-height: 12;"
                );
            }
            topRow.getChildren().add(badgeIndicator);

            // Icon Image or Text loaded dynamically
            Node iconVisual;
            if (entry.imagePath() != null && !entry.imagePath().isEmpty()) {
                try {
                    javafx.scene.image.ImageView img = new javafx.scene.image.ImageView(
                        new javafx.scene.image.Image(entry.imagePath())
                    );
                    img.setFitWidth(28);
                    img.setFitHeight(28);
                    img.setPreserveRatio(true);
                    iconVisual = img;
                } catch (Exception ex) {
                    Label fallbackIcon = new Label(entry.icon());
                    fallbackIcon.setStyle("-fx-font-size: 26px; -fx-text-fill: " + entry.glowColor() + ";");
                    iconVisual = fallbackIcon;
                }
            } else {
                Label mainIcon = new Label(entry.icon());
                mainIcon.setStyle("-fx-font-size: 26px; -fx-text-fill: " + entry.glowColor() + ";");
                iconVisual = mainIcon;
            }

            // Glow Circle Container
            StackPane iconCircle = new StackPane(iconVisual);
            iconCircle.setPrefSize(50, 50);
            iconCircle.setMaxSize(50, 50);
            iconCircle.setStyle(
                "-fx-background-color: rgba(255, 255, 255, 0.02); " +
                "-fx-border-color: " + entry.glowColor() + "1E; " + // 12% opacity border
                "-fx-border-width: 1.5; " +
                "-fx-border-radius: 25; " +
                "-fx-background-radius: 25; " +
                "-fx-effect: dropshadow(three-pass-box, " + entry.glowColor() + "20, 15, 0.25, 0, 0);" // Glow shadow
            );

            // Name
            Label nameLabel = new Label(entry.name());
            nameLabel.setStyle("-fx-text-fill: #e6f1f3; -fx-font-weight: bold; -fx-font-size: 11px; -fx-font-family: 'Epilogue'; -fx-alignment: center;");
            nameLabel.setWrapText(true);
            nameLabel.setAlignment(Pos.CENTER);

            // Pill Badge
            Label statusBadge = new Label();
            if (entry.detected()) {
                statusBadge.setText("DETECTED");
                statusBadge.setStyle(
                    "-fx-text-fill: #2ecc71; " +
                    "-fx-background-color: rgba(46, 204, 113, 0.08); " +
                    "-fx-border-color: rgba(46, 204, 113, 0.18); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 4; " +
                    "-fx-background-radius: 4; " +
                    "-fx-padding: 2 6; " +
                    "-fx-font-size: 7px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Inter';"
                );
            } else {
                statusBadge.setText("PLACEHOLDER");
                statusBadge.setStyle(
                    "-fx-text-fill: #e67e22; " +
                    "-fx-background-color: rgba(230, 126, 34, 0.08); " +
                    "-fx-border-color: rgba(230, 126, 34, 0.18); " +
                    "-fx-border-width: 1; " +
                    "-fx-border-radius: 4; " +
                    "-fx-background-radius: 4; " +
                    "-fx-padding: 2 6; " +
                    "-fx-font-size: 7px; " +
                    "-fx-font-weight: bold; " +
                    "-fx-font-family: 'Inter';"
                );
            }

            // Detail
            Label detailLabel = new Label(entry.detail());
            detailLabel.setStyle("-fx-text-fill: #72848a; -fx-font-size: 9px; -fx-font-family: 'Inter';");

            card.getChildren().addAll(topRow, iconCircle, nameLabel, statusBadge, detailLabel);
            surfacesGrid.getChildren().add(card);
        }

        // Update top metrics
        valTotal.setText(String.valueOf(totalCount));
        valDetected.setText(String.valueOf(detectedCount));
        valPlaceholders.setText(String.valueOf(placeholderCount));
        double coverage = totalCount > 0 ? (double) detectedCount / totalCount * 100 : 0.0;
        valCoverage.setText(String.format("%.0f%%", coverage));
    }
}
