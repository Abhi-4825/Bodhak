package com.example.bodhak.ui.analysisReport.surface;

import com.example.bodhak.ui.analysisReport.state.ProjectSurfacesState;
import com.example.bodhak.ui.analysisReport.uiComponent.DashboardCard;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

public class ProjectSurfacesCard extends VBox {

    private final ProjectSurfacesState state;
    private final DashboardCard card;
    private final FlowPane surfacesGrid = new FlowPane();

    public ProjectSurfacesCard(ProjectSurfacesState state) {
        this.state = state;
        this.card = new DashboardCard("Project Surfaces", new Label("🎯"));
        initialise();
    }

    private void initialise() {
        getChildren().add(card);
        VBox.setVgrow(card, Priority.ALWAYS);

        surfacesGrid.setHgap(20);
        surfacesGrid.setVgap(20);
        surfacesGrid.setPadding(new Insets(20));
        surfacesGrid.setAlignment(Pos.CENTER);
        
        card.setContent(surfacesGrid);

        state.getSurfaces().addListener((ListChangeListener<ProjectSurfacesState.SurfaceEntry>) c -> {
            surfacesGrid.getChildren().clear();
            for (ProjectSurfacesState.SurfaceEntry entry : state.getSurfaces()) {
                VBox surfaceBox = new VBox(8);
                surfaceBox.setAlignment(Pos.CENTER);
                surfaceBox.setPrefWidth(100);

                Label iconLabel = new Label(entry.icon());
                iconLabel.setStyle("-fx-font-size: 32px; -fx-text-fill: #00e5ff;");

                Label nameLabel = new Label(entry.name());
                nameLabel.getStyleClass().add("metric-title");
                nameLabel.setStyle("-fx-text-fill: #e6f1f3; -fx-alignment: center;");
                nameLabel.setWrapText(true);
                nameLabel.setMaxWidth(90);

                Label detailLabel = new Label(entry.detail());
                detailLabel.getStyleClass().add("metric-subtitle");

                surfaceBox.getChildren().addAll(iconLabel, nameLabel, detailLabel);
                surfacesGrid.getChildren().add(surfaceBox);
            }
        });
    }
}
