package com.example.anuviya.ui.analysisReport.uiComponent;


import javafx.beans.property.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

/**
 * Small reusable metric card used throughout the Analysis Report dashboard.
 *
 * Layout:
 *
 *  +---------------------------+
 *  |  Icon                     |
 *  |                           |
 *  |  1,248                    |
 *  |  Compilation Units        |
 *  |  Optional Subtitle        |
 *  |  Optional ProgressBar     |
 *  +---------------------------+
 */
public final class MetricTile extends VBox {

    private final ObjectProperty<Node> icon =
            new SimpleObjectProperty<>();

    private final ObjectProperty value =
            new SimpleObjectProperty("");

    private final StringProperty title =
            new SimpleStringProperty("");

    private final StringProperty subtitle =
            new SimpleStringProperty("");

    private final DoubleProperty progress =
            new SimpleDoubleProperty(-1);

    private final VBox iconContainer =
            new VBox();

    private final Label valueLabel =
            new Label();

    private final Label titleLabel =
            new Label();

    private final Label subtitleLabel =
            new Label();

    private final ProgressBar progressBar =
            new ProgressBar();

    public MetricTile() {

        initialise();

    }

    public MetricTile(
            String title,
            String value,
            Node icon
    ) {

        initialise();

        setTitle(title);

        setValue(value);

        setIcon(icon);

    }

    private void initialise() {

        getStyleClass().add("metric-tile");

        setSpacing(8);

        setPadding(new Insets(18));

        setAlignment(Pos.TOP_LEFT);

        setPrefWidth(180);

        setMinWidth(160);

        VBox.setVgrow(this, Priority.ALWAYS);

        iconContainer.setAlignment(Pos.CENTER_LEFT);

        icon.addListener((obs, oldIcon, newIcon) -> {

            iconContainer.getChildren().clear();

            if (newIcon != null) {

                newIcon.getStyleClass().add("metric-tile-icon");

                iconContainer.getChildren().add(newIcon);

            }

        });

        valueLabel.textProperty().bind(value);

        titleLabel.textProperty().bind(title);

        subtitleLabel.textProperty().bind(subtitle);

        valueLabel.getStyleClass().add("metric-value");

        titleLabel.getStyleClass().add("metric-title");

        subtitleLabel.getStyleClass().add("metric-subtitle");

        subtitleLabel.visibleProperty().bind(

                subtitle.isNotEmpty()

        );

        subtitleLabel.managedProperty().bind(

                subtitleLabel.visibleProperty()

        );

        progressBar.progressProperty().bind(progress);

        progressBar.visibleProperty().bind(

                progress.greaterThanOrEqualTo(0)

        );

        progressBar.managedProperty().bind(

                progressBar.visibleProperty()

        );

        progressBar.setMaxWidth(Double.MAX_VALUE);

        Region spacer = new Region();

        VBox.setVgrow(spacer, Priority.ALWAYS);

        getChildren().addAll(

                iconContainer,

                valueLabel,

                titleLabel,

                subtitleLabel,

                spacer,

                progressBar

        );

    }

    // ---------------------------------------------------
    // Icon
    // ---------------------------------------------------

    public ObjectProperty<Node> iconProperty() {

        return icon;

    }

    public void setIcon(Node icon) {

        this.icon.set(icon);

    }

    public Node getIcon() {

        return icon.get();

    }

    // ---------------------------------------------------
    // Value
    // ---------------------------------------------------

    public ObjectProperty valueProperty() {

        return value;

    }

    public void setValue(String value) {

        this.value.set(value);

    }

    public Object getValue() {

        return value;

    }

    // ---------------------------------------------------
    // Title
    // ---------------------------------------------------

    public StringProperty titleProperty() {

        return title;

    }

    public void setTitle(String title) {

        this.title.set(title);

    }

    public String getTitle() {

        return title.get();

    }

    // ---------------------------------------------------
    // Subtitle
    // ---------------------------------------------------

    public StringProperty subtitleProperty() {

        return subtitle;

    }

    public void setSubtitle(String subtitle) {

        this.subtitle.set(subtitle);

    }

    public String getSubtitle() {

        return subtitle.get();

    }

    // ---------------------------------------------------
    // Progress
    // ---------------------------------------------------

    public DoubleProperty progressProperty() {

        return progress;

    }

    public void setProgress(double value) {

        progress.set(value);

    }

    public double getProgress() {

        return progress.get();

    }

}
