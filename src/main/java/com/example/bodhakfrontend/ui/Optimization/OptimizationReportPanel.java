package com.example.bodhakfrontend.ui.Optimization;

import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;
import com.example.bodhakfrontend.Backend.models.Class.MethodInfo;
import com.example.bodhakfrontend.Backend.models.Project.Hotspots;
import com.example.bodhakfrontend.Nic.Model.*;
import com.example.bodhakfrontend.uiHelper.UiFeatures;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class OptimizationReportPanel {

    private final ScrollPane scrollPane = new ScrollPane();
    private final VBox root = new VBox(30);
    private final UiFeatures uiFeatures;

    public OptimizationReportPanel(OptimizationReport optimizationReport, UiFeatures uiFeatures) {
        this.uiFeatures = uiFeatures;
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #121212;");
        scrollPane.setContent(root);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: #121212; -fx-control-inner-background: #121212;");
        var css = getClass().getResource("/style.css");
        if (css != null) {
            scrollPane.getStylesheets().add(css.toExternalForm());
        }
        // Header
        VBox headerBox = new VBox(5);
        VBox healthBar=createHealthScoreSection(optimizationReport);

        Label title = new Label("Optimization Complete");
        title.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #e8eaed;");
        Label subtitle = new Label(" Successfully determined the optimal architecture structure.");
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #9aa0a6;");
        headerBox.getChildren().addAll(title, subtitle);


        // 3. Impact Analysis Table Section
        VBox tableBox = new VBox(10);
        HBox tableTitle=loadLabelWithIcon("largestFiles.png","Impact Analysis");
        TableView<Row> table = buildTable(optimizationReport.getGAResult());
        tableBox.getChildren().addAll(tableTitle, table);

        // 4. Strategy Section
        VBox strategyBox = createStrategySection(optimizationReport.getGAResult());

        // 5. Suggested Refactoring Locations Section
        VBox suggestionsBox = createSuggestionsSection(optimizationReport);

        // 6. Code Hotspots Section
        VBox hotspotsBox = createHotspotsSection(optimizationReport);

        root.getChildren().addAll(headerBox, healthBar, tableBox, strategyBox, suggestionsBox, hotspotsBox);

        animateEntrance();
    }

    public Node getRoot() {
        return scrollPane;
    }

    private VBox createHealthScoreSection(OptimizationReport report) {

        double beforeScore = report.getBeforeScore();
        double afterScore = report.getAfterScore();
        double improvement = afterScore - beforeScore;

        Metrics before = report.getGAResult().beforeMetrics();
        Metrics after = report.getGAResult().afterMetrics();

        VBox sectionBox = new VBox(15);

        VBox card = new VBox(18);
        card.setStyle("""
        -fx-background-color: #090f10;
        -fx-background-radius: 12;
        -fx-padding: 20;
        -fx-border-color: #1a2122;
        -fx-border-radius: 12;
        -fx-border-width: 1;
    """);

        // ================= TOP =================
        VBox topBox = new VBox(4);

        Label titleLabel = new Label("Architecture Health");
        titleLabel.setStyle("""
        -fx-font-size: 16px;
        -fx-text-fill: #94a3b8;
        -fx-font-weight: bold;
    """);

        Label improvementLabel = new Label(String.format("%+.2f", improvement));
        improvementLabel.setStyle("""
        -fx-font-size: 28px;
        -fx-font-weight: bold;
        -fx-text-fill: #4bf6ff;
    """);

        Label improve = new Label("Improvement");
        improve.setStyle("""
        -fx-font-size: 11px;
        -fx-text-fill: #4bf6ff;
    """);

        topBox.getChildren().addAll(titleLabel, improvementLabel, improve);

        // ================= BARS =================
        VBox barsBox = new VBox(15);

        // ---------- BEFORE ----------
        VBox beforeBox = new VBox(6);

        Label beforeLabel = new Label("Before Optimization");
        beforeLabel.setStyle("-fx-text-fill: #dde4e5; -fx-font-size: 12px;");

        StackPane beforeStack = new StackPane();

        ProgressBar beforeBar = new ProgressBar(beforeScore / 100);
        beforeBar.setPrefHeight(8);
        beforeBar.setMaxWidth(Double.MAX_VALUE);
        beforeBar.setStyle("""
        -fx-accent: #ff6b6b;
        -fx-control-inner-background: #1a2122;
        -fx-background-radius: 6;
    """);

        Label beforeScoreFormat = new Label(String.format("%.2f", beforeScore));
        beforeScoreFormat.setStyle("""
        -fx-text-fill: #dde4e5;
        -fx-font-size: 10px;
        -fx-font-weight: bold;
    """);

        beforeStack.getChildren().addAll(beforeBar, beforeScoreFormat);
        beforeBox.getChildren().addAll(beforeLabel, beforeStack);

        // ---------- AFTER ----------
        VBox afterBox = new VBox(6);

        Label afterLabel = new Label("After Optimization");
        afterLabel.setStyle("-fx-text-fill: #dde4e5; -fx-font-size: 12px;");

        StackPane afterStack = new StackPane();

        ProgressBar afterBar = new ProgressBar(afterScore / 100);
        afterBar.setPrefHeight(8);
        afterBar.setMaxWidth(Double.MAX_VALUE);
        afterBar.setStyle("""
        -fx-accent: #4bf6ff;
        -fx-control-inner-background: #1a2122;
        -fx-background-radius: 6;
    """);

        Label afterScoreFormat = new Label(String.format("%.2f", afterScore));
        afterScoreFormat.setStyle("""
        -fx-text-fill: #dde4e5;
        -fx-font-size: 10px;
        -fx-font-weight: bold;
    """);

        afterStack.getChildren().addAll(afterBar, afterScoreFormat);
        afterBox.getChildren().addAll(afterLabel, afterStack);

        barsBox.getChildren().addAll(beforeBox, afterBox);

        // ================= BASED ON SECTION =================
        VBox basedOnBox = new VBox(6);

        Label basedOnTitle = new Label("Score is based on:");
        basedOnTitle.setStyle("""
        -fx-text-fill: #94a3b8;
        -fx-font-size: 11px;
        -fx-font-weight: bold;
    """);

        Label factors = new Label("""
        • Method Complexity
        • Code Size (LOC)
        • Dependency Structure
    """);

        factors.setStyle("""
        -fx-text-fill: #dde4e5;
        -fx-font-size: 11px;
    """);

        basedOnBox.getChildren().addAll(basedOnTitle, factors);

        // ================= DYNAMIC EXPLANATION =================
        VBox explanationBox = new VBox(6);

        Label explanationTitle = new Label("Insight:");
        explanationTitle.setStyle("""
        -fx-text-fill: #94a3b8;
        -fx-font-size: 11px;
        -fx-font-weight: bold;
    """);

        StringBuilder explanationText = new StringBuilder();

        if (after.getAverageMethodLoc() < before.getAverageMethodLoc()) {
            explanationText.append("• Method size reduced\n");
        }
        if (after.getAverageDeps() < before.getAverageDeps()) {
            explanationText.append("• Dependencies reduced\n");
        }
        if (after.getAverageLoc() < before.getAverageLoc()) {
            explanationText.append("• Code became more balanced\n");
        }

        if (explanationText.isEmpty()) {
            explanationText.append("• No significant structural improvements detected");
        }

        Label explanation = new Label(explanationText.toString());
        explanation.setStyle("""
        -fx-text-fill: #4bf6ff;
        -fx-font-size: 11px;
    """);

        explanationBox.getChildren().addAll(explanationTitle, explanation);

        // ================= FINAL LAYOUT =================
        card.getChildren().addAll(topBox, barsBox, basedOnBox, explanationBox);

        // shadow
        DropShadow shadow = new DropShadow();
        shadow.setRadius(12);
        shadow.setColor(Color.rgb(0, 0, 0, 0.5));
        card.setEffect(shadow);

        sectionBox.getChildren().add(card);

        return sectionBox;
    }private VBox createStrategySection(GAResult result) {
        VBox sectionBox = new VBox(15);

        HBox sectionTitle=loadLabelWithIcon("suggestion.png"," Recommended Refactoring Strategies");
        FlowPane strategyCards = new FlowPane(15, 15);
        strategyCards.setAlignment(Pos.CENTER_LEFT);
        for(Genes gene : new HashSet<>(result.bestChromosome().getGenesList())) {
            VBox card = new VBox(10);
            card.setStyle("-fx-background-color: #242424; -fx-background-radius: 8; -fx-padding: 15; -fx-border-color: #404040; -fx-border-width: 1; -fx-border-radius: 8;");
            card.setPrefWidth(220);

            String opName = gene.getDispalyName();
            ImageView icon = getIconForStrategy(opName);

            HBox header = new HBox(8);
            header.setAlignment(Pos.CENTER_LEFT);


            Label opLabel = new Label("Strategy");
            opLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #9aa0a6;");
            header.getChildren().addAll(icon, opLabel);

            Label valLabel = new Label(opName);
            valLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #aecbfa; -fx-font-weight: bold; -fx-wrap-text: true;");

            card.getChildren().addAll(header, valLabel);
            addHoverEffect(card);
            strategyCards.getChildren().add(card);
        }
        sectionBox.getChildren().addAll(sectionTitle, strategyCards);
        return sectionBox;
    }

    private VBox createSuggestionsSection(OptimizationReport optimizationReport) {
        VBox sectionBox = new VBox(15);

        HBox header = loadLabelWithIcon("location.png","Suggested Refactoring Locations");

        FlowPane cardsContainer = new FlowPane(20, 20);
        cardsContainer.setAlignment(Pos.CENTER_LEFT);

        List<RefactoringSuggestion > suggestions = optimizationReport.getSuggestions();


        for(RefactoringSuggestion suggestion : suggestions) {


            cardsContainer.getChildren().add(createSuggestionCard(suggestion.getOperation(),suggestion.getClazz(),suggestion.getMethod(),suggestion.getReason(),suggestion.getSuggestion()));
        }

        sectionBox.getChildren().addAll(header, cardsContainer);
        return sectionBox;
    }

    private VBox createSuggestionCard(String title, ClassInfo clazz, MethodInfo method, String reason, String suggestion) {
        VBox card = new VBox(8);
        card.setStyle("-fx-background-color: #1e1e24; -fx-background-radius: 10; -fx-padding: 15; -fx-border-color: #3b3b4f; -fx-border-width: 1; -fx-border-radius: 10; -fx-pref-width: 320px;");

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 15px; -fx-text-fill: #fbbc04; -fx-font-weight: bold;");

        Text targetText = new Text("Target: " + clazz );
        targetText.setStyle("-fx-fill: #e8eaed; -fx-font-size: 14px; -fx-font-family: 'Courier New';");






        Label reasonLabel = new Label("Reason: " + reason);
        reasonLabel.setStyle("-fx-text-fill: #9aa0a6; -fx-font-size: 13px; -fx-wrap-text: true;");

        Label actionLabel = new Label("Action: " + suggestion);
        actionLabel.setStyle("-fx-text-fill: #8ab4f8; -fx-font-size: 13px; -fx-wrap-text: true;");

       if(method!=null){
           Text methodName=new Text("Method: " + method.getMethodName());
           methodName.setStyle("-fx-fill: #e8eaed; -fx-font-size: 14px; -fx-font-family: 'Courier New';");
           card.getChildren().addAll(titleLabel, targetText,methodName, reasonLabel, actionLabel);
       }
       else {card.getChildren().addAll(titleLabel, targetText, reasonLabel, actionLabel);}


        addHoverEffect(card);
       card.setCursor(Cursor.HAND);
       card.setOnMouseClicked(mouseEvent -> {
           if(method!=null){
               uiFeatures.openAndHighlight(method.getMethodName(), method.getStartLine(), method.getStartColumn(), method.getSourceFile());
           }
           else{
               uiFeatures.openAndHighlight(clazz.toString(), clazz.getBeginLine(), clazz.getBeginColumn(), clazz.getSourceFile());
           }

       });
        return card;
    }

    private VBox createHotspotsSection(OptimizationReport optimizationReport) {
        List<Hotspots> hotspots = optimizationReport.getHotspots();
        VBox sectionBox = new VBox(15);
        HBox sectionTitle = loadLabelWithIcon("hotspot.png","Code Hotspots");
        FlowPane cardsContainer = new FlowPane(15, 15);
        cardsContainer.setAlignment(Pos.CENTER_LEFT);


        for(Hotspots hotspot:hotspots){

            cardsContainer.getChildren().add(createHotspotCard(hotspot.getClassInfo(),hotspot.getReasons()));

        }




        sectionBox.getChildren().addAll(sectionTitle, cardsContainer);
        return sectionBox;
    }

    private VBox createHotspotCard(ClassInfo clazz, Set<String> reasons) {
        VBox card = new VBox(5);
        card.setStyle("-fx-background-color: #2b1c1c; -fx-background-radius: 8; -fx-padding: 12; -fx-border-color: #5c3a3a; -fx-border-width: 1; -fx-border-radius: 8; -fx-pref-width: 250px;");

        Label classLabel = new Label(clazz.toString());
        classLabel.setStyle("-fx-font-size: 14px; -fx-font-family: 'Courier New'; -fx-text-fill: #f28b82; -fx-font-weight: bold; -fx-wrap-text: true;");
        VBox reasonsBox = new VBox(5);
        for(String reason: reasons){
            Label issueLabel = new Label(reason);
            issueLabel.setStyle("-fx-font-size: 12px; -fx-text-fill: #e8eaed;");
            reasonsBox.getChildren().add(issueLabel);

        }
        card.getChildren().addAll(classLabel, reasonsBox);



        addHoverEffect(card);
        card.setCursor(Cursor.HAND);
        card.setOnMouseClicked(mouseEvent -> {
            uiFeatures.openAndHighlight(clazz.toString(), clazz.getBeginLine(), clazz.getBeginColumn(), clazz.getSourceFile());
        });
        return card;
    }

    private ImageView getIconForStrategy(String strategyName) {
        if (strategyName == null) return loadIcon("star.png");
        String lower = strategyName.toLowerCase();
        if (lower.contains("break") || lower.contains("extract")) return loadIcon("scissor.png");
        if (lower.contains("dead") || lower.contains("unused")) return loadIcon("broom.png");
        if (lower.contains("interface") || lower.contains("decouple")) return loadIcon("box.png");
        if (lower.contains("split") || lower.contains("decompose")) return loadIcon("axe.png");
        if (lower.contains("dependency") || lower.contains("circular")) return loadIcon("reuse.png");
        if (lower.contains("inline")) return loadIcon("reuse.png");
        return loadIcon("puzzle.png");
    }

    private ImageView loadIcon(String iconName) {

        URL url = getClass().getResource("/icons/" + iconName);

        if (url == null) {
            System.out.println("Icon not found: " + iconName);
            return new ImageView();
        }

        ImageView icon = new ImageView(new Image(url.toExternalForm()));
        icon.setFitWidth(18);
        icon.setFitHeight(18);

        return icon;
    }

    private HBox loadLabelWithIcon(String iconName,String  label) {
        HBox labelBox = new HBox(8);
        ImageView icon = loadIcon(iconName);
        Label header = new Label(label);
        header.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e8eaed;");
        labelBox.getChildren().addAll(icon,header);
        return labelBox;
    }


    private TableView<Row> buildTable(GAResult result) {

        Metrics before = result.beforeMetrics();
        Metrics after = result.afterMetrics();

        TableView<Row> table = new TableView<>();

        table.setStyle("""
        -fx-background-color: #090f10;
        -fx-control-inner-background: #090f10;
        -fx-border-color: #1a2122;
        -fx-border-radius: 8;
        -fx-background-radius: 8;
    """);

        table.setPrefHeight(140);

        // ================= COLUMNS =================
        TableColumn<Row, String> metric = new TableColumn<>("Metric");
        TableColumn<Row, String> beforeCol = new TableColumn<>("Before");
        TableColumn<Row, String> afterCol = new TableColumn<>("After");
        TableColumn<Row, String> improvement = new TableColumn<>("Improvement");

        metric.setCellValueFactory(new PropertyValueFactory<>("metric"));
        beforeCol.setCellValueFactory(new PropertyValueFactory<>("before"));
        afterCol.setCellValueFactory(new PropertyValueFactory<>("after"));
        improvement.setCellValueFactory(new PropertyValueFactory<>("improvement"));

        // ================= ALIGNMENT =================
        metric.setStyle("-fx-alignment: CENTER-LEFT;");
        beforeCol.setStyle("-fx-alignment: CENTER;");
        afterCol.setStyle("-fx-alignment: CENTER;");
        improvement.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        // ================= ROW STYLE =================
        table.setRowFactory(tv -> new TableRow<>() {
            @Override
            protected void updateItem(Row item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setStyle("");
                } else {
                    setStyle("""
                    -fx-background-color: transparent;
                    -fx-border-color: #1a2122;
                    -fx-border-width: 0 0 1 0;
                """);
                }
            }
        });

        // ================= HEADER STYLE =================
        table.skinProperty().addListener((obs, oldSkin, newSkin) -> {
            table.lookupAll(".column-header").forEach(node -> {
                node.setStyle("""
                -fx-background-color: #090f10;
                -fx-border-color: #1a2122;
                -fx-border-width: 0 0 1 0;
            """);
            });
        });

        // ================= DATA =================
        table.getItems().addAll(
                new Row("Total LOC", before.getTotalLoc(), after.getTotalLoc()),
                new Row("Avg Method Length", before.getAverageMethodLoc(), after.getAverageMethodLoc()),
                new Row("Dependencies", before.getTotalDependency(), after.getTotalDependency())
        );

        table.getColumns().addAll(metric, beforeCol, afterCol, improvement);

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        return table;
    }

    private void animateEntrance() {
        for(int i=0; i<root.getChildren().size(); i++){
            Node node = root.getChildren().get(i);
            node.setOpacity(0);

            TranslateTransition tt = new TranslateTransition(Duration.millis(600), node);
            tt.setFromY(30);
            tt.setToY(0);

            FadeTransition ft = new FadeTransition(Duration.millis(600), node);
            ft.setFromValue(0);
            ft.setToValue(1);

            ParallelTransition pt = new ParallelTransition(tt, ft);
            pt.setDelay(Duration.millis(150 * i));
            pt.play();
        }
    }

    private void addHoverEffect(Node node) {
        ScaleTransition scaleIn = new ScaleTransition(Duration.millis(150), node);
        scaleIn.setToX(1.02);
        scaleIn.setToY(1.02);

        ScaleTransition scaleOut = new ScaleTransition(Duration.millis(150), node);
        scaleOut.setToX(1.0);
        scaleOut.setToY(1.0);

        node.setOnMouseEntered(e -> scaleIn.playFromStart());
        node.setOnMouseExited(e -> scaleOut.playFromStart());
    }

    private String formatDouble(double value) {
        return String.format("%.2f", value);
    }

    private String calculateImprovement(double before, double after) {
        double imp = before == 0 ? 0 : ((before - after) / before) * 100;
        return String.format("%.2f%%", imp);
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

        public String getMetric() {
            return metric;
        }

        public String getBefore() {
            return before;
        }

        public String getAfter() {
            return after;
        }

        public String getImprovement() {
            return improvement;
        }
    }







}
