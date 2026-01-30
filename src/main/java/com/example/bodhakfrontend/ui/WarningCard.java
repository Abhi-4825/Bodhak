package com.example.bodhakfrontend.ui;

import com.example.bodhakfrontend.Models.WarningRule;
import com.example.bodhakfrontend.util.SeverityStyle;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class WarningCard {
    public static Node create(WarningRule rule){
        Label icon =new  Label(
                SeverityStyle.icon(rule.getSeverity())
        );
        Label text=new  Label(rule.getMessage());
        text.setWrapText(true);
        Label level = new Label(rule.getSeverity().name());
        level.getStyleClass().add("label-bold");
        switch (rule.getSeverity()) {
            case HIGH -> level.getStyleClass().add("label-severity-high");
            case MEDIUM -> level.getStyleClass().add("label-severity-medium");
            case LOW -> level.getStyleClass().add("label-severity-low");
        }
        VBox content = new VBox(level, text);
        content.setSpacing(4);

        HBox card = new HBox(icon, content);
        card.setSpacing(10);
        card.setPadding(new Insets(10));
        card.getStyleClass().add("warning-card");
        switch (rule.getSeverity()) {
            case HIGH -> card.getStyleClass().add("warning-card-high");
            case MEDIUM -> card.getStyleClass().add("warning-card-medium");
            case LOW -> card.getStyleClass().add("warning-card-low");
        }

        return card;


    }


}
