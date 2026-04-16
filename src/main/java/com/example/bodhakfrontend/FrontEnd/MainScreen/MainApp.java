//package com.example.bodhakfrontend.FrontEnd.MainScreen;
//
//import javafx.geometry.*;
//import javafx.scene.Node;
//import javafx.scene.control.*;
//import javafx.scene.layout.*;
//
//import java.io.File;
//import java.util.function.Consumer;
//
//
//public class HomeScreen {
//
//
//    // ---------------- TOP BAR ----------------
//    public Node createTopBar(Consumer<Button> setOnAction) {
//
//        HBox root = new HBox(20);
//        root.setPadding(new Insets(12, 24, 12, 24));
//        root.setAlignment(Pos.CENTER_LEFT);
//        root.setStyle("-fx-background-color: " + Theme.SURFACE + ";");
//
//        Label title = new Label("Bodhak – Project Analyzer");
//        title.setStyle("""
//        -fx-font-family: 'Manrope';
//        -fx-font-size: 16;
//        -fx-font-weight: 800;
//    """);
//
//        Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//
//        Button select = new Button("Select Folder");
//        setOnAction.accept(select);
//
//        select.setStyle("""
//        -fx-background-color: linear-gradient(to right, #2979FF, #5393FF);
//        -fx-text-fill: white;
//        -fx-font-weight: bold;
//        -fx-background-radius: 10;
//        -fx-padding: 6 14;
//    """);
//
//        root.getChildren().addAll(title, spacer, select);
//
//        return root;
//    }
//
//    // ---------------- SIDEBAR ----------------
//    public Node createSidebar(TreeView<File> tree) {
//
//        VBox box = new VBox(10);
//        box.setPrefWidth(260);
//        box.setPadding(new Insets(16));
//        box.setStyle("-fx-background-color: " + Theme.SURFACE_LOW + ";");
//
//        Label header = new Label("Project Files");
//        header.setStyle("-fx-font-weight: bold; -fx-font-size: 13;");
//
//
//        VBox.setVgrow(tree, Priority.ALWAYS);
//
//        // Remove ugly default borders
//        tree.setStyle("""
//        -fx-background-color: transparent;
//        -fx-control-inner-background: transparent;
//    """);
//
//        Button analyze = new Button("Analyze Project");
//        analyze.setMaxWidth(Double.MAX_VALUE);
//        analyze.setStyle("""
//        -fx-background-color: rgba(7,94,239,0.76);
//        -fx-text-fill: #2979FF;
//        -fx-font-weight: bold;
//        -fx-background-radius: 10;
//    """);
//
//        box.getChildren().addAll(header, tree, analyze);
//        return box;
//    }
//
//    private Node createEditor() {
//
//        VBox root = new VBox();
//
//        root.setStyle("-fx-background-color: " + Theme.SURFACE_LOWEST + ";");
//
//        TabPane tabs = new TabPane();
//        tabs.setStyle("""
//        -fx-background-color: #eef1f5;
//    """);
//
//        TextArea code = new TextArea();
//        code.setStyle("""
//        -fx-font-family: 'JetBrains Mono';
//        -fx-font-size: 13;
//        -fx-background-color: white;
//    """);
//
//        VBox.setVgrow(code, Priority.ALWAYS);
//
//        // Floating buttons
//        HBox floating = new HBox(10);
//        floating.setAlignment(Pos.CENTER);
//
//        Button ast = pillButton("AST", true);
//        Button overview = pillButton("Overview", false);
//
//        floating.getChildren().addAll(ast, overview);
//        floating.setPadding(new Insets(10));
//
//        root.getChildren().addAll(tabs, code, floating);
//        return root;
//    }
//    private Button pillButton(String text, boolean active) {
//
//        Button btn = new Button(text);
//
//        if (active) {
//            btn.setStyle("""
//            -fx-background-color: #2979FF;
//            -fx-text-fill: white;
//            -fx-background-radius: 20;
//            -fx-padding: 6 16;
//        """);
//        } else {
//            btn.setStyle("""
//            -fx-background-color: transparent;
//            -fx-text-fill: #6b7280;
//            -fx-background-radius: 20;
//            -fx-padding: 6 16;
//        """);
//        }
//
//        return btn;
//    }
//
//    // ---------------- RIGHT PANEL ----------------
//    private Node createAnalysisPanel() {
//
//        VBox root = new VBox(20);
//        root.setPadding(new Insets(20));
//        root.setPrefWidth(380);
//        root.setStyle("-fx-background-color: " + Theme.SURFACE_LOW + ";");
//
//        root.getChildren().addAll(
//                projectHealthCard(),
//                summaryGrid(),
//                entryPointCard(),
//                metricsGrid()
//        );
//
//        return root;
//    }private VBox projectHealthCard() {
//
//        VBox card = new VBox(12);
//        card.setPadding(new Insets(16));
//
//        card.setStyle("""
//        -fx-background-color: white;
//        -fx-background-radius: 16;
//    """);
//
//        Label title = smallLabel("PROJECT HEALTH");
//
//        Label score = new Label("84.2");
//        score.setStyle("""
//        -fx-font-family: 'Manrope';
//        -fx-font-size: 36;
//        -fx-font-weight: 900;
//    """);
//
//        ProgressBar bar = new ProgressBar(0.84);
//        bar.setStyle("-fx-accent: #ba1a1a;");
//
//        Label warn = new Label("Moderate technical debt detected");
//        warn.setStyle("-fx-text-fill: #ba1a1a;");
//
//        card.getChildren().addAll(title, score, bar, warn);
//        return card;
//    }
//    private Label smallLabel(String text) {
//        Label l = new Label(text);
//        l.setStyle("""
//        -fx-font-size: 10;
//        -fx-text-fill: #6b7280;
//        -fx-font-weight: bold;
//        -fx-letter-spacing: 1;
//    """);
//        return l;
//    }
//    private HBox summaryGrid() {
//
//        HBox row = new HBox(12);
//
//        row.getChildren().addAll(
//                infoCard("Architecture", "JavaFX"),
//                infoCard("Structure", "14 Folders")
//        );
//
//        return row;
//    }private VBox infoCard(String title, String value) {
//
//        VBox card = new VBox(6);
//        card.setPadding(new Insets(12));
//
//        card.setStyle("""
//        -fx-background-color: white;
//        -fx-background-radius: 14;
//    """);
//
//        Label t = smallLabel(title);
//
//        Label v = new Label(value);
//        v.setStyle("-fx-font-size: 16; -fx-font-weight: bold;");
//
//        card.getChildren().addAll(t, v);
//        return card;
//    }
//
//
//
//
//    // ---------------- STATUS BAR ----------------
//    private Node createStatusBar() {
//
//        HBox bar = new HBox(20);
//        bar.setPadding(new Insets(6, 12, 6, 12));
//
//        bar.setStyle("-fx-background-color: #2979FF;");
//
//        Label left = new Label("READY");
//        left.setStyle("-fx-text-fill: white;");
//
//        Region spacer = new Region();
//        HBox.setHgrow(spacer, Priority.ALWAYS);
//
//        Label right = new Label("JAVA 17  |  LINE 12");
//        right.setStyle("-fx-text-fill: white;");
//
//        bar.getChildren().addAll(left, spacer, right);
//
//        return bar;
//    }
//    private VBox entryPointCard() {
//
//        VBox card = new VBox(10);
//        card.setPadding(new Insets(12));
//
//        card.setStyle("""
//        -fx-background-color: white;
//        -fx-background-radius: 14;
//    """);
//
//        Label title = smallLabel("ENTRY POINTS");
//
//        Label main = new Label("App.java → JavaFX");
//        main.setStyle("-fx-font-weight: bold;");
//
//        card.getChildren().addAll(title, main);
//        return card;
//    }
//    private HBox metricsGrid() {
//
//        HBox row = new HBox(12);
//
//        row.getChildren().addAll(
//                infoCard("Classes", "156"),
//                infoCard("Avg Complexity", "12.4")
//        );
//
//        return row;
//    }
//}
