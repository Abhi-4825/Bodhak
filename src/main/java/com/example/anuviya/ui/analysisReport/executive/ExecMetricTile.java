package com.example.anuviya.ui.analysisReport.executive;

import javafx.beans.property.StringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

/**
 * A single glassmorphism metric tile for the Executive Overview banner.
 *
 * Layout:
 *
 * ┌─[4px accent bar]─────────────────────────────────────┐
 * │ LABEL TINY                         [icon]            │
 * │                                                      │
 * │ 1,248 (large value)                                  │
 * │ subtitle (optional)                                  │
 * └──────────────────────────────────────────────────────┘
 *
 * Supports:
 * • Material Symbols
 * • PNG / JPG / SVG ImageView
 * • Any custom JavaFX Node
 */
public final class ExecMetricTile extends HBox {

    private final Label valueLabel = new Label();
    private final StackPane iconHolder = new StackPane();

    private VBox content;
    private Node subtitleNode;

    public ExecMetricTile(
            String labelText,
            String materialIconName,
            String accentBarStyleClass,
            String iconStyleClass
    ) {

        // Accent bar
        Region accentBar = new Region();
        accentBar.setPrefWidth(4);
        accentBar.setMinWidth(4);
        accentBar.setMaxWidth(4);
        accentBar.setMaxHeight(Double.MAX_VALUE);

        if (accentBarStyleClass != null && !accentBarStyleClass.isBlank()) {
            accentBar.getStyleClass().add(accentBarStyleClass);
        }

        // Content
        content = new VBox(4);
        content.setAlignment(Pos.TOP_LEFT);
        content.setPadding(new Insets(12, 14, 12, 12));
        HBox.setHgrow(content, Priority.ALWAYS);

        // Small title
        Label tinyLabel = new Label(labelText);
        tinyLabel.getStyleClass().add("ar-label-tiny");

        // Icon holder
        if (iconStyleClass != null && !iconStyleClass.isBlank()) {
            iconHolder.getStyleClass().add(iconStyleClass);
        }

        if (materialIconName != null && !materialIconName.isBlank()) {
            setMaterialIcon(materialIconName);
        }

        // Top row
        HBox topRow = new HBox();
        topRow.setAlignment(Pos.CENTER_LEFT);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        topRow.getChildren().addAll(tinyLabel, spacer, iconHolder);

        // Value
        valueLabel.getStyleClass().add("ar-value-xl");
        valueLabel.setWrapText(false);

        content.getChildren().addAll(topRow, valueLabel);

        // Root
        getStyleClass().add("ar-exec-tile");
        setAlignment(Pos.CENTER_LEFT);

        setMinWidth(USE_PREF_SIZE);
        setPrefWidth(USE_COMPUTED_SIZE);
        setMaxWidth(Double.MAX_VALUE);

        getChildren().addAll(accentBar, content);
    }

    // -------------------------------------------------------------------------
    // Properties
    // -------------------------------------------------------------------------

    public StringProperty valueProperty() {
        return valueLabel.textProperty();
    }

    public Label getValueLabel() {
        return valueLabel;
    }

    // -------------------------------------------------------------------------
    // Icons
    // -------------------------------------------------------------------------

    /**
     * Uses Google's Material Symbols font.
     */
    public void setMaterialIcon(String iconName) {

        Label icon = new Label(iconName);

        icon.setStyle("""
                -fx-font-family: 'Material Symbols Outlined';
                -fx-font-size: 18px;
                -fx-text-fill: #00daf3;
                -fx-opacity: 0.45;
                """);

        iconHolder.getChildren().setAll(icon);
    }

    /**
     * Loads an image from the resources folder.
     *
     * Example:
     * setImageIcon("/icons/java.png",18);
     */
    public void setImageIcon(String resourcePath, double size) {

        Image image = new Image(getClass().getResourceAsStream(resourcePath));

        setImageIcon(image, size);
    }

    /**
     * Uses an already loaded Image.
     */
    public void setImageIcon(Image image, double size) {

        ImageView imageView = new ImageView(image);

        imageView.setFitWidth(size);
        imageView.setFitHeight(size);
        imageView.setPreserveRatio(true);
        imageView.setSmooth(true);

        iconHolder.getChildren().setAll(imageView);
    }

    /**
     * Uses an existing ImageView.
     */
    public void setImageIcon(ImageView imageView) {
        iconHolder.getChildren().setAll(imageView);
    }

    /**
     * Allows any JavaFX node as the icon.
     */
    public void setIcon(Node node) {
        iconHolder.getChildren().setAll(node);
    }

    /**
     * Backwards compatibility.
     */
    public void setCustomIcon(Node node) {
        setIcon(node);
    }

    // -------------------------------------------------------------------------
    // Subtitle
    // -------------------------------------------------------------------------

    /**
     * Adds or replaces the subtitle shown below the value.
     */
    public void setSubtitleNode(Node node) {

        if (subtitleNode != null) {
            content.getChildren().remove(subtitleNode);
        }

        subtitleNode = node;

        if (node != null) {
            content.getChildren().add(node);
        }
    }
}