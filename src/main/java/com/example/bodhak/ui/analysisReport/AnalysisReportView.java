package com.example.bodhak.ui.analysisReport;

import com.example.bodhak.ui.analysisReport.state.AnalysisReportState;
import com.example.bodhak.ui.analysisReport.uiComponent.AnalysisInspectorContent;
import com.example.bodhak.ui.analysisReport.uiComponent.AnalysisInspectorContainer;
import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import javafx.scene.Node;
import javafx.scene.control.Tooltip;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import java.util.HashMap;
import java.util.Map;

public class AnalysisReportView extends StackPane {

    private final AnalysisReportState state;
    private final DashboardGrid dashboardGrid;
    private final ScrollPane scrollPane;
    private final com.example.bodhak.ui.analysisReport.compiler.CompilerExplorerDashboard compilerExplorerDashboard;

    private final BorderPane layoutRoot = new BorderPane();
    private final StackPane contentArea = new StackPane();
    private final VBox sidebar = new VBox();
    
    private final Map<String, Node> views = new HashMap<>();
    private final Map<String, VBox> navItems = new HashMap<>();
    private String activeTab = "Overview";

    private final Region darkScrim;
    private final StackPane inspectorWrapper;
    private ParallelTransition activeTransition;

    public AnalysisReportView(AnalysisReportState state) {
        this.state = state;
        this.dashboardGrid = new DashboardGrid(state, this::showInspector);
        this.scrollPane = new ScrollPane(dashboardGrid);
        this.compilerExplorerDashboard = new com.example.bodhak.ui.analysisReport.compiler.CompilerExplorerDashboard(state);
        
        this.darkScrim = new Region();
        this.inspectorWrapper = new StackPane();
        
        initialise();
    }

