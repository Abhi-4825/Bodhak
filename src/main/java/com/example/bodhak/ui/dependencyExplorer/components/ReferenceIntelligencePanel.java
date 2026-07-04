package com.example.bodhak.ui.dependencyExplorer.components;

import com.example.bodhak.ui.dependencyExplorer.state.BreakdownState;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.*;

public class ReferenceIntelligencePanel extends VBox {

    private final BreakdownState state;

    public ReferenceIntelligencePanel(BreakdownState state) {
        this.state = state;
        initialise();
    }

    private void initialise() {
        getStyleClass().add("dd-card");
        setSpacing(12);

        Label title = new Label("REFERENCE INTELLIGENCE");
        title.getStyleClass().add("dd-card-title");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        VBox.setVgrow(grid, Priority.ALWAYS);

        // Grid (3 Columns x 2 Rows)
        grid.add(createBlock("Method Calls", "Calls between methods", state.methodCallsProperty().asString(), "code", "#00daf3", "Represents invocation of methods belonging to other classes."), 0, 0);
        grid.add(createBlock("Field References", "Member access", state.fieldAccessesProperty().asString(), "grain", "#00e676", "Represents reading or writing member variables of other classes."), 1, 0);
        grid.add(createBlock("Type References", "Class/interface usage", state.typeReferencesProperty().asString(), "schema", "#ffa726", "Represents references to type names in variable declarations, castings, etc."), 2, 0);

        grid.add(createBlock("Inheritance", "extends / implements", state.otherProperty().asString(), "account_tree", "#ab47bc", "Represents sub-classing or interface implementations."), 0, 1);
        grid.add(createBlock("Annotations", "Framework annotations", state.annotationsProperty().asString(), "alternate_email", "#ec407a", "Represents references to compiler or framework annotations."), 1, 1);
        grid.add(createBlock("Imports", "Import statements", state.importsProperty().asString(), "download", "#26a69a", "Represents explicit imports of package namespaces."), 2, 1);

        // Make columns expand equally
        ColumnConstraints c1 = new ColumnConstraints(); c1.setPercentWidth(33.3);
        ColumnConstraints c2 = new ColumnConstraints(); c2.setPercentWidth(33.3);
        ColumnConstraints c3 = new ColumnConstraints(); c3.setPercentWidth(33.3);
        grid.getColumnConstraints().addAll(c1, c2, c3);

        getChildren().addAll(title, grid);
    }

    private HBox createBlock(String labelStr, String descStr, javafx.beans.value.ObservableValue<String> valProp, String iconName, String colorHex, String tooltipText) {
        HBox block = new HBox(8);
        block.setAlignment(Pos.CENTER_LEFT);
        block.setStyle("-fx-background-color: #1a2027; -fx-padding: 8 10; -fx-background-radius: 6; -fx-cursor: hand;");

        Label icon = new Label(iconName);
        icon.setStyle("-fx-font-family: 'Material Symbols Outlined'; -fx-font-size: 20px; -fx-text-fill: " + colorHex + ";");
        icon.setMinWidth(Region.USE_PREF_SIZE);

        VBox text = new VBox(1);
        Label val = new Label();
        val.textProperty().bind(valProp);
        val.setStyle("-fx-font-family: 'JetBrains Mono'; -fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #dce3ec;");
        
        Label lbl = new Label(labelStr);
        lbl.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: #dce3ec;");
        
        Label desc = new Label(descStr);
        desc.setStyle("-fx-font-size: 8px; -fx-text-fill: #849396;");
        
        text.getChildren().addAll(val, lbl, desc);

        block.getChildren().addAll(icon, text);
        HBox.setHgrow(text, Priority.ALWAYS);

        // Hover Effect
        block.setOnMouseEntered(e -> block.setStyle("-fx-background-color: #242b31; -fx-padding: 8 10; -fx-background-radius: 6; -fx-cursor: hand;"));
        block.setOnMouseExited(e -> block.setStyle("-fx-background-color: #1a2027; -fx-padding: 8 10; -fx-background-radius: 6; -fx-cursor: hand;"));

        // Setup Tooltip
        Tooltip tooltip = new Tooltip(tooltipText);
        tooltip.setStyle("-fx-font-size: 11px; -fx-background-color: #151c23; -fx-text-fill: #dce3ec; -fx-border-color: rgba(132,147,150,0.15);");
        Tooltip.install(block, tooltip);

        return block;
    }
}
