package com.example.anuviya.ui.settings;

import com.example.anuviya.ui.settings.ai.AISettingsPage;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class SettingsDialog {

    public static void show() {
        Stage stage = new Stage(StageStyle.UTILITY);
        stage.initModality(Modality.APPLICATION_MODAL);
        stage.setTitle("Settings");
        com.example.anuviya.ui.helper.IconHelper.applyWindowIcon(stage);
        stage.setMinWidth(800);
        stage.setMinHeight(550);

        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: #0e1415;");

        // Sidebar categories
        VBox sidebar = new VBox(8);
        sidebar.setPadding(new Insets(16));
        sidebar.setPrefWidth(180);
        sidebar.setStyle("-fx-background-color: #0c1011; -fx-border-color: transparent #1a2122 transparent transparent; -fx-border-width: 1;");

        Label title = new Label("SETTINGS");
        title.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #566465; -fx-letter-spacing: 1px; -fx-padding: 0 0 10 0;");
        sidebar.getChildren().add(title);

        String[] categories = {"General", "Appearance", "Workspace", "Compiler", "AI", "Plugins", "Updates"};
        ToggleGroup group = new ToggleGroup();
        
        StackPane contentArea = new StackPane();
        contentArea.setPadding(new Insets(20));

        // Stub pages
        VBox generalPage = createStubPage("General Settings");
        VBox appearancePage = createStubPage("Appearance Settings");
        VBox workspacePage = createStubPage("Workspace Settings");
        VBox compilerPage = createStubPage("Compiler Settings");
        AISettingsPage aiPage = new AISettingsPage();
        VBox pluginsPage = createStubPage("Plugins Settings");
        VBox updatesPage = createStubPage("Updates Settings");

        for (String cat : categories) {
            ToggleButton btn = new ToggleButton(cat);
            btn.setToggleGroup(group);
            btn.setMaxWidth(Double.MAX_VALUE);
            btn.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
            btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #849494; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 12; -fx-background-radius: 6; -fx-cursor: hand;");
            
            btn.selectedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) {
                    btn.setStyle("-fx-background-color: #1a2122; -fx-text-fill: #4bf6ff; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 12; -fx-background-radius: 6;");
                    contentArea.getChildren().clear();
                    switch (cat) {
                        case "General" -> contentArea.getChildren().add(generalPage);
                        case "Appearance" -> contentArea.getChildren().add(appearancePage);
                        case "Workspace" -> contentArea.getChildren().add(workspacePage);
                        case "Compiler" -> contentArea.getChildren().add(compilerPage);
                        case "AI" -> contentArea.getChildren().add(aiPage);
                        case "Plugins" -> contentArea.getChildren().add(pluginsPage);
                        case "Updates" -> contentArea.getChildren().add(updatesPage);
                    }
                } else {
                    btn.setStyle("-fx-background-color: transparent; -fx-text-fill: #849494; -fx-font-weight: bold; -fx-font-size: 13px; -fx-padding: 8 12; -fx-background-radius: 6; -fx-cursor: hand;");
                }
            });
            sidebar.getChildren().add(btn);
            if ("AI".equals(cat)) {
                // Select AI page by default
                btn.setSelected(true);
            }
        }

        layout.setLeft(sidebar);
        layout.setCenter(contentArea);

        Scene scene = new Scene(layout);
        stage.setScene(scene);
        stage.showAndWait();
    }

    private static VBox createStubPage(String title) {
        VBox box = new VBox(10);
        box.setStyle("-fx-background-color: #0e1415;");
        Label label = new Label(title);
        label.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dde4e5;");
        Label desc = new Label("This configuration page is currently managed under system defaults.");
        desc.setStyle("-fx-font-size: 13px; -fx-text-fill: #849494;");
        box.getChildren().addAll(label, desc);
        return box;
    }
}
