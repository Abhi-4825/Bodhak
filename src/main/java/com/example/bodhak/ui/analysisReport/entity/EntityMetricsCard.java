package com.example.bodhak.ui.analysisReport.entity;

import com.example.bodhak.ui.analysisReport.state.EntityMetricsState;
import com.example.bodhak.ui.analysisReport.state.EntityMetricsState.EntityEntry;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;

public class EntityMetricsCard extends VBox {

    private final EntityMetricsState state;
    private final TableView<EntityEntry> table = new TableView<>();

    public EntityMetricsCard(EntityMetricsState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        getStyleClass().add("ar-glass-card");
        setPadding(new Insets(22));
        setSpacing(16);
        setMaxWidth(Double.MAX_VALUE);
        setMaxHeight(Double.MAX_VALUE);
        VBox.setVgrow(this, Priority.ALWAYS);

        // ── Title ──────────────────────────────────────────────────
        Label title = new Label("Entity Metrics (Top 5)");
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
        table.setFixedCellSize(46);
        table.prefHeightProperty().bind(
                table.fixedCellSizeProperty().multiply(
                        Bindings.size(table.getItems()).add(1.05)
                )
        );
        table.setMaxHeight(Double.MAX_VALUE);

        // Entity name column
        TableColumn<EntityEntry, String> nameCol = new TableColumn<>("ENTITY");
        nameCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().name()));
        nameCol.setMinWidth(180);
        nameCol.setSortable(false);
        nameCol.setCellFactory(col -> new TableCell<>() {
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
        });

        // Kind column
        TableColumn<EntityEntry, String> kindCol = new TableColumn<>("KIND");
        kindCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().kind()));
        kindCol.setMinWidth(80);
        kindCol.setMaxWidth(110);
        kindCol.setSortable(false);
        kindCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.toUpperCase());
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-font-family: 'Inter'; -fx-font-size: 11px; -fx-text-fill: #849396;");
                }
            }
        });

        // LOC column
        TableColumn<EntityEntry, Integer> locCol = createNumericColumn("LOC", "loc", 60);

        // Complexity column
        TableColumn<EntityEntry, Integer> compCol = createNumericColumn("COMPLEXITY", "complexity", 90);

        // Fan-Out column
        TableColumn<EntityEntry, Integer> fanOutCol = createNumericColumn("FAN-OUT", "fanOut", 70);

        // Fan-In column
        TableColumn<EntityEntry, Integer> fanInCol = createNumericColumn("FAN-IN", "fanIn", 70);

        // Risk column
        TableColumn<EntityEntry, String> riskCol = new TableColumn<>("RISK");
        riskCol.setCellValueFactory(cellData -> new javafx.beans.property.SimpleStringProperty(cellData.getValue().risk()));
        riskCol.setMinWidth(80);
        riskCol.setMaxWidth(110);
        riskCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toUpperCase());
                    String color = "#ffa726";
                    if ("CRITICAL".equalsIgnoreCase(item) || "HIGH".equalsIgnoreCase(item)) {
                        color = "#ff4b4b";
                    } else if ("LOW".equalsIgnoreCase(item)) {
                        color = "#00e676";
                    }
                    setStyle("-fx-alignment: CENTER-LEFT; -fx-font-family: 'Inter'; -fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: " + color + ";");
                }
            }
        });

        table.getColumns().addAll(nameCol, kindCol, locCol, compCol, fanOutCol, fanInCol, riskCol);

        // Limit items to at most 6
        updateTableItems();
        state.getEntities().addListener((javafx.collections.ListChangeListener<EntityEntry>) c -> updateTableItems());

        // Hover row styling
        table.setRowFactory(tv -> {
            javafx.scene.control.TableRow<EntityEntry> row = new javafx.scene.control.TableRow<>();
            row.getStyleClass().add("ar-table-row");
            return row;
        });
    }

    private TableColumn<EntityEntry, Integer> createNumericColumn(String header, String property, double minW) {
        TableColumn<EntityEntry, Integer> col = new TableColumn<>(header);
        if ("loc".equals(property)) {
            col.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().loc()).asObject());
        } else if ("complexity".equals(property)) {
            col.setCellValueFactory(cellData -> new javafx.beans.property.SimpleIntegerProperty(cellData.getValue().complexity()).asObject());
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

    private void updateTableItems() {
        var all = state.getEntities();
        var limited = javafx.collections.FXCollections.<EntityEntry>observableArrayList();
        for (int i = 0; i < Math.min(6, all.size()); i++) {
            limited.add(all.get(i));
        }
        table.setItems(limited);
    }
}
