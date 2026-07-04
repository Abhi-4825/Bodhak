package com.example.bodhak.ui.dependencyExplorer;

import com.example.bodhak.orchestration.AnalysisEngine;
import com.example.bodhak.ui.nav.workspace.Workspace;
import com.example.bodhak.ui.dependencyExplorer.state.DependencyExplorerState;
import com.example.bodhak.ui.dependencyExplorer.components.DependencyExplorerDashboard;
import com.example.bodhak.ui.dependencyExplorer.components.DependencyPathFinderDashboard;
import com.example.bodhak.ui.dependencyExplorer.components.DependencyCyclesDashboard;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.*;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class DependencyWorkspace implements Workspace {

    private final BorderPane root = new BorderPane();
    private final DependencyExplorerState state = new DependencyExplorerState();
    
    private final StackPane contentArea = new StackPane();
    private final VBox sidebar = new VBox();
    
    private final Map<String, Node> views = new HashMap<>();
    private final Map<String, VBox> navItems = new HashMap<>();
    
    private String activeTab = "Explorer";

    public DependencyWorkspace() {
        initialise();
    }

    private void initialise() {
        // Load CSS stylesheet
        String css = getClass().getResource("/styles/DependencyDashboard.css").toExternalForm();
        root.getStylesheets().add(css);
        root.getStyleClass().add("dd-dashboard-bg");

        // Build Left Navigation Sidebar
        sidebar.getStyleClass().add("dd-sidebar");
        sidebar.setPrefWidth(64);
        

        buildNavButton("Explorer", "/icons/DependencyDashboard/Explorer.png",20.0);
        buildNavButton("Cycles", "/icons/DependencyDashboard/Cycle.png",20.0);

        buildNavButton("Path Finder", "/icons/DependencyDashboard/path.png",20.0);




        root.setLeft(sidebar);

        // Pre-create/Setup Views
        DependencyPathFinderDashboard pathFinder = new DependencyPathFinderDashboard(state);
        views.put("Explorer", new DependencyExplorerDashboard(state));

        views.put("Cycles", new DependencyCyclesDashboard(state, this::showTab, pathFinder::setPathQuery));

        views.put("Path Finder", pathFinder);




        root.setCenter(contentArea);
        
        // Show active tab
        showTab(activeTab);
    }

    private void buildNavButton(String name, String iconPath,Double size) {
        VBox item = new VBox(4);
        item.getStyleClass().add("dd-nav-item");
        Image iconSymbol=new Image(getClass().getResourceAsStream(iconPath));


        ImageView icon=new ImageView(iconSymbol);
        icon.setFitWidth(size);
        icon.setFitHeight(size);
        icon.getStyleClass().add("dd-nav-icon");
        
        item.getChildren().add(icon);
        Tooltip.install(item, new Tooltip(name));
        
        item.setOnMouseClicked(e -> showTab(name));
        
        sidebar.getChildren().add(item);
        navItems.put(name, item);
    }

    private void showTab(String tabName) {
        // Toggle active button style
        navItems.values().forEach(item -> item.getStyleClass().remove("dd-nav-item-active"));
        VBox activeItem = navItems.get(tabName);
        if (activeItem != null) {
            activeItem.getStyleClass().add("dd-nav-item-active");
        }

        activeTab = tabName;
        Node view = views.get(tabName);
        if (view != null) {
            contentArea.getChildren().setAll(view);
        }
    }

    private Node buildPlaceholder(String title) {
        VBox box = new VBox(20);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(80, 40, 80, 40));

        Label hexIcon = new Label("📊");
        hexIcon.setStyle("-fx-font-size: 64px; -fx-text-fill: #1e2a2b;");

        Label heading = new Label(title);
        heading.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");

        Label subheading = new Label("Placeholder View");
        subheading.setStyle("-fx-font-size: 14px; -fx-text-fill: #849494;");

        box.getChildren().addAll(hexIcon, heading, subheading);
        return box;
    }

    @Override
    public Node getRoot() {
        return root;
    }

    @Override
    public void refresh(AnalysisEngine engine) {
        if (engine != null && engine.getAnalysisContext() != null) {
            state.update(engine.getAnalysisContext());
        }
    }
}
