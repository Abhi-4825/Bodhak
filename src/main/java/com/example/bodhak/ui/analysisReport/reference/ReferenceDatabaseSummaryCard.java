package com.example.bodhak.ui.analysisReport.reference;

import com.example.bodhak.ui.analysisReport.state.ReferenceDatabaseSummaryState;
import com.example.bodhak.ui.analysisReport.uiComponent.DashboardCard;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ReferenceDatabaseSummaryCard extends VBox {

    private final ReferenceDatabaseSummaryState state;
    private final DashboardCard card;
    private final TableView<ReferenceDatabaseSummaryState.ReferenceSummaryEntry> table = new TableView<>();

    public ReferenceDatabaseSummaryCard(ReferenceDatabaseSummaryState state) {
        this.state = state;
        this.card = new DashboardCard("Reference Database Summary", new Label("🔗"));
        initialise();
    }

    private void initialise() {
        getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        table.setItems(state.getSummaries());
        table.setPrefHeight(250);
        
        table.widthProperty().addListener((obs, old, newWidth) -> {
            for (TableColumn<?, ?> column : table.getColumns()) {
                column.setReorderable(false);
            }
        });

        TableColumn<ReferenceDatabaseSummaryState.ReferenceSummaryEntry, String> kindCol = new TableColumn<>("Reference Kind");
        kindCol.setCellValueFactory(new PropertyValueFactory<>("kind"));
        kindCol.setPrefWidth(200);

        TableColumn<ReferenceDatabaseSummaryState.ReferenceSummaryEntry, Integer> usesCol = new TableColumn<>("Total Uses");
        usesCol.setCellValueFactory(new PropertyValueFactory<>("totalUses"));
        usesCol.setPrefWidth(120);

        TableColumn<ReferenceDatabaseSummaryState.ReferenceSummaryEntry, Long> sourcesCol = new TableColumn<>("Distinct Sources");
        sourcesCol.setCellValueFactory(new PropertyValueFactory<>("distinctSources"));
        sourcesCol.setPrefWidth(150);

        TableColumn<ReferenceDatabaseSummaryState.ReferenceSummaryEntry, Long> targetsCol = new TableColumn<>("Distinct Targets");
        targetsCol.setCellValueFactory(new PropertyValueFactory<>("distinctTargets"));
        targetsCol.setPrefWidth(150);

        table.getColumns().addAll(kindCol, usesCol, sourcesCol, targetsCol);
        table.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

        card.setContent(table);
    }
}
