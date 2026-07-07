package com.example.bodhak.ui.analysisReport.risk;

import com.example.bodhak.ui.analysisReport.state.RiskHotspotsState;
import com.example.bodhak.ui.analysisReport.state.RiskHotspotsState.OversizedEntry;
import com.example.bodhak.ui.analysisReport.state.RiskHotspotsState.InstableEntry;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

public class RiskHotspotsCard extends VBox {

    private final RiskHotspotsState state;
    private final VBox oversizedSection = new VBox(8);
    private final VBox instableSection = new VBox(8);

    public RiskHotspotsCard(RiskHotspotsState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        getStyleClass().add("ar-glass-card");
        setPadding(new Insets(24));
        setSpacing(24);
        setMaxWidth(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);

        // Header Row for entire card
        HBox overallHeader = new HBox(12);
        overallHeader.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("Risk Hotspots");
        title.getStyleClass().add("ar-section-title");
        overallHeader.getChildren().add(title);

        // 1. Oversized Section Title
        HBox oversizedTitleRow = new HBox(8);
        oversizedTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label oversizedLabel = new Label("Oversized Entities");
        oversizedLabel.setStyle("-fx-text-fill: #00daf3; -fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Epilogue';");
        Label infoIcon1 = new Label("ⓘ");
        infoIcon1.setStyle("-fx-text-fill: #72848a; -fx-font-size: 12px;");
        oversizedTitleRow.getChildren().addAll(oversizedLabel, infoIcon1);

        // 2. Highly Instable Section Title
        HBox instableTitleRow = new HBox(8);
        instableTitleRow.setAlignment(Pos.CENTER_LEFT);
        Label instableLabel = new Label("Highly Instable Entities");
        instableLabel.setStyle("-fx-text-fill: #ff4b4b; -fx-font-weight: bold; -fx-font-size: 14px; -fx-font-family: 'Epilogue';");
        Label infoIcon2 = new Label("ⓘ");
        infoIcon2.setStyle("-fx-text-fill: #72848a; -fx-font-size: 12px;");
        instableTitleRow.getChildren().addAll(instableLabel, infoIcon2);

        // Populate sections
        oversizedSection.setMaxWidth(Double.MAX_VALUE);
        instableSection.setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(
            overallHeader, 
            oversizedTitleRow, oversizedSection, 
            instableTitleRow, instableSection
        );

        // Bind State
        state.getOversizedHotspots().addListener((ListChangeListener<OversizedEntry>) c -> rebuildOversized());
        state.getInstableHotspots().addListener((ListChangeListener<InstableEntry>) c -> rebuildInstable());

        rebuildOversized();
        rebuildInstable();
    }

