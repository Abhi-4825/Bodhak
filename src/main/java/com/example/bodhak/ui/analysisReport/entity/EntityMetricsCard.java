package com.example.bodhak.ui.analysisReport.entity;

import com.example.bodhak.ui.analysisReport.state.EntityMetricsState;
import com.example.bodhak.ui.analysisReport.uiComponent.DashboardCard;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class EntityMetricsCard extends VBox {

    private final EntityMetricsState state;
    private final DashboardCard card;
    private final TableView<EntityMetricsState.EntityEntry> table = new TableView<>();

    public EntityMetricsCard(EntityMetricsState state) {
        this.state = state;
        this.card = new DashboardCard("Entity Metrics (Top 5)", new Label("📄"));
        initialise();
    }

    private void initialise() {
        getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        table.setItems(state.getEntities());
        table.setPrefHeight(250);
        
        table.widthProperty().addListener((obs, old, newWidth) -> {
            for (TableColumn<?, ?> column : table.getColumns()) {
                column.setReorderable(false);
            }
        });

        TableColumn<EntityMetricsState.EntityEntry, String> nameCol = new TableColumn<>("Entity");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().name()));
        nameCol.setPrefWidth(200);

        TableColumn<EntityMetricsState.EntityEntry, String> kindCol = new TableColumn<>("Kind");
        kindCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().kind()));
        kindCol.setPrefWidth(80);

        TableColumn<EntityMetricsState.EntityEntry, Integer> locCol = new TableColumn<>("LOC");
        locCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().loc()).asObject());
        locCol.setPrefWidth(80);

        TableColumn<EntityMetricsState.EntityEntry, Integer> compCol = new TableColumn<>("Complexity");
        compCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().complexity()).asObject());
        compCol.setPrefWidth(100);

        TableColumn<EntityMetricsState.EntityEntry, Integer> fanOutCol = new TableColumn<>("Fan-Out");
        fanOutCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().fanOut()).asObject());
        fanOutCol.setPrefWidth(80);

        TableColumn<EntityMetricsState.EntityEntry, Integer> fanInCol = new TableColumn<>("Fan-In");
        fanInCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().fanIn()).asObject());
        fanInCol.setPrefWidth(80);

        TableColumn<EntityMetricsState.EntityEntry, String> riskCol = new TableColumn<>("Risk");
        riskCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().risk()));
        riskCol.setPrefWidth(100);

        table.getColumns().addAll(nameCol, kindCol, locCol, compCol, fanOutCol, fanInCol, riskCol);
        table.getStyleClass().add("ar-table");
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<EntityMetricsState.EntityEntry> row = new javafx.scene.control.TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
        table.setStyle("-fx-padding: 10;");

        card.setContent(table);
    }
}
