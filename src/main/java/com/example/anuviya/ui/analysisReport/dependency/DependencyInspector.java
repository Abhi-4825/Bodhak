package com.example.anuviya.ui.analysisReport.dependency;

import com.example.anuviya.ui.analysisReport.state.AnalysisReportState;
import com.example.anuviya.ui.analysisReport.state.DependencyOverviewState;
import com.example.anuviya.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;

/**
 * Concrete inspector for Dependency Overview.
 */
public class DependencyInspector extends AnalysisInspectorContent {

    private final TableView<DependencyOverviewState.CoupledEntityEntry> coupledTable = new TableView<>();
    private final TableView<DependencyOverviewState.CycleEntry> cyclesTable = new TableView<>();
    private DependencyOverviewState state;

    public DependencyInspector() {
        setSpacing(24);
        setPadding(new Insets(10));

        // Coupled Section
        VBox coupledBox = new VBox(8);
        Label coupledLabel = new Label("HIGHLY COUPLED ENTITIES");
        coupledLabel.getStyleClass().add("ar-label-tiny");
        setupCoupledTable();
        coupledBox.getChildren().addAll(coupledLabel, coupledTable);
        VBox.setVgrow(coupledTable, Priority.ALWAYS);

        // Cycles Section
        VBox cyclesBox = new VBox(8);
        Label cyclesLabel = new Label("KNOWN DEPENDENCY CYCLES");
        cyclesLabel.getStyleClass().add("ar-label-tiny");
        cyclesLabel.setStyle("-fx-text-fill: #ff5252;");
        setupCyclesTable();
        cyclesBox.getChildren().addAll(cyclesLabel, cyclesTable);
        VBox.setVgrow(cyclesTable, Priority.ALWAYS);

        getChildren().addAll(coupledBox, cyclesBox);
    }

    @Override
    public String getTitle() {
        return "Dependency Overview";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("🕸️");
        icon.setStyle("-fx-font-size: 24px;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getDependencyOverviewState();
        coupledTable.setItems(this.state.getCoupledEntities());
        cyclesTable.setItems(this.state.getCycleGroups());
    }

    @SuppressWarnings("unchecked")
    private void setupCoupledTable() {
        coupledTable.getStyleClass().add("ar-table");
        coupledTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        coupledTable.setPrefHeight(230);

        TableColumn<DependencyOverviewState.CoupledEntityEntry, String> entityCol = new TableColumn<>("ENTITY");
        entityCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().entity()));
        entityCol.setMinWidth(300);
        entityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().setAll("table-cell", "ar-table-namespace-cell");
                }
            }
        });

        TableColumn<DependencyOverviewState.CoupledEntityEntry, Integer> fanOutCol = numCol("FAN-OUT", DependencyOverviewState.CoupledEntityEntry::fanOut, 100);
        TableColumn<DependencyOverviewState.CoupledEntityEntry, Integer> fanInCol = numCol("FAN-IN", DependencyOverviewState.CoupledEntityEntry::fanIn, 100);

        coupledTable.getColumns().addAll(entityCol, fanOutCol, fanInCol);

        coupledTable.setRowFactory(tv -> {
            TableRow<DependencyOverviewState.CoupledEntityEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }

    @SuppressWarnings("unchecked")
    private void setupCyclesTable() {
        cyclesTable.getStyleClass().add("ar-table");
        cyclesTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        cyclesTable.setPrefHeight(200);

        TableColumn<DependencyOverviewState.CycleEntry, String> groupCol = new TableColumn<>("CYCLE GROUP");
        groupCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().mainEntity()));
        groupCol.setMinWidth(300);
        groupCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #ff5252;");
                }
            }
        });

        TableColumn<DependencyOverviewState.CycleEntry, Integer> sizeCol = numCol("GROUP SIZE", DependencyOverviewState.CycleEntry::groupSize, 100);
        TableColumn<DependencyOverviewState.CycleEntry, Integer> fanOutCol = numCol("FAN-OUT", DependencyOverviewState.CycleEntry::fanOut, 100);

        cyclesTable.getColumns().addAll(groupCol, sizeCol, fanOutCol);

        cyclesTable.setRowFactory(tv -> {
            TableRow<DependencyOverviewState.CycleEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }

    private <S> TableColumn<S, Integer> numCol(String header, java.util.function.Function<S, Integer> extractor, double minW) {
        TableColumn<S, Integer> col = new TableColumn<>(header);
        col.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(extractor.apply(cellData.getValue())).asObject());
        col.setMinWidth(minW);
        col.setStyle("-fx-alignment: CENTER-RIGHT;");
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(NumberFormat.getInstance().format(item));
                setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #bac9cc;");
            }
        });
        return col;
    }
}
