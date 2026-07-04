package com.example.bodhak.ui.analysisReport.compilationUnit;

import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import com.example.bodhak.ui.analysisReport.state.CompilationUnitOverviewState;
import com.example.bodhak.ui.analysisReport.state.CompilationUnitOverviewState.CompilationUnitEntry;
import com.example.bodhak.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;

/**
 * Concrete inspector for Largest Compilation Units.
 */
public class CompilationUnitOverviewInspector extends AnalysisInspectorContent {

    private final TableView<CompilationUnitEntry> table = new TableView<>();
    private CompilationUnitOverviewState state;

    public CompilationUnitOverviewInspector() {
        setSpacing(16);
        setPadding(new Insets(10));

        Label label = new Label("ALL SOURCE COMPILATION UNITS");
        label.getStyleClass().add("ar-label-tiny");

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(label, table);
    }

    @Override
    public String getTitle() {
        return "Compilation Units";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("article");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #00daf3;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getCompilationUnitOverviewState();
        table.setItems(this.state.getAllCompilationUnits());
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        table.getStyleClass().add("ar-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(500);

        TableColumn<CompilationUnitEntry, String> fileCol = new TableColumn<>("FILE PATH");
        fileCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().file()));
        fileCol.setMinWidth(350);
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

        TableColumn<CompilationUnitEntry, Integer> locCol = numCol("LOC", CompilationUnitEntry::loc, 100);
        TableColumn<CompilationUnitEntry, Integer> entitiesCol = numCol("ENTITIES", CompilationUnitEntry::entities, 100);
        TableColumn<CompilationUnitEntry, Integer> complexityCol = numCol("COMPLEXITY", CompilationUnitEntry::complexity, 120);

        table.getColumns().addAll(fileCol, locCol, entitiesCol, complexityCol);

        table.setRowFactory(tv -> {
            TableRow<CompilationUnitEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }

    private TableColumn<CompilationUnitEntry, Integer> numCol(String header, java.util.function.Function<CompilationUnitEntry, Integer> extractor, double minW) {
        TableColumn<CompilationUnitEntry, Integer> col = new TableColumn<>(header);
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
