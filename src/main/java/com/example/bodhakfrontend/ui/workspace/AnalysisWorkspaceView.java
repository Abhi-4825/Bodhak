package com.example.bodhakfrontend.ui.workspace;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class AnalysisWorkspaceView extends BorderPane {

    private final Button restoreBtn;
    private final StackPane mainContentArea;
    private final HBox topBarRight;
    
    // Toggle Buttons for Main Content
    private final ToggleButton depsBtn;
    private final ToggleButton methodBtn;
    private final ToggleButton healthBtn;

    public AnalysisWorkspaceView() {
        this.getStyleClass().add("analysis-workspace");

        // 1. Top Bar
        restoreBtn = new Button("← Back to Code");
        restoreBtn.getStyleClass().add("restore-btn");
        
        topBarRight = new HBox(10);
        topBarRight.setAlignment(Pos.CENTER_RIGHT);

        HBox topBar = new HBox(10, restoreBtn);
        HBox.setHgrow(topBarRight, Priority.ALWAYS);
        topBar.getChildren().add(topBarRight);

        topBar.setPadding(new Insets(10, 20, 10, 20));
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.getStyleClass().add("analysis-topbar");
        this.setTop(topBar);



        // 3. Main Content Area
        depsBtn = new ToggleButton("Dependencies");
        methodBtn = new ToggleButton("Methods");
        healthBtn = new ToggleButton("Health");
        
        mainContentArea = new StackPane();
        mainContentArea.getStyleClass().add("analysis-main-content");

        VBox centerContent = new VBox(0);
        centerContent.getChildren().addAll(createMainContentTabs(), mainContentArea);
        VBox.setVgrow(mainContentArea, Priority.ALWAYS);
        
        // 4. Insights Panel (Right)
        VBox insightsPanel = createInsightsPanel();

        // Assemble Middle Section
        HBox body = new HBox( centerContent, insightsPanel);
        HBox.setHgrow(centerContent, Priority.ALWAYS);
        this.setCenter(body);
    }

    private VBox createLeftNav() {
        VBox nav = new VBox(10);
        nav.setPadding(new Insets(20));
        nav.setPrefWidth(220);
        nav.getStyleClass().add("analysis-nav");

        Label sectionLabel = new Label("NAVIGATION");
        sectionLabel.getStyleClass().add("nav-section-label");

        // Placeholder nav buttons
        Button searchBtn = createNavButton("Search", "nav-btn");
        Button structureBtn = createNavButton("Structure", "nav-btn");
        Button contextBtn = createNavButton("Context Tree", "nav-btn");
        
        // Active button example
        Button activeBtn = createNavButton("Explorer", "nav-btn", "nav-btn-active");

        nav.getChildren().addAll(sectionLabel, activeBtn, searchBtn, structureBtn, contextBtn);
        return nav;
    }

    private Button createNavButton(String text, String... styleClasses) {
        Button btn = new Button(text);
        btn.getStyleClass().addAll(styleClasses);
        btn.setMaxWidth(Double.MAX_VALUE);
        btn.setAlignment(Pos.CENTER_LEFT);
        return btn;
    }

    private HBox createMainContentTabs() {
        ToggleGroup group = new ToggleGroup();
        depsBtn.setToggleGroup(group);
        methodBtn.setToggleGroup(group);
        healthBtn.setToggleGroup(group);
        depsBtn.setSelected(true);

        HBox hb = new HBox(20, depsBtn, methodBtn, healthBtn);
        hb.setPadding(new Insets(15, 20, 15, 20));
        hb.setAlignment(Pos.CENTER_LEFT);
        hb.getStyleClass().add("analysis-content-tabs");
        
        depsBtn.getStyleClass().addAll("analysis-tab-btn");
        methodBtn.getStyleClass().addAll("analysis-tab-btn");
        healthBtn.getStyleClass().addAll("analysis-tab-btn");

        return hb;
    }

    private VBox createInsightsPanel() {
        VBox panel = new VBox(15);
        panel.setPadding(new Insets(20));
        panel.setPrefWidth(280);
        panel.getStyleClass().add("insights-panel");

        Label title = new Label("INSIGHTS");
        title.getStyleClass().add("insights-title");

        // Placeholders for Insights
        panel.getChildren().addAll(
            title,
            createInsightCard("Health Score", "98/100", "score-good"),
            createInsightCard("Risk Alerts", "2 High, 5 Low", "score-warning"),
            createInsightCard("Metrics", "Complexity: Normal", "score-neutral")
        );

        return panel;
    }

    private VBox createInsightCard(String title, String value, String valueStyleClass) {
        VBox card = new VBox(5);
        card.getStyleClass().add("insight-card");
        
        Label tLabel = new Label(title);
        tLabel.getStyleClass().add("insight-card-title");
        
        Label vLabel = new Label(value);
        vLabel.getStyleClass().addAll("insight-card-value", valueStyleClass);
        
        card.getChildren().addAll(tLabel, vLabel);
        return card;
    }

    public void setOverviewContent(Node dependenciesView, Node methodsView, Node healthView) {
        mainContentArea.getChildren().clear();
        
        // Wrap views in a scroll pane if not already
        ScrollPane depsScroll = wrapInScrollPane(dependenciesView);
        ScrollPane methodsScroll = wrapInScrollPane(methodsView);
        ScrollPane healthScroll = wrapInScrollPane(healthView);

        mainContentArea.getChildren().addAll(depsScroll, methodsScroll, healthScroll);
        
        depsScroll.setVisible(true);
        methodsScroll.setVisible(false);
        healthScroll.setVisible(false);

        depsBtn.setOnAction(e -> showView(depsScroll, methodsScroll, healthScroll));
        methodBtn.setOnAction(e -> showView(methodsScroll, depsScroll, healthScroll));
        healthBtn.setOnAction(e -> showView(healthScroll, depsScroll, methodsScroll));
    }

    private ScrollPane wrapInScrollPane(Node node) {
        ScrollPane pane = new ScrollPane(node);
        pane.setFitToWidth(true);
        pane.setFitToHeight(true);
        pane.getStyleClass().add("analysis-scroll-pane");
        return pane;
    }

    private void showView(Node show, Node... hide) {
        show.setVisible(true);
        for (Node n : hide) {
            n.setVisible(false);
        }
    }

    public void setEntitySelector(Node selector) {
        topBarRight.getChildren().clear();
        if (selector != null) {
            topBarRight.getChildren().add(selector);
        }
    }

    public Button getRestoreBtn() {
        return restoreBtn;
    }
}
