package com.example.bodhak.ui.analysisReport.risk;

import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import com.example.bodhak.ui.analysisReport.state.RiskHotspotsState;
import com.example.bodhak.ui.analysisReport.state.RiskHotspotsState.RiskEntry;
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
 * Concrete inspector for Risk Hotspots.
 */
public class RiskHotspotsInspector extends AnalysisInspectorContent {

    private final TableView<RiskEntry> table = new TableView<>();
    private RiskHotspotsState state;

    public RiskHotspotsInspector() {
        setSpacing(16);
        setPadding(new Insets(10));

        Label label = new Label("ALL PROJECT RISK HOTSPOTS");
        label.getStyleClass().add("ar-label-tiny");

        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(label, table);
    }

    @Override
    public String getTitle() {
        return "Risk Hotspots";
    }

    @Override
    public Node getIcon() {
        Label icon = new Label("warning");
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 24px; -fx-text-fill: #ff9956;");
        return icon;
    }

    @Override
    public String getConfidenceText() {
        return null;
    }

    @Override
    public void bindToState(AnalysisReportState state) {
        this.state = state.getRiskHotspotsState();
        table.setItems(this.state.getHotspots());
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        table.getStyleClass().add("ar-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(500);

        TableColumn<RiskEntry, String> entityCol = new TableColumn<>("ENTITY");
        entityCol.setCellValueFactory(new PropertyValueFactory<>("entity"));
        entityCol.setMinWidth(200);
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

        TableColumn<RiskEntry, String> riskCol = new TableColumn<>("RISK LEVEL");
        riskCol.setCellValueFactory(new PropertyValueFactory<>("riskLevel"));
        riskCol.setMinWidth(100);
        riskCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("HIGH".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ff4b4b; -fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold;");
                    } else if ("MEDIUM".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #fec931; -fx-font-family: 'JetBrains Mono'; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #56d69b; -fx-font-family: 'JetBrains Mono';");
                    }
                }
            }
        });

        TableColumn<RiskEntry, String> flagsCol = new TableColumn<>("ACTIVE FLAGS");
        flagsCol.setCellValueFactory(new PropertyValueFactory<>("activeFlags"));
        flagsCol.setMinWidth(250);
        flagsCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.replace("_", " "));
                    setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 11px; -fx-text-fill: #849494;");
                }
            }
        });

        TableColumn<RiskEntry, Integer> locCol = new TableColumn<>("LOC");
        locCol.setCellValueFactory(new PropertyValueFactory<>("loc"));
        locCol.setMinWidth(80);
        locCol.setStyle("-fx-alignment: CENTER-RIGHT;");
        locCol.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(NumberFormat.getInstance().format(item));
                setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'JetBrains Mono'; -fx-font-size: 12px; -fx-text-fill: #bac9cc;");
            }
        });

        table.getColumns().addAll(entityCol, riskCol, flagsCol, locCol);

        table.setRowFactory(tv -> {
            TableRow<RiskEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }
}
