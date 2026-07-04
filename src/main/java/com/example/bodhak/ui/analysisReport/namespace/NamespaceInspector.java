package com.example.bodhak.ui.analysisReport.namespace;

import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import com.example.bodhak.ui.analysisReport.state.NamespaceOverviewState;
import com.example.bodhak.ui.analysisReport.state.NamespaceOverviewState.NamespaceEntry;
import com.example.bodhak.ui.analysisReport.uiComponent.AnalysisInspectorContent;
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
 * Concrete inspector for Namespace Overview.
 */
public class NamespaceInspector extends AnalysisInspectorContent {

    private final TableView<NamespaceEntry> table = new TableView<>();
    private NamespaceOverviewState state;

    public NamespaceInspector() {
        setSpacing(16);
        setPadding(new Insets(10));

        Label label = new Label("ALL DETECTED PROJECT NAMESPACES");
        label.getStyleClass().add("ar-label-tiny");

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(label, table);
    }

    @Override
    public String getTitle() {
        return "Namespace Overview";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("lan");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #00daf3;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getNamespaceOverviewState();
        table.setItems(this.state.getNamespaces());
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        table.getStyleClass().add("ar-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(500);

        TableColumn<NamespaceEntry, String> nsCol = new TableColumn<>("NAMESPACE");
        nsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().name()));
        nsCol.setMinWidth(250);
        nsCol.setCellFactory(col -> new TableCell<>() {
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

        TableColumn<NamespaceEntry, Integer> entCol = numCol("ENTITIES", "entitiesCount", 100);
        TableColumn<NamespaceEntry, Long> locCol = locColumn();
        TableColumn<NamespaceEntry, Integer> foCol = numCol("FAN-OUT", "fanOut", 90);
        TableColumn<NamespaceEntry, Integer> fiCol = numCol("FAN-IN", "fanIn", 90);

        table.getColumns().addAll(nsCol, entCol, locCol, foCol, fiCol);

        table.setRowFactory(tv -> {
            TableRow<NamespaceEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }

    private TableColumn<NamespaceEntry, Integer> numCol(String header, String property, double minW) {
        TableColumn<NamespaceEntry, Integer> col = new TableColumn<>(header);
        if ("entitiesCount".equals(property)) {
            col.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().entitiesCount()).asObject());
        } else if ("fanOut".equals(property)) {
            col.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().fanOut()).asObject());
        } else if ("fanIn".equals(property)) {
            col.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().fanIn()).asObject());
        }
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

    private TableColumn<NamespaceEntry, Long> locColumn() {
        TableColumn<NamespaceEntry, Long> col = new TableColumn<>("LOC");
        col.setCellValueFactory(cellData -> new javafx.beans.property.SimpleLongProperty(cellData.getValue().loc()).asObject());
        col.setMinWidth(100);
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(NumberFormat.getInstance().format(item));
                setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #bac9cc;");
            }
        });
        return col;
    }
}
