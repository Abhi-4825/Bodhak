package com.example.bodhak.ui.analysisReport.entity;

import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import com.example.bodhak.ui.analysisReport.state.EntityMetricsState;
import com.example.bodhak.ui.analysisReport.state.EntityMetricsState.EntityEntry;
import com.example.bodhak.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;

/**
 * Concrete inspector for Entity Metrics.
 */
public class EntityMetricsInspector extends AnalysisInspectorContent {

    private final TableView<EntityEntry> table = new TableView<>();
    private final TextField searchField = new TextField();
    private EntityMetricsState state;

    public EntityMetricsInspector() {
        setSpacing(16);
        setPadding(new Insets(10));

        HBox topRow = new HBox(12);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label label = new Label("ALL PROJECT ENTITIES & METRICS");
        label.getStyleClass().add("ar-label-tiny");

        searchField.setPromptText("Search entity name...");
        searchField.setStyle("-fx-background-color: rgba(255,255,255,0.03); -fx-border-color: rgba(0,218,243,0.2); " +
                             "-fx-border-radius: 6; -fx-background-radius: 6; -fx-text-fill: #dce3ec; " +
                             "-fx-font-family: 'Inter'; -fx-font-size: 13px; -fx-padding: 8 12;");
        searchField.setPrefWidth(300);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(label, spacer, searchField);

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(topRow, table);
    }

    @Override
    public String getTitle() {
        return "Entity Metrics";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("description");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #00daf3;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getEntityMetricsState();

        FilteredList<EntityEntry> filteredData = new FilteredList<>(this.state.getAllEntities(), p -> true);

        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(entity -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return entity.name().toLowerCase().contains(lowerCaseFilter) ||
                       entity.kind().toLowerCase().contains(lowerCaseFilter);
            });
        });

        table.setItems(filteredData);
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        table.getStyleClass().add("ar-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(500);

        TableColumn<EntityEntry, String> nameCol = new TableColumn<>("ENTITY NAME");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().name()));
        nameCol.setMinWidth(250);
        nameCol.setCellFactory(col -> new TableCell<>() {
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

        TableColumn<EntityEntry, String> kindCol = new TableColumn<>("KIND");
        kindCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().kind()));
        kindCol.setMinWidth(100);

        TableColumn<EntityEntry, Integer> locCol = numCol("LOC", EntityEntry::loc, 80);
        TableColumn<EntityEntry, Integer> complexityCol = numCol("COMPLEXITY", EntityEntry::complexity, 100);
        TableColumn<EntityEntry, Integer> fanOutCol = numCol("FAN-OUT", EntityEntry::fanOut, 80);
        TableColumn<EntityEntry, Integer> fanInCol = numCol("FAN-IN", EntityEntry::fanIn, 80);

        TableColumn<EntityEntry, String> riskCol = new TableColumn<>("RISK");
        riskCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().risk()));
        riskCol.setMinWidth(85);
        riskCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("High".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ff4b4b; -fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold;");
                    } else if ("Medium".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #fec931; -fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #56d69b; -fx-font-family: 'JetBrains Mono';");
                    }
                }
            }
        });

        table.getColumns().addAll(nameCol, kindCol, locCol, complexityCol, fanOutCol, fanInCol, riskCol);

        table.setRowFactory(tv -> {
            TableRow<EntityEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }

    private TableColumn<EntityEntry, Integer> numCol(String header, java.util.function.Function<EntityEntry, Integer> extractor, double minW) {
        TableColumn<EntityEntry, Integer> col = new TableColumn<>(header);
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