    private void initialise() {
        // Load the Cyber-Intelligence CSS
        String css = getClass().getResource("/styles/AnalysisReport.css").toExternalForm();
        getStylesheets().add(css);

        setStyle("-fx-background-color: #050B11;");
        getStyleClass().add("ar-dashboard-bg");

        // ScrollPane setup
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        scrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollPane.getStyleClass().setAll("ar-scroll-pane");
        scrollPane.setStyle("-fx-background: #050B11; -fx-background-color: #050B11;");

        // Tab setup
        views.put("Overview", scrollPane);
        views.put("Compiler Explorer", compilerExplorerDashboard);

        sidebar.setPrefWidth(64);
        sidebar.getStyleClass().add("ar-sidebar");

        buildNavButton("Overview", "/icons/AnalysisReportIcons/cardiogram.png", 20.0);
        buildNavButton("Compiler Explorer", "/icons/AnalysisReportIcons/code.png", 20.0);

        layoutRoot.setLeft(sidebar);
        layoutRoot.setCenter(contentArea);

        showTab(activeTab);

        // Dark Scrim setup
        darkScrim.getStyleClass().add("ar-scrim");
        darkScrim.setVisible(false);
        darkScrim.setManaged(false);
        darkScrim.setOpacity(0.0);
        darkScrim.setOnMouseClicked(e -> closeInspector());

        // Inspector Wrapper setup
        inspectorWrapper.setAlignment(Pos.CENTER);
        inspectorWrapper.setVisible(false);
        inspectorWrapper.setManaged(false);
        inspectorWrapper.setOpacity(0.0);
        inspectorWrapper.setOnMouseClicked(e -> {
            if (e.getTarget() == inspectorWrapper) {
                closeInspector();
            }
        });

        getChildren().addAll(layoutRoot, darkScrim, inspectorWrapper);

        // Escape Key handler
        addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && inspectorWrapper.isVisible()) {
                closeInspector();
                event.consume();
            }
        });
    }

    public void showInspector(AnalysisInspectorContent content) {
        if (activeTransition != null) {
            activeTransition.stop();
        }

        content.bindToState(this.state);
        AnalysisInspectorContainer container = new AnalysisInspectorContainer(content, this::closeInspector);
        inspectorWrapper.getChildren().setAll(container);

        darkScrim.setVisible(true);
        darkScrim.setManaged(true);
        inspectorWrapper.setVisible(true);
        inspectorWrapper.setManaged(true);

        BoxBlur blur = new BoxBlur(0, 0, 3);
        scrollPane.setEffect(blur);

        // Opening animations (~200ms)
        FadeTransition scrimFade = new FadeTransition(Duration.millis(200), darkScrim);
        scrimFade.setFromValue(0.0);
        scrimFade.setToValue(1.0);

        FadeTransition wrapperFade = new FadeTransition(Duration.millis(200), inspectorWrapper);
        wrapperFade.setFromValue(0.0);
        wrapperFade.setToValue(1.0);

        ScaleTransition containerScale = new ScaleTransition(Duration.millis(200), container);
        containerScale.setFromX(0.96);
        containerScale.setFromY(0.96);
        containerScale.setToX(1.0);
        containerScale.setToY(1.0);

        TranslateTransition containerTranslate = new TranslateTransition(Duration.millis(200), container);
        containerTranslate.setFromY(20);
        containerTranslate.setToY(0);

        Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.ZERO, 
                        new KeyValue(blur.widthProperty(), 0), 
                        new KeyValue(blur.heightProperty(), 0)),
                new KeyFrame(Duration.millis(200), 
                        new KeyValue(blur.widthProperty(), 12), 
                        new KeyValue(blur.heightProperty(), 12))
        );

        activeTransition = new ParallelTransition(
                scrimFade, wrapperFade, containerScale, containerTranslate, blurTimeline
        );
        activeTransition.setOnFinished(e -> {
            container.requestFocus();
            activeTransition = null;
        });
        activeTransition.play();
    }

    public void closeInspector() {
        if (inspectorWrapper.getChildren().isEmpty()) return;
        
        // Request focus on root view to prevent JavaFX auto-scrolling to table views in ScrollPane
        this.requestFocus();

        if (activeTransition != null) {
            activeTransition.stop();
        }

        AnalysisInspectorContainer container = (AnalysisInspectorContainer) inspectorWrapper.getChildren().get(0);
        BoxBlur blur = (BoxBlur) scrollPane.getEffect();

        // Closing animations (~180ms - slightly faster)
        FadeTransition scrimFade = new FadeTransition(Duration.millis(180), darkScrim);
        scrimFade.setToValue(0.0);

        FadeTransition wrapperFade = new FadeTransition(Duration.millis(180), inspectorWrapper);
        wrapperFade.setToValue(0.0);

        ScaleTransition containerScale = new ScaleTransition(Duration.millis(180), container);
        containerScale.setToX(0.96);
        containerScale.setToY(0.96);

        TranslateTransition containerTranslate = new TranslateTransition(Duration.millis(180), container);
        containerTranslate.setToY(20);

        Timeline blurTimeline = new Timeline(
                new KeyFrame(Duration.millis(180), 
                        new KeyValue(blur.widthProperty(), 0), 
                        new KeyValue(blur.heightProperty(), 0))
        );

        activeTransition = new ParallelTransition(
                scrimFade, wrapperFade, containerScale, containerTranslate, blurTimeline
        );
        activeTransition.setOnFinished(e -> {
            darkScrim.setVisible(false);
            darkScrim.setManaged(false);
            inspectorWrapper.setVisible(false);
            inspectorWrapper.setManaged(false);
            scrollPane.setEffect(null);
            inspectorWrapper.getChildren().clear();
            activeTransition = null;
        });
        activeTransition.play();
    }

    private void buildNavButton(String name, String iconPath, double iconSize) {
        VBox item = new VBox(6);
        item.getStyleClass().add("ar-nav-item");
        
        javafx.scene.image.ImageView icon = null;
        try {
            javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream(iconPath));
            icon = new javafx.scene.image.ImageView(img);
            icon.setFitWidth(iconSize);
            icon.setFitHeight(iconSize);
            icon.getStyleClass().add("ar-nav-icon");
        } catch (Exception e) {
            Label fallbackIcon = new Label("•");
            fallbackIcon.getStyleClass().add("ar-nav-icon");
            item.getChildren().add(fallbackIcon);
        }
        
        if (icon != null) {
            item.getChildren().add(icon);
        }
        
        Label lblName = new Label(name);
        lblName.setStyle("-fx-text-fill: #849396; -fx-font-size: 8px; -fx-font-family: 'JetBrains Mono'; -fx-letter-spacing: 0.05em;");
        item.getChildren().add(lblName);
        
        Tooltip.install(item, new Tooltip(name));
        item.setOnMouseClicked(e -> showTab(name));
        
        sidebar.getChildren().add(item);
        navItems.put(name, item);
    }

    private void showTab(String tabName) {
        navItems.values().forEach(item -> item.getStyleClass().remove("ar-nav-item-active"));
        VBox activeItem = navItems.get(tabName);
        if (activeItem != null) {
            activeItem.getStyleClass().add("ar-nav-item-active");
        }

        activeTab = tabName;
        Node view = views.get(tabName);
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    public AnalysisReportState getState() {
        return state;
    }
}