    private void rebuildOversized() {
        oversizedSection.getChildren().clear();
        var list = state.getOversizedHotspots();

        if (list.isEmpty()) {
            oversizedSection.getChildren().add(createEmptyState("No oversized entities detected"));
            return;
        }

        // Header Grid Row
        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(16);
        headerGrid.setPadding(new Insets(6, 12, 6, 12));
        setupOversizedColumns(headerGrid);

        addHeaderLabel(headerGrid, "#", 0);
        addHeaderLabel(headerGrid, "Entity", 1);
        addHeaderLabel(headerGrid, "Package", 2);
        addHeaderLabel(headerGrid, "LOC", 3);
        addHeaderLabel(headerGrid, "Methods", 4);
        addHeaderLabel(headerGrid, "Fields", 5);

        oversizedSection.getChildren().add(headerGrid);

        // Data Rows
        for (int i = 0; i < list.size(); i++) {
            OversizedEntry entry = list.get(i);
            GridPane rowGrid = new GridPane();
            rowGrid.setHgap(16);
            rowGrid.setPadding(new Insets(10, 12, 10, 12));
            rowGrid.setStyle(
                "-fx-background-color: rgba(18, 24, 31, 0.4); " +
                "-fx-border-color: rgba(26, 38, 48, 0.4); " +
                "-fx-border-width: 0 0 1 0;"
            );
            
            // Hover highlight
            rowGrid.setOnMouseEntered(e -> rowGrid.setStyle(
                "-fx-background-color: rgba(0, 218, 243, 0.03); " +
                "-fx-border-color: rgba(0, 218, 243, 0.15); " +
                "-fx-border-width: 0 0 1 0;"
            ));
            rowGrid.setOnMouseExited(e -> rowGrid.setStyle(
                "-fx-background-color: rgba(18, 24, 31, 0.4); " +
                "-fx-border-color: rgba(26, 38, 48, 0.4); " +
                "-fx-border-width: 0 0 1 0;"
            ));

            setupOversizedColumns(rowGrid);

            // Index
            Label lblIdx = new Label(String.valueOf(i + 1));
            lblIdx.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px;");
            rowGrid.add(lblIdx, 0, 0);

            // Entity Class Name with C Icon
            HBox entityBox = new HBox(8);
            entityBox.setAlignment(Pos.CENTER_LEFT);
            Label cIcon = new Label("C");
            cIcon.setStyle(
                "-fx-text-fill: #00daf3; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 9px; " +
                "-fx-background-color: rgba(0, 218, 243, 0.08); " +
                "-fx-border-color: rgba(0, 218, 243, 0.25); " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-alignment: center; " +
                "-fx-pref-width: 14; " +
                "-fx-pref-height: 14;"
            );
            Label nameLbl = new Label(entry.name());
            nameLbl.setStyle("-fx-text-fill: #e6f1f3; -fx-font-weight: bold; -fx-font-size: 12px; -fx-font-family: 'Epilogue';");
            entityBox.getChildren().addAll(cIcon, nameLbl);
            rowGrid.add(entityBox, 1, 0);

            // Package
            Label pkgLbl = new Label(entry.pkg());
            pkgLbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-family: 'Inter';");
            rowGrid.add(pkgLbl, 2, 0);

            // LOC
            Label locLbl = new Label(String.valueOf(entry.loc()));
            locLbl.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold;");
            rowGrid.add(locLbl, 3, 0);

            // Methods
            Label methodsLbl = new Label(String.valueOf(entry.methodCount()));
            methodsLbl.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
            rowGrid.add(methodsLbl, 4, 0);

            // Fields
            Label fieldsLbl = new Label(String.valueOf(entry.fieldCount()));
            fieldsLbl.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
            rowGrid.add(fieldsLbl, 5, 0);

            oversizedSection.getChildren().add(rowGrid);
        }
    }

    private void rebuildInstable() {
        instableSection.getChildren().clear();
        var list = state.getInstableHotspots();

        if (list.isEmpty()) {
            instableSection.getChildren().add(createEmptyState("No instable entities detected"));
            return;
        }

        // Header Grid Row
        GridPane headerGrid = new GridPane();
        headerGrid.setHgap(16);
        headerGrid.setPadding(new Insets(6, 12, 6, 12));
        setupInstableColumns(headerGrid);

        addHeaderLabel(headerGrid, "#", 0);
        addHeaderLabel(headerGrid, "Entity", 1);
        addHeaderLabel(headerGrid, "Package", 2);
        addHeaderLabel(headerGrid, "Instability", 3);
        addHeaderLabel(headerGrid, "Fan-Out", 4);
        addHeaderLabel(headerGrid, "Fan-In", 5);
        addHeaderLabel(headerGrid, "Stability", 6);

        instableSection.getChildren().add(headerGrid);

        // Data Rows
        for (int i = 0; i < list.size(); i++) {
            InstableEntry entry = list.get(i);
            GridPane rowGrid = new GridPane();
            rowGrid.setHgap(16);
            rowGrid.setPadding(new Insets(10, 12, 10, 12));
            rowGrid.setStyle(
                "-fx-background-color: rgba(18, 24, 31, 0.4); " +
                "-fx-border-color: rgba(26, 38, 48, 0.4); " +
                "-fx-border-width: 0 0 1 0;"
            );

            // Hover highlight
            rowGrid.setOnMouseEntered(e -> rowGrid.setStyle(
                "-fx-background-color: rgba(255, 75, 75, 0.02); " +
                "-fx-border-color: rgba(255, 75, 75, 0.12); " +
                "-fx-border-width: 0 0 1 0;"
            ));
            rowGrid.setOnMouseExited(e -> rowGrid.setStyle(
                "-fx-background-color: rgba(18, 24, 31, 0.4); " +
                "-fx-border-color: rgba(26, 38, 48, 0.4); " +
                "-fx-border-width: 0 0 1 0;"
            ));

            setupInstableColumns(rowGrid);

            // Index
            Label lblIdx = new Label(String.valueOf(i + 1));
            lblIdx.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px;");
            rowGrid.add(lblIdx, 0, 0);

            // Entity Class Name with C Icon
            HBox entityBox = new HBox(8);
            entityBox.setAlignment(Pos.CENTER_LEFT);
            Label cIcon = new Label("C");
            cIcon.setStyle(
                "-fx-text-fill: #ff4b4b; " +
                "-fx-font-weight: bold; " +
                "-fx-font-size: 9px; " +
                "-fx-background-color: rgba(255, 75, 75, 0.08); " +
                "-fx-border-color: rgba(255, 75, 75, 0.25); " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-alignment: center; " +
                "-fx-pref-width: 14; " +
                "-fx-pref-height: 14;"
            );
            Label nameLbl = new Label(entry.name());
            nameLbl.setStyle("-fx-text-fill: #e6f1f3; -fx-font-weight: bold; -fx-font-size: 12px; -fx-font-family: 'Epilogue';");
            entityBox.getChildren().addAll(cIcon, nameLbl);
            rowGrid.add(entityBox, 1, 0);

            // Package
            Label pkgLbl = new Label(entry.pkg());
            pkgLbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-family: 'Inter';");
            rowGrid.add(pkgLbl, 2, 0);

            // Instability Value with Red Bar Glow
            HBox instBox = new HBox(8);
            instBox.setAlignment(Pos.CENTER_LEFT);
            Label instValue = new Label(String.format("%.2f", entry.instability()));
            instValue.setStyle("-fx-text-fill: #ff4b4b; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-font-weight: bold;");
            
            // Faint red line glow
            Region line = new Region();
            line.setPrefSize(40, 4);
            line.setStyle("-fx-background-color: #ff4b4b; -fx-background-radius: 2; -fx-opacity: 0.8;");
            instBox.getChildren().addAll(instValue, line);
            rowGrid.add(instBox, 3, 0);

            // Fan-Out
            Label fanOutLbl = new Label(String.valueOf(entry.fanOut()));
            fanOutLbl.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
            rowGrid.add(fanOutLbl, 4, 0);

            // Fan-In
            Label fanInLbl = new Label(String.valueOf(entry.fanIn()));
            fanInLbl.setStyle("-fx-text-fill: #bac9cc; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px;");
            rowGrid.add(fanInLbl, 5, 0);

            // Stability Badge
            Label stabilityBadge = new Label(entry.stability().toUpperCase());
            String color = entry.stability().equalsIgnoreCase("Very Low") ? "#ff4b4b" : "#e67e22";
            stabilityBadge.setStyle(
                "-fx-text-fill: " + color + "; " +
                "-fx-background-color: " + color + "1A; " +
                "-fx-border-color: " + color + "33; " +
                "-fx-border-width: 1; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-padding: 3 8; " +
                "-fx-font-size: 8px; " +
                "-fx-font-weight: bold; " +
                "-fx-font-family: 'Inter';"
            );
            rowGrid.add(stabilityBadge, 6, 0);

            instableSection.getChildren().add(rowGrid);
        }
    }

