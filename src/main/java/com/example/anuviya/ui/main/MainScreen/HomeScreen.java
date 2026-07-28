package com.example.anuviya.ui.main.MainScreen;

import javafx.geometry.*;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.File;
import java.util.function.Consumer;

public class HomeScreen {

    // ================= TOP BAR =================

    /**
     * Legacy overload — keeps existing callers compiling.
     * Produces a top bar without a nav strip (used before project load).
     */
    public Node createTopBar(Consumer<Button> onSelectFolder) {
        return createTopBar(onSelectFolder, null, null);
    }

    /**
     * Creates the top bar with an optional navigation strip.
     *
     * @param onSelectFolder Action to run when "Select Folder" is clicked.
     * @param navBar         The {@link com.example.anuviya.ui.nav.BodhakNavBar} node,
     *                       or {@code null} to omit the navigation strip.
     */
    public Node createTopBar(Consumer<Button> onSelectFolder, Node navBar) {
        return createTopBar(onSelectFolder, navBar, null);
    }

    /**
     * Creates the top bar with an optional navigation strip and a home/recent callback.
     *
     * @param onSelectFolder Action to run when "Select Folder" is clicked.
     * @param navBar         The {@link com.example.anuviya.ui.nav.BodhakNavBar} node,
     *                       or {@code null} to omit the navigation strip.
     * @param onShowHome     Runnable to show Workspace Home, or {@code null} to omit.
     * @param onLoadProject  Called with the chosen recent project File to load it directly.
     */
    public Node createTopBar(Consumer<Button> onSelectFolder, Node navBar, Runnable onShowHome) {
        return createTopBar(onSelectFolder, navBar, onShowHome, null);
    }

    /**
     * Full overload with explicit direct-load callback for recent project items.
     *
     * @param onSelectFolder Action to run when "Select Folder" is clicked.
     * @param navBar         Nav strip, or {@code null}.
     * @param onShowHome     Runnable to navigate to Workspace Home.
     * @param onLoadProject  Direct load consumer for a chosen recent project File.
     */
    public Node createTopBar(Consumer<Button> onSelectFolder, Node navBar, Runnable onShowHome, Consumer<File> onLoadProject) {
        return createTopBar(onSelectFolder, navBar, onShowHome, onLoadProject, null);
    }

    public Node createTopBar(Consumer<Button> onSelectFolder, Node navBar, Runnable onShowHome, Consumer<File> onLoadProject, Runnable onToggleSidebar) {
        HBox root = new HBox(14);
        root.getStyleClass().add("top-bar");
        root.setAlignment(Pos.CENTER_LEFT);
        root.setPadding(new Insets(8, 16, 8, 16));

        Label logo = new Label("Anuviya");
        logo.getStyleClass().add("logo");

        javafx.scene.image.ImageView logoIcon = com.example.anuviya.ui.helper.IconHelper.createLogoImageView(22);
        Node logoNode = logo;
        if (logoIcon != null) {
            HBox logoBox = new HBox(8, logoIcon, logo);
            logoBox.setAlignment(Pos.CENTER_LEFT);
            logoNode = logoBox;
        }

        // Home button — always visible when a home callback is provided
        if (onShowHome != null) {
            Button homeBtn = new Button("⌂");
            homeBtn.setTooltip(new Tooltip("Recent Projects"));
            homeBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-text-fill: #00daf3;" +
                "-fx-font-size: 17px;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 2 8 2 0;"
            );
            homeBtn.setOnAction(e -> onShowHome.run());
            root.getChildren().addAll(logoNode, homeBtn);
        } else {
            root.getChildren().add(logoNode);
        }

        // Sidebar toggle button (looks exactly like collapsible sidebar icon)
        if (onToggleSidebar != null) {
            Button sidebarToggleBtn = new Button();
            sidebarToggleBtn.setStyle(
                "-fx-background-color: transparent;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 0 4;"
            );
            javafx.scene.shape.SVGPath path = new javafx.scene.shape.SVGPath();
            path.setContent("M3 2 h10 a1 1 0 0 1 1 1 v10 a1 1 0 0 1 -1 1 h-10 a1 1 0 0 1 -1 -1 v-10 a1 1 0 0 1 1 -1 z M6 2 v12");
            path.setStroke(javafx.scene.paint.Color.web("#849494"));
            path.setStrokeWidth(1.5);
            path.setFill(javafx.scene.paint.Color.TRANSPARENT);
            sidebarToggleBtn.setGraphic(path);

            sidebarToggleBtn.setOnMouseEntered(ev -> path.setStroke(javafx.scene.paint.Color.web("#00daf3")));
            sidebarToggleBtn.setOnMouseExited(ev -> path.setStroke(javafx.scene.paint.Color.web("#849494")));
            sidebarToggleBtn.setOnAction(e -> onToggleSidebar.run());

            root.getChildren().add(sidebarToggleBtn);
        }

        Button selectBtn = new Button("SELECT FOLDER");
        selectBtn.getStyleClass().add("btn-primary");
        onSelectFolder.accept(selectBtn);
        root.getChildren().add(selectBtn);

