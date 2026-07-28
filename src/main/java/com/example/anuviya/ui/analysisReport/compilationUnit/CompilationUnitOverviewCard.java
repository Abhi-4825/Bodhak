package com.example.anuviya.ui.analysisReport.compilationUnit;

import com.example.anuviya.ui.analysisReport.state.CompilationUnitOverviewState;
import com.example.anuviya.ui.analysisReport.uiComponent.DashboardCard;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableRow;
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

        table.getStyleClass().add("ar-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setItems(state.getCompilationUnits());
        table.setPrefHeight(250);
        
        table.widthProperty().addListener((obs, old, newWidth) -> {
            for (TableColumn<?, ?> column : table.getColumns()) {
                column.setReorderable(false);
            }
        });

        table.setRowFactory(tv -> {
            TableRow<CompilationUnitOverviewState.CompilationUnitEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });

        TableColumn<CompilationUnitOverviewState.CompilationUnitEntry, String> fileCol = new TableColumn<>("File");
        fileCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().file()));
        fileCol.setPrefWidth(250);
        fileCol.setCellFactory(col -> new TableCell<>() {
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

        TableColumn<CompilationUnitOverviewState.CompilationUnitEntry, Integer> locCol = new TableColumn<>("LOC");
        locCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().loc()).asObject());
        locCol.setPrefWidth(100);
        locCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        locCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(java.text.NumberFormat.getInstance().format(item));
                setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #bac9cc;");
            }
        });

        TableColumn<CompilationUnitOverviewState.CompilationUnitEntry, Integer> entitiesCol = new TableColumn<>("Entities");
        entitiesCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().entities()).asObject());
        entitiesCol.setPrefWidth(100);
        entitiesCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        entitiesCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(java.text.NumberFormat.getInstance().format(item));
                setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #bac9cc;");
            }
        });

        TableColumn<CompilationUnitOverviewState.CompilationUnitEntry, Integer> compCol = new TableColumn<>("Complexity");
        compCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().complexity()).asObject());
        compCol.setPrefWidth(100);
        compCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        compCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(java.text.NumberFormat.getInstance().format(item));
                setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #bac9cc;");
            }
        });

        table.getColumns().addAll(fileCol, locCol, entitiesCol, compCol);
        table.setStyle("-fx-background-color: transparent; -fx-padding: 10;");

        card.setContent(table);
    }
}