    private void setupOversizedColumns(GridPane grid) {
        ColumnConstraints c0 = new ColumnConstraints(); c0.setPrefWidth(20);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(120); c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPrefWidth(200); c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPrefWidth(60);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPrefWidth(60);
        ColumnConstraints c5 = new ColumnConstraints(); c5.setPrefWidth(60);
        grid.getColumnConstraints().setAll(c0, c1, c2, c3, c4, c5);
    }

    private void setupInstableColumns(GridPane grid) {
        ColumnConstraints c0 = new ColumnConstraints(); c0.setPrefWidth(20);
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPrefWidth(120); c1.setHgrow(Priority.ALWAYS);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPrefWidth(200); c2.setHgrow(Priority.ALWAYS);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPrefWidth(90);
        ColumnConstraints c4 = new ColumnConstraints(); c4.setPrefWidth(60);
        ColumnConstraints c5 = new ColumnConstraints(); c5.setPrefWidth(60);
        ColumnConstraints c6 = new ColumnConstraints(); c6.setPrefWidth(80);
        grid.getColumnConstraints().setAll(c0, c1, c2, c3, c4, c5, c6);
    }

    private void addHeaderLabel(GridPane grid, String text, int col) {
        Label lbl = new Label(text.toUpperCase());
        lbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 9px; -fx-font-weight: bold; -fx-font-family: 'Inter';");
        grid.add(lbl, col, 0);
    }

    private Node createEmptyState(String text) {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(16));
        box.setStyle(
            "-fx-background-color: rgba(18, 24, 31, 0.2); " +
            "-fx-border-color: rgba(26, 38, 48, 0.3); " +
            "-fx-border-radius: 8; " +
            "-fx-background-radius: 8;"
        );
        Label lbl = new Label(text);
        lbl.setStyle("-fx-text-fill: #72848a; -fx-font-size: 11px; -fx-font-style: italic;");
        box.getChildren().add(lbl);
        return box;
    }
}
