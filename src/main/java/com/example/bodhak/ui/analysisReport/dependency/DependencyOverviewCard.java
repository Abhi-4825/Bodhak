package com.example.bodhak.ui.analysisReport.dependency;

import com.example.bodhak.ui.analysisReport.state.DependencyOverviewState;
import com.example.bodhak.ui.analysisReport.uiComponent.DashboardCard;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class DependencyOverviewCard extends VBox {

    private final DependencyOverviewState state;
    private final DashboardCard card;
    private final VBox contentBox = new VBox(16);

    public DependencyOverviewCard(DependencyOverviewState state) {
        this.state = state;
        this.card = new DashboardCard("Dependency Overview", new Label("🕸️"));
        initialise();
    }

    private void initialise() {
        getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        contentBox.setStyle("-fx-padding: 16;");
        
        // Highly Coupled Entities Table
        Label coupledLabel = new Label("Highly Coupled Entities");
        coupledLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #00e5ff;");
        TableView<DependencyOverviewState.CoupledEntityEntry> coupledTable = new TableView<>();
        setupCoupledTable(coupledTable);

        // Cycles Table
        Label cyclesLabel = new Label("Known Dependency Cycles");
        cyclesLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #ff5252;");
        TableView<DependencyOverviewState.CycleEntry> cyclesTable = new TableView<>();
        setupCyclesTable(cyclesTable);

        contentBox.getChildren().addAll(coupledLabel, coupledTable, cyclesLabel, cyclesTable);
        card.setContent(contentBox);
    }

    private void setupCoupledTable(TableView<DependencyOverviewState.CoupledEntityEntry> table) {
        table.setItems(state.getCoupledEntities());
        table.setPrefHeight(180);
        table.widthProperty().addListener((obs, old, newWidth) -> {
            for (TableColumn<?, ?> column : table.getColumns()) column.setReorderable(false);
        });

        TableColumn<DependencyOverviewState.CoupledEntityEntry, String> entityCol = new TableColumn<>("Entity");
        entityCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().entity()));
        entityCol.setPrefWidth(300);

        TableColumn<DependencyOverviewState.CoupledEntityEntry, Integer> fanOutCol = new TableColumn<>("Fan-Out");
        fanOutCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().fanOut()).asObject());
        fanOutCol.setPrefWidth(100);

        TableColumn<DependencyOverviewState.CoupledEntityEntry, Integer> fanInCol = new TableColumn<>("Fan-In");
        fanInCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().fanIn()).asObject());
        fanInCol.setPrefWidth(100);

        table.getColumns().addAll(entityCol, fanOutCol, fanInCol);
        table.getStyleClass().add("ar-table");
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<DependencyOverviewState.CoupledEntityEntry> row = new javafx.scene.control.TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }

    private void setupCyclesTable(TableView<DependencyOverviewState.CycleEntry> table) {
        table.setItems(state.getCycleGroups());
        table.setPrefHeight(150);
        table.widthProperty().addListener((obs, old, newWidth) -> {
            for (TableColumn<?, ?> column : table.getColumns()) column.setReorderable(false);
        });

        TableColumn<DependencyOverviewState.CycleEntry, String> mainEntityCol = new TableColumn<>("Cycle Group");
        mainEntityCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().mainEntity()));
        mainEntityCol.setPrefWidth(300);

        TableColumn<DependencyOverviewState.CycleEntry, Integer> sizeCol = new TableColumn<>("Group Size");
        sizeCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().groupSize()).asObject());
        sizeCol.setPrefWidth(100);

        TableColumn<DependencyOverviewState.CycleEntry, Integer> fanOutCol = new TableColumn<>("Fan-Out");
        fanOutCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().fanOut()).asObject());
        fanOutCol.setPrefWidth(100);

        table.getColumns().addAll(mainEntityCol, sizeCol, fanOutCol);
        table.getStyleClass().add("ar-table");
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<DependencyOverviewState.CycleEntry> row = new javafx.scene.control.TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }
}
