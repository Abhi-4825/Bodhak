package com.example.bodhak.ui.analysisReport.uiComponent;


import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Standard header used by every dashboard card.
 *
 * Layout:
 *
 *  [ Icon ]  Title                          Right Content
 *
 * The right side may contain buttons, badges, refresh actions,
 * export actions, info buttons, etc.
 */
public final class DashboardHeader extends HBox {

    private final HBox leftContainer = new HBox(10);

    private final Region spacer = new Region();

    private final HBox rightContainer = new HBox(8);

    private final Label titleLabel = new Label();

    private Node icon;

    public DashboardHeader(String title, Node icon) {

        this.icon = icon;

        initialise(title);
    }

    private void initialise(String title) {

        getStyleClass().add("dashboard-header");

        setAlignment(Pos.CENTER_LEFT);

        setPadding(new Insets(16, 20, 16, 20));

        HBox.setHgrow(spacer, Priority.ALWAYS);

        titleLabel.setText(title);

        titleLabel.getStyleClass().add("dashboard-card-title");

        if (icon != null) {
            icon.getStyleClass().add("dashboard-card-icon");
            leftContainer.getChildren().add(icon);
        }

        leftContainer.getChildren().add(titleLabel);

        leftContainer.setAlignment(Pos.CENTER_LEFT);

        rightContainer.setAlignment(Pos.CENTER_RIGHT);

        getChildren().addAll(
                leftContainer,
                spacer,
                rightContainer
        );
    }

    // -----------------------------------------------------------------------
    // Title
    // -----------------------------------------------------------------------

    public String getTitle() {
        return titleLabel.getText();
    }

    public void setTitle(String title) {
        titleLabel.setText(title);
    }

    public Label getTitleLabel() {
        return titleLabel;
    }

    // -----------------------------------------------------------------------
    // Icon
    // -----------------------------------------------------------------------

    public Node getIcon() {
        return icon;
    }

    public void setIcon(Node icon) {

        if (this.icon != null) {
            leftContainer.getChildren().remove(this.icon);
        }

        this.icon = icon;

        if (icon != null) {

            icon.getStyleClass().add("dashboard-card-icon");

            leftContainer.getChildren().add(0, icon);
        }
    }

    // -----------------------------------------------------------------------
    // Right Content
    // -----------------------------------------------------------------------

    /**
     * Replaces everything on the right side.
     */
    public void setRight(Node node) {

        rightContainer.getChildren().setAll(node);

    }

    /**
     * Adds a new node on the right side.
     */
    public void addRight(Node node) {

        rightContainer.getChildren().add(node);

    }

    /**
     * Clears all right-side actions.
     */
    public void clearRight() {

        rightContainer.getChildren().clear();

    }

    public HBox getRightContainer() {
        return rightContainer;
    }

    public HBox getLeftContainer() {
        return leftContainer;
    }

}
