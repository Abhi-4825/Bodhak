package com.example.bodhakfrontend.ui.rightPanel;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class RightPanelTabManager {
    private final TabPane rightTabPane;
    private  final Map<String, Tab> singletonTabs=new HashMap<>();
    private final Map<String, Tab> overviewTabs=new HashMap<>();
    public RightPanelTabManager(TabPane rightTabPane) {
        this.rightTabPane = rightTabPane;
    }

    public void openAnalyzeTab(Node analyzeContent) {
        Tab tab = singletonTabs.get("ANALYZE");

        if (tab == null) {
            tab = new Tab("Analyze");
            tab.setClosable(true);
            singletonTabs.put("ANALYZE", tab);
            rightTabPane.getTabs().add(tab);
        }
        tab.setOnClosed(event -> {
            singletonTabs.clear();
        });

        tab.setContent(analyzeContent);
        rightTabPane.getSelectionModel().select(tab);
    }

    public void openOverviewTab(File file, Node overviewContent) {
        String key = file.getAbsolutePath();
        Tab tab = overviewTabs.get(key);

        if (tab == null) {
            tab = new Tab(file.getName());
            tab.setContent(overviewContent);
            tab.setClosable(true);

            overviewTabs.put(key, tab);
            rightTabPane.getTabs().add(tab);

            tab.setOnClosed(e -> overviewTabs.remove(key));
        }

        rightTabPane.getSelectionModel().select(tab);
    }

    public void refreshOverviewTabs(
            Function<File, Node> overviewContentBuilder
    ) {
        for (Map.Entry<String, Tab> entry : overviewTabs.entrySet()) {
            File file = new File(entry.getKey());
            Tab tab = entry.getValue();

            Node refreshedContent = overviewContentBuilder.apply(file);
            tab.setContent(refreshedContent);
        }
    }




}