        // Recent Projects dropdown menu button
        if (onShowHome != null) {
            MenuButton recentBtn = new MenuButton("Recent ▾");
            recentBtn.setStyle(
                "-fx-background-color: rgba(0,218,243,0.08);" +
                "-fx-text-fill: #c3f5ff;" +
                "-fx-border-color: rgba(0,218,243,0.25);" +
                "-fx-border-width: 1;" +
                "-fx-border-radius: 6;" +
                "-fx-background-radius: 6;" +
                "-fx-font-family: 'Epilogue';" +
                "-fx-font-size: 11px;" +
                "-fx-font-weight: 700;" +
                "-fx-cursor: hand;" +
                "-fx-padding: 4 10 4 10;"
            );

            // Populate with recent workspace projects
            java.util.List<com.example.anuviya.workspace.model.WorkspaceProject> recentProjects =
                com.example.anuviya.workspace.WorkspaceManager.getInstance().getRecentProjects();

            if (recentProjects.isEmpty()) {
                MenuItem noItems = new MenuItem("No recent projects");
                noItems.setDisable(true);
                recentBtn.getItems().add(noItems);
            } else {
                for (com.example.anuviya.workspace.model.WorkspaceProject p : recentProjects) {
                    // Relative time label
                    String timeAgo = p.lastOpened() != null
                        ? formatRelativeTime(p.lastOpened())
                        : "";
                    MenuItem item = new MenuItem("📁  " + p.name() + (timeAgo.isEmpty() ? "" : "  —  " + timeAgo));
                    item.setStyle("-fx-font-size: 11px;");
                    item.setOnAction(ev -> {
                        File f = new File(p.location());
                        if (f.exists()) {
                            if (onLoadProject != null) {
                                onLoadProject.accept(f);
                            } else if (onShowHome != null) {
                                onShowHome.run(); // fallback: go to home
                            }
                        }
                    });
                    recentBtn.getItems().add(item);
                }

                recentBtn.getItems().add(new SeparatorMenuItem());
                MenuItem viewAll = new MenuItem("View All Projects...");
                viewAll.setOnAction(ev -> onShowHome.run());
                recentBtn.getItems().add(viewAll);
            }

            root.getChildren().add(recentBtn);
        }

        // Nav strip sits right after the folder buttons
        if (navBar != null) {
            root.getChildren().add(navBar);
        }

        // Push everything to the left; spacer fills the right
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        root.getChildren().add(spacer);

        // Far-right: AI status indicator
        com.example.anuviya.analyzer.ai.ui.AIStatusIndicator indicator = new com.example.anuviya.analyzer.ai.ui.AIStatusIndicator();
        
        Button settingsBtn = new Button("⚙");
        settingsBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #dde4e5; -fx-font-size: 16px; -fx-cursor: hand;");
        settingsBtn.setOnAction(e -> {
            com.example.anuviya.ui.settings.SettingsDialog.show();
        });

        root.getChildren().addAll(indicator, settingsBtn);

        return root;
    }


    // ================= SIDEBAR =================
    public Node createSidebar(TreeView<File> tree) {

        VBox root = new VBox(10);
        root.getStyleClass().add("left-panel");
        root.setPrefWidth(220);
        root.setPadding(new Insets(16));

        Label title = new Label("PROJECT EXPLORER");
        title.getStyleClass().add("section-title");

        VBox.setVgrow(tree, Priority.ALWAYS);
        tree.getStyleClass().add("tree-view");

        root.getChildren().addAll(title, tree);
        return root;
    }

    public Node createBottomBar(ProgressBar progress, Label status) {

        BorderPane root = new BorderPane();
        root.getStyleClass().add("bottom-bar");

        // ================= PROGRESS =================
        if (progress != null) {
            progress.getStyleClass().add("bottom-progress");

            // Set progress bar to half the length of the bottom bar
            progress.prefWidthProperty().bind(root.widthProperty().divide(2));
            progress.setMaxWidth(Region.USE_PREF_SIZE);
            progress.setPrefHeight(3);

            HBox progressBox = new HBox(progress);
            progressBox.setAlignment(Pos.CENTER_LEFT);
            progressBox.setPadding(new Insets(0, 0, 0, 16));
            root.setCenter(progressBox);
        }

        // ================= STATUS =================
        status.getStyleClass().add("bottom-status");

        HBox statusBox = new HBox(status);
        statusBox.setAlignment(Pos.CENTER_RIGHT);
        statusBox.setPadding(new Insets(0, 12, 0, 0));

        // ================= LAYOUT =================
        root.setRight(statusBox);   // text on right

        return root;
    }

    private String formatRelativeTime(java.time.Instant instant) {
        long seconds = java.time.Duration.between(instant, java.time.Instant.now()).getSeconds();
        if (seconds < 60) return "just now";
        if (seconds < 3600) return (seconds / 60) + " min ago";
        if (seconds < 86400) return (seconds / 3600) + " hr ago";
        if (seconds < 172800) return "Yesterday";
        return (seconds / 86400) + " days ago";
    }
}
