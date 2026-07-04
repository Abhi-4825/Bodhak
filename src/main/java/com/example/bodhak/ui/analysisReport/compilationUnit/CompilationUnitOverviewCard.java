package com.example.bodhak.ui.analysisReport.compilationUnit;

import com.example.bodhak.ui.analysisReport.state.CompilationUnitOverviewState;
import com.example.bodhak.ui.analysisReport.uiComponent.DashboardCard;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class CompilationUnitOverviewCard extends VBox {

    private final CompilationUnitOverviewState state;
    private final DashboardCard card;
    private final TableView<CompilationUnitOverviewState.CompilationUnitEntry> table = new TableView<>();

    public CompilationUnitOverviewCard(CompilationUnitOverviewState state) {
        this.state = state;
        this.card = new DashboardCard("Largest Compilation Units", new Label("📄"));
        initialise();
    }

    private void initialise() {
        getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        table.setItems(state.getCompilationUnits());
        table.setPrefHeight(250);
        
        table.widthProperty().addListener((obs, old, newWidth) -> {
            for (TableColumn<?, ?> column : table.getColumns()) {
                column.setReorderable(false);
            }
        });

        TableColumn<CompilationUnitOverviewState.CompilationUnitEntry, String> fileCol = new TableColumn<>("File");
        fileCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().file()));
        fileCol.setPrefWidth(250);

        TableColumn<CompilationUnitOverviewState.CompilationUnitEntry, Integer> locCol = new TableColumn<>("LOC");
        locCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().loc()).asObject());
        locCol.setPrefWidth(100);

        TableColumn<CompilationUnitOverviewState.CompilationUnitEntry, Integer> entitiesCol = new TableColumn<>("Entities");
        entitiesCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().entities()).asObject());
        entitiesCol.setPrefWidth(100);

        TableColumn<CompilationUnitOverviewState.CompilationUnitEntry, Integer> compCol = new TableColumn<>("Complexity");
        compCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().complexity()).asObject());
        compCol.setPrefWidth(100);

        table.getColumns().addAll(fileCol, locCol, entitiesCol, compCol);
        table.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

        card.setContent(table);
    }
}
