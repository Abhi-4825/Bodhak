package com.example.anuviya.ui.dependencyExplorer.components;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.ui.dependencyExplorer.state.DependencyExplorerState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;

public class EntityBrowserPanel extends VBox {

    private final DependencyExplorerState state;
    private final ListView<EntityInfo> listView = new ListView<>();

    public EntityBrowserPanel(DependencyExplorerState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        getStyleClass().add("dd-card");
        setSpacing(10);

        Label label = new Label("ENTITY LIST");
        label.getStyleClass().add("dd-card-title");

        TextField searchField = new TextField();
        searchField.setPromptText("Filter entities...");
        searchField.setStyle("-fx-background-color: #0d141a; -fx-text-fill: #dce3ec; -fx-prompt-text-fill: #849396; -fx-border-color: rgba(132, 147, 150, 0.1); -fx-border-radius: 4; -fx-background-radius: 4; -fx-padding: 4 8;");
        state.getEntityBrowserState().searchQueryProperty().bindBidirectional(searchField.textProperty());

        listView.setStyle("-fx-background-color: transparent; -fx-background-insets: 0; -fx-padding: 0;");
        listView.setItems(state.getEntityBrowserState().getFilteredEntities());
        VBox.setVgrow(listView, Priority.ALWAYS);

        // Setup custom empty placeholder
        Label emptyPlaceholder = new Label("No entities found");
        emptyPlaceholder.setStyle("-fx-text-fill: #849396; -fx-font-size: 11px; -fx-font-family: 'JetBrains Mono';");
        listView.setPlaceholder(emptyPlaceholder);

        // Custom Cell Factory to render entity info & active highlight
        listView.setCellFactory(lv -> new ListCell<EntityInfo>() {
            @Override
            protected void updateItem(EntityInfo item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("-fx-background-color: transparent;");
                } else {
                    boolean isActive = state.selectedEntityProperty().get() == item;

                    VBox box = new VBox(2);
                    box.setPadding(new Insets(2, 0, 2, 0));

                    HBox topRow = new HBox(8);
                    topRow.setAlignment(Pos.CENTER_LEFT);

                    Label nameLbl = new Label(item.getSimpleName());
                    nameLbl.setStyle("-fx-text-fill: " + (isActive ? "#00daf3" : "#dce3ec") + "; -fx-font-weight: bold; -fx-font-size: 12px;");

                    Label kindLbl = new Label(item.getKind().name());
                    kindLbl.setStyle("-fx-font-size: 8px; -fx-text-fill: #00daf3; -fx-background-color: rgba(0, 218, 243, 0.1); -fx-padding: 1 4; -fx-background-radius: 3;");

                    topRow.getChildren().addAll(nameLbl, kindLbl);

                    Label nsLbl = new Label(item.getNamespaceName());
                    nsLbl.setStyle("-fx-font-size: 10px; -fx-text-fill: #849396;");

                    box.getChildren().addAll(topRow, nsLbl);
                    setGraphic(box);

                    // Active Focused Item Highlighting
                    if (isActive) {
                        setStyle("-fx-background-color: rgba(0, 218, 243, 0.12); -fx-padding: 6 8; -fx-background-radius: 4; -fx-border-color: #00daf3; -fx-border-width: 0 0 0 3;");
                    } else {
                        setStyle("-fx-background-color: transparent; -fx-padding: 6 8;");
                    }
                }
            }

            {
                // Hover highlight transitions
                setOnMouseEntered(e -> {
                    if (getItem() != null && state.selectedEntityProperty().get() != getItem()) {
                        setStyle("-fx-background-color: rgba(132, 147, 150, 0.08); -fx-padding: 6 8; -fx-background-radius: 4;");
                    }
                });
                setOnMouseExited(e -> {
                    if (getItem() != null && state.selectedEntityProperty().get() != getItem()) {
                        setStyle("-fx-background-color: transparent; -fx-padding: 6 8;");
                    }
                });
            }
        });

        // Sync list selection with selectedEntity state changes
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && state.selectedEntityProperty().get() != newVal) {
                state.selectedEntityProperty().set(newVal);
            }
        });

        state.selectedEntityProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                listView.getSelectionModel().select(newVal);
                listView.scrollTo(newVal);
            } else {
                listView.getSelectionModel().clearSelection();
            }
            listView.refresh(); // Force repaint to update active cell borders
        });

        // Trigger initial selection
        if (state.selectedEntityProperty().get() != null) {
            listView.getSelectionModel().select(state.selectedEntityProperty().get());
        }

        getChildren().addAll(label, searchField, listView);
    }
}
