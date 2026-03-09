package com.example.bodhakfrontend.ui.Optimization;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.MethodInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Project.Hotspots;
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
        VBox sectionBox = new VBox(15);

        VBox card = new VBox(25);
        card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 12; -fx-padding: 30; -fx-border-color: #333333; -fx-border-width: 1; -fx-border-radius: 12;");

        // Top Part - Title & Improvement
        VBox topBox = new VBox(5);
        Label titleLabel = new Label("Architecture Health");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-text-fill: #9aa0a6; -fx-font-weight: bold;");

        Label improvementLabel = new Label(String.format("%+.2f", improvement));
        improvementLabel.setStyle("-fx-font-size: 32px; -fx-font-weight: bold; -fx-text-fill: #81c995;");
        Label improve=new Label("Improvement");
        improve.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #81c995;");
        topBox.getChildren().addAll(titleLabel, improvementLabel, improve);

        // Bottom Part - Progress Bars
        VBox barsBox = new VBox(20);

        // Before Optimization
        VBox beforeBox = new VBox(8);
        Label beforeLabel = new Label("Before Optimization");
        beforeLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e8eaed;");

        // Stackpane lets us put text over the progressbar
        javafx.scene.layout.StackPane beforeStack = new javafx.scene.layout.StackPane();
        ProgressBar beforeBar = new ProgressBar(beforeScore/100);
        beforeBar.setPrefWidth(Double.MAX_VALUE);
        beforeBar.setPrefHeight(25);
        beforeBar.setStyle("-fx-accent: #f28b82; -fx-control-inner-background: #2b2b2b; -fx-background-color: #2b2b2b; -fx-background-radius: 8;");

        Label beforeScoreFormat = new Label(String.format("%.2f", beforeScore));
        beforeScoreFormat.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        beforeStack.getChildren().addAll(beforeBar, beforeScoreFormat);
        beforeBox.getChildren().addAll(beforeLabel, beforeStack);

        // After Optimization
        VBox afterBox = new VBox(8);
        Label afterLabel = new Label("After Optimization");
        afterLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e8eaed;");

        javafx.scene.layout.StackPane afterStack = new javafx.scene.layout.StackPane();
        ProgressBar afterBar = new ProgressBar(afterScore/100);
        afterBar.setPrefWidth(Double.MAX_VALUE);
        afterBar.setPrefHeight(25);
        afterBar.setStyle("-fx-accent: #81c995; -fx-control-inner-background: #2b2b2b; -fx-background-color: #2b2b2b; -fx-background-radius: 8;");

        Label afterScoreFormat = new Label(String.format("%.2f", afterScore));
        afterScoreFormat.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 13px;");
        afterStack.getChildren().addAll(afterBar, afterScoreFormat);
        afterBox.getChildren().addAll(afterLabel, afterStack);

        barsBox.getChildren().addAll(beforeBox, afterBox);
        card.getChildren().addAll(topBox, barsBox);

        DropShadow shadow = new DropShadow(15, Color.rgb(0,0,0, 0.4));
        card.setEffect(shadow);

        sectionBox.getChildren().addAll(card);
        return sectionBox;
    }


    private VBox createQuickSummarySection(GAResult result) {
        VBox sectionBox = new VBox(15);
        Label sectionTitle = new Label("⚡ Quick Optimization Summary");
        sectionTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #e8eaed;");

        FlowPane cardsContainer = new FlowPane(20, 20);
        cardsContainer.setAlignment(Pos.CENTER_LEFT);

        Metrics before = result.beforeMetrics();
        Metrics after = result.afterMetrics();

        cardsContainer.getChildren().addAll(
                createSummaryCard("Total LOC", formatDouble(before.getTotalLoc()), formatDouble(after.getTotalLoc()), calculateImprovement(before.getTotalLoc(), after.getTotalLoc())),
                createSummaryCard("Avg Method Length", formatDouble(before.getAverageMethodLoc()), formatDouble(after.getAverageMethodLoc()), calculateImprovement(before.getAverageMethodLoc(), after.getAverageMethodLoc())),
                createSummaryCard("Dependencies", formatDouble(before.getTotalDependency()), formatDouble(after.getTotalDependency()), calculateImprovement(before.getTotalDependency(), after.getTotalDependency()))
        );

        sectionBox.getChildren().addAll(sectionTitle, cardsContainer);
        return sectionBox;
    }

    private VBox createSummaryCard(String title, String before, String after, String improvement) {
        VBox card = new VBox(10);
        card.setStyle("-fx-background-color: #1e1e1e; -fx-background-radius: 10; -fx-padding: 20; -fx-border-color: #333333; -fx-border-width: 1; -fx-border-radius: 10;");
        card.setPrefWidth(220);

        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #9aa0a6; -fx-font-weight: bold;");

        HBox valuesBox = new HBox(10);
        valuesBox.setAlignment(Pos.CENTER_LEFT);
        Label beforeLabel = new Label(before);
        beforeLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #e8eaed; -fx-strikethrough: true;");
        Label arrowLabel = new Label("→");
        arrowLabel.setStyle("-fx-font-size: 18px; -fx-text-fill: #9aa0a6;");
        Label afterLabel = new Label(after);
        afterLabel.setStyle("-fx-font-size: 22px; -fx-text-fill: #81c995; -fx-font-weight: bold;");
        valuesBox.getChildren().addAll(beforeLabel, arrowLabel, afterLabel);

        Label impLabel = new Label((improvement.startsWith("-") || improvement.equals("0.00%") ? "" : "+") + improvement + " Improvement");
        impLabel.setStyle("-fx-font-size: 13px; -fx-text-fill: " + (improvement.startsWith("-") ? "#f28b82" : "#8ab4f8") + "; -fx-font-weight: bold;");

        card.getChildren().addAll(titleLabel, valuesBox, impLabel);
        addHoverEffect(card);
        return card;
    }

    private VBox createStrategySection(GAResult result) {
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
