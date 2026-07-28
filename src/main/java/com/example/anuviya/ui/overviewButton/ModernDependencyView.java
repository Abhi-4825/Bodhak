package com.example.anuviya.ui.overviewButton;


import com.example.anuviya.model.entity.EntityViewModel;
import com.example.anuviya.ui.DependencyGraphWindow;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.*;

import java.util.Map;

public class ModernDependencyView {

    private final Map<String, EntityViewModel> vmMap;

    public ModernDependencyView(Map<String, EntityViewModel> vmMap) {
        this.vmMap = vmMap;
    }

    public Node show(String entityName) {

        EntityViewModel vm = vmMap.get(entityName);

        VBox root = new VBox(30);
        root.getStyleClass().add("modern-dependency-root");
        root.setPadding(new Insets(30));

        // ================= HERO CARD =================

        HBox heroCard = createHeroCard(vm);

        // ================= DEPENDENCY SECTION =================

        HBox dependencySection = new HBox(30);

        VBox dependsOnBox = createDependencyColumn(
                "DEPENDS ON",
                vm,
                true
        );

        VBox usedByBox = createDependencyColumn(
                "USED BY",
                vm,
                false
        );

        HBox.setHgrow(dependsOnBox, Priority.ALWAYS);
        HBox.setHgrow(usedByBox, Priority.ALWAYS);

        dependencySection.getChildren().addAll(
                dependsOnBox,
                usedByBox
        );

        root.getChildren().addAll(
                heroCard,
                dependencySection
        );

        ScrollPane scrollPane = new ScrollPane(root);
        scrollPane.setFitToWidth(true);
        scrollPane.getStyleClass().add("modern-scroll");

        return scrollPane;
    }

    // =========================================================



    // =========================================================

    private HBox createHeroCard(EntityViewModel vm) {

        HBox hero = new HBox(20);
        hero.setAlignment(Pos.CENTER_LEFT);

        hero.getStyleClass().add("hero-card");

        VBox textBox = new VBox(8);

        Label className = new Label(vm.toEntityInfo().getSimpleName());
        className.getStyleClass().add("hero-title");

        Label sub = new Label(
                "ROOT CLASS • " + vm.toEntityInfo().getNamespaceName()
        );

        sub.getStyleClass().add("hero-subtitle");

        textBox.getChildren().addAll(className, sub);
        Button visualize=new Button("visualize");
        visualize.getStyleClass().add("action-btn-secondary");
        visualize.setOnAction(e -> {
            DependencyGraphWindow.show(vm.getEntityName(),vmMap);
        });


        Region spacer = new Region();

        HBox.setHgrow(
                spacer,
                Priority.ALWAYS
        );

        hero.getChildren().addAll(
                textBox,
                spacer,
                visualize
        );

        return hero;
    }

    // =========================================================

    private VBox createDependencyColumn(
            String title,
            EntityViewModel vm,
            boolean dependsOn
    ) {

        VBox column = new VBox(20);

        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("section-title");

        VBox cards = new VBox(15);

        var dependencies = dependsOn
                ? vm.getDependsOn()
                : vm.getUsedBy();

        for (String dep : dependencies) {

            EntityViewModel depVm = vmMap.get(dep);

            if (depVm == null) continue;

            cards.getChildren().add(
                    createDependencyCard(depVm)
            );
        }

        column.getChildren().addAll(
                titleLabel,
                cards
        );

        return column;
    }

    // =========================================================

    private HBox createDependencyCard(EntityViewModel vm) {

        HBox card = new HBox();
        card.setAlignment(Pos.CENTER_LEFT);

        card.getStyleClass().add("dependency-card");

        VBox text = new VBox(5);

        Label name = new Label(vm.toEntityInfo().getSimpleName());
        name.getStyleClass().add("dependency-title");

        Label pkg = new Label(vm.toEntityInfo().getNamespaceName());
        pkg.getStyleClass().add("dependency-subtitle");

        text.getChildren().addAll(name, pkg);

        card.getChildren().add(text);

        return card;
    }

}
