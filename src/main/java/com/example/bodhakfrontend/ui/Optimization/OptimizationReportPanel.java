package com.example.bodhakfrontend.ui.Optimization;

import com.example.bodhakfrontend.Nic.GAResult;
import com.example.bodhakfrontend.Nic.Model.Genes;
import com.example.bodhakfrontend.Nic.Model.Metrics;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class OptimizationReportPanel {

    private final VBox root = new VBox(25);

    public OptimizationReportPanel(GAResult result) {
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #121212;");

        // Header
        VBox headerBox = new VBox(5);
        Label title = new Label("Optimization Complete");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e8eaed;");
        Label subtitle = new Label("AI has successfully determined the optimal architecture structure.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #9aa0a6;");
        headerBox.getChildren().addAll(title, subtitle);

        // Metrics Table
        VBox tableBox = new VBox(10);
        Label tableTitle = new Label("Impact Analysis (Tabular View)");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e8eaed;");
        TableView<Row> table = buildTable(result);
        tableBox.getChildren().addAll(tableTitle, table);

        // Strategy Section (Cards)
        VBox strategyBox = new VBox(10);
        Label strategyTitle = new Label("Recommended Refactoring Strategies:");
        strategyTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e8eaed;");
        
        FlowPane strategyCards = new FlowPane(15, 15);
        strategyCards.setAlignment(Pos.CENTER_LEFT);
        for(Genes gene : result.bestChromosome().getGenesList()) {
            VBox card = new VBox(8);
            card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #3d3d3d; -fx-border-width: 1; -fx-border-radius: 8; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 5, 0, 0, 2);");
            card.setPrefWidth(200);
            
            Label opLabel = new Label("Refactor Operation");
            opLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9aa0a6;");
            
            Label valLabel = new Label(gene.toString());
            valLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #8ab4f8; -fx-font-weight: bold; -fx-wrap-text: true;");
            
            card.getChildren().addAll(opLabel, valLabel);
            strategyCards.getChildren().add(card);
        }
        strategyBox.getChildren().addAll(strategyTitle, strategyCards);

        root.getChildren().addAll(headerBox, tableBox, strategyBox);

        animateEntrance();
    }

    public VBox getRoot() {
        return root;
    }

    private TableView<Row> buildTable(GAResult result) {
        Metrics before = result.beforeMetrics();
        Metrics after = result.afterMetrics();

        TableView<Row> table = new TableView<>();
        table.setStyle("-fx-background-color: #1e1e1e; -fx-control-inner-background: #1e1e1e; -fx-border-color: #2d2d2d;");
        table.setPrefHeight(150); // Give it enough height for 3 rows + header

        TableColumn<Row, String> metric = new TableColumn<>("Metric");
        TableColumn<Row, String> beforeCol = new TableColumn<>("Before");
        TableColumn<Row, String> afterCol = new TableColumn<>("After");
        TableColumn<Row, String> improvement = new TableColumn<>("Improvement");

        metric.setCellValueFactory(new PropertyValueFactory<>("metric"));
        beforeCol.setCellValueFactory(new PropertyValueFactory<>("before"));
        afterCol.setCellValueFactory(new PropertyValueFactory<>("after"));
        improvement.setCellValueFactory(new PropertyValueFactory<>("improvement"));

        // Modern flat table styling logic applies globally via application CSS,
        // but adding local styles to match dark theme cleanly.
        metric.setStyle("-fx-alignment: CENTER-LEFT;");
        beforeCol.setStyle("-fx-alignment: CENTER;");
        afterCol.setStyle("-fx-alignment: CENTER;");
        improvement.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        table.getColumns().addAll(metric, beforeCol, afterCol, improvement);

        table.getItems().addAll(
                new Row("Total LOC", before.getTotalLoc(), after.getTotalLoc()),
                new Row("Avg Method Length", before.getAverageMethodLoc(), after.getAverageMethodLoc()),
                new Row("Dependencies", before.getTotalDependency(), after.getTotalDependency())
        );

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        return table;
    }

    private void animateEntrance() {
        for(int i=0; i<root.getChildren().size(); i++){
            Node node = root.getChildren().get(i);
            node.setOpacity(0);
            
            TranslateTransition tt = new TranslateTransition(Duration.millis(500), node);
            tt.setFromY(20);
            tt.setToY(0);
            
            FadeTransition ft = new FadeTransition(Duration.millis(500), node);
            ft.setFromValue(0);
            ft.setToValue(1);
            
            ParallelTransition pt = new ParallelTransition(tt, ft);
            pt.setDelay(Duration.millis(150 * i));
            pt.play();
        }
    }

    public static class Row {
        private final String metric;
        private final String before;
        private final String after;
        private final String improvement;

        public Row(String metric, double before, double after) {
            this.metric = metric;
            this.before = String.format("%.2f", before);
            this.after = String.format("%.2f", after);

            double imp = before == 0 ? 0 : ((before - after) / before) * 100;
            this.improvement = String.format("%s%.2f%%", imp > 0 ? "+" : "", imp);
        }

        public String getMetric() { return metric; }
        public String getBefore() { return before; }
        public String getAfter() { return after; }
        public String getImprovement() { return improvement; }
    }
}
