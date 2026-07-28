package com.example.anuviya.ui.analysisReport.namespace;

import com.example.anuviya.ui.analysisReport.state.NamespaceOverviewState;
import com.example.anuviya.ui.analysisReport.state.NamespaceOverviewState.NamespaceEntry;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Callback;

import java.text.NumberFormat;

/**
 * Namespace Overview Card — Row 3, left panel (7/12 weight).
 *
 * Styled TableView with:
 * - Cyan monospace namespace column
 * - Row hover highlight
 * - No fixed column widths — NAMESPACE column expands, numerics stay compact
 * - Hides itself when no namespace data, right column expands to fill row
 *
 * Contains NO calculations — all data from NamespaceOverviewState.
 */
public class NamespaceOverviewCard extends VBox {

    private final NamespaceOverviewState state;
    private final TableView<NamespaceEntry> table = new TableView<>();

    public NamespaceOverviewCard(NamespaceOverviewState state) {
        this.state = state;
        initialise();
        bindState();
    }

    private void initialise() {
        getStyleClass().add("ar-glass-card");
        setPadding(new Insets(22));
        setSpacing(16);
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);

        // ── Title ──────────────────────────────────────────────────
        Label title = new Label("Namespace Overview");
        title.getStyleClass().add("ar-section-title");

        // ── Table ──────────────────────────────────────────────────
        setupTable();
        VBox.setVgrow(table, Priority.ALWAYS);

        getChildren().addAll(title, table);
    }

    @SuppressWarnings("unchecked")
    private void setupTable() {
        table.getStyleClass().addAll("ar-table", "no-scroll-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPlaceholder(buildEmptyState());
        table.setFixedCellSize(46);
        table.prefHeightProperty().bind(
                table.fixedCellSizeProperty().multiply(
                        Bindings.size(table.getItems()).add(1.05)
                )
        );
        table.setMaxHeight(Double.MAX_VALUE);

        // Namespace column — cyan monospace, expands with long names
        TableColumn<NamespaceEntry, String> nsCol = new TableColumn<>("NAMESPACE");
        nsCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().name()));
        nsCol.setMinWidth(160);
        nsCol.setSortable(false);
        nsCol.setCellFactory(namespaceCellFactory());

        // Numeric columns — compact, right-aligned
        TableColumn<NamespaceEntry, Integer> entCol = numCol("ENTITIES", "entitiesCount", 80);
        TableColumn<NamespaceEntry, Long>    locCol = locColumn();
        TableColumn<NamespaceEntry, Integer> foCol  = numCol("FAN-OUT",  "fanOut",  70);
        TableColumn<NamespaceEntry, Integer> fiCol  = numCol("FAN-IN",   "fanIn",   70);

        table.getColumns().addAll(nsCol, entCol, locCol, foCol, fiCol);
        
        // Limit namespaces to at most 6 items
        updateTableItems();
        state.getNamespaces().addListener((javafx.collections.ListChangeListener<NamespaceEntry>) c -> updateTableItems());

        // Hover row styling via row factory
        table.setRowFactory(tv -> {
            TableRow<NamespaceEntry> row = new TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }

    private void updateTableItems() {
        var all = state.getNamespaces();
        var limited = javafx.collections.FXCollections.<NamespaceEntry>observableArrayList();
        for (int i = 0; i < Math.min(6, all.size()); i++) {
            limited.add(all.get(i));
        }
        table.setItems(limited);
    }

    private Callback<TableColumn<NamespaceEntry, String>, TableCell<NamespaceEntry, String>> namespaceCellFactory() {
        return col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    getStyleClass().setAll("table-cell", "ar-table-namespace-cell");
                }
            }
        };
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
        col.setMaxWidth(minW * 1.5);
        col.setSortable(true);
        col.setStyle("-fx-alignment: CENTER-RIGHT;");
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                setText(NumberFormat.getInstance().format(item));
                setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'JetBrains Mono'; " +
                         "-fx-font-size: 12px; -fx-text-fill: #bac9cc;");
            }
        });
        return col;
    }

    private TableColumn<NamespaceEntry, Long> locColumn() {
        TableColumn<NamespaceEntry, Long> col = new TableColumn<>("LOC");
        col.setCellValueFactory(cellData -> new javafx.beans.property.SimpleLongProperty(cellData.getValue().loc()).asObject());
        col.setMinWidth(80);
        col.setMaxWidth(110);
        col.setSortable(true);
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Long item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); return; }
                String formatted = item >= 1000
                        ? String.format("%,.0f", item / 1.0)
                        : String.valueOf(item);
                setText(formatted);
                setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-family: 'JetBrains Mono'; " +
                         "-fx-font-size: 12px; -fx-text-fill: #bac9cc;");
            }
        });
        return col;
    }

    private Label buildEmptyState() {
        Label lbl = new Label("No namespace data yet");
        lbl.getStyleClass().add("ar-empty-state");
        return lbl;
    }

    private void bindState() {
        // Hide entire card when namespaces is empty; right column expands
        visibleProperty().bind(
                Bindings.createBooleanBinding(
                        () -> !state.getNamespaces().isEmpty(),
                        state.getNamespaces()
                )
        );
        managedProperty().bind(visibleProperty());
    }
}
