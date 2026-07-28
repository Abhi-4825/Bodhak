package com.example.anuviya.ui.analysisReport.diagnostic;

import com.example.anuviya.ui.analysisReport.state.AnalysisReportState;
import com.example.anuviya.ui.analysisReport.state.DiagnosticsSummaryState;
import com.example.anuviya.ui.analysisReport.state.DiagnosticsSummaryState.DiagnosticEntry;
import com.example.anuviya.ui.analysisReport.uiComponent.AnalysisInspectorContent;
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
 * Concrete inspector for Live Diagnostics.
 */
public class DiagnosticsInspector extends AnalysisInspectorContent {

    private final TableView<DiagnosticEntry> table = new TableView<>();
    private DiagnosticsSummaryState state;

    public DiagnosticsInspector() {
        setSpacing(16);
        setPadding(new Insets(10));

        Label label = new Label("ALL COMPILER DIAGNOSTIC MESSAGES");
        label.getStyleClass().add("ar-label-tiny");

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(label, table);
    }

    @Override
    public String getTitle() {
        return "Live Diagnostics";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("bug_report");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #ff4b4b;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getDiagnosticsSummaryState();
        table.setItems(this.state.getDiagnostics());
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        table.getStyleClass().add("ar-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(500);

        TableColumn<DiagnosticEntry, String> severityCol = new TableColumn<>("SEVERITY");
        severityCol.setCellValueFactory(new PropertyValueFactory<>("severity"));
        severityCol.setMinWidth(100);
        severityCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    if ("ERROR".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ff4b4b; -fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #fec931; -fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold;");
                    }
                }
            }
        });

        TableColumn<DiagnosticEntry, String> msgCol = new TableColumn<>("MESSAGE");
        msgCol.setCellValueFactory(new PropertyValueFactory<>("message"));
        msgCol.setMinWidth(300);
        msgCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-family: 'Inter'; -fx-font-size: 13px; -fx-text-fill: #dce3ec;");
                }
            }
        });

        TableColumn<DiagnosticEntry, String> fileCol = new TableColumn<>("FILE");
        fileCol.setCellValueFactory(new PropertyValueFactory<>("file"));
        fileCol.setMinWidth(150);
        fileCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #849494;");
                }
            }
        });

        TableColumn<DiagnosticEntry, Integer> lineCol = new TableColumn<>("LINE");
        lineCol.setCellValueFactory(new PropertyValueFactory<>("line"));
        lineCol.setMinWidth(80);
        lineCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        lineCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item <= 0) { setText(null); return; }
                setText(NumberFormat.getInstance().format(item));
                setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #bac9cc;");
            }
        });

        table.getColumns().addAll(severityCol, msgCol, fileCol, lineCol);

        table.setRowFactory(tv -> {
            TableRow<DiagnosticEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }
}
