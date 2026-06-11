package com.example.bodhakfrontend.ui.dashboard.architecture.components;


import com.example.bodhakfrontend.engine.GraphSnapshot;

import com.example.bodhakfrontend.ui.dashboard.common.component.DashboardSection;
import javafx.scene.control.Label;

public class RankingPanel
        extends DashboardSection {

    public RankingPanel(
            GraphSnapshot snapshot
    ) {

        super("Most Depended Upon");

        snapshot.reverseDependencies()
                .entrySet()
                .stream()
                .sorted(
                        (a,b) ->
                                Integer.compare(
                                        b.getValue().size(),
                                        a.getValue().size()
                                )
                )
                .limit(10)
                .forEach(entry -> {

                    add(
                            new Label(
                                    entry.getKey()
                                            + " ("
                                            + entry.getValue().size()
                                            + ")"
                            )
                    );
                });
    }
}
