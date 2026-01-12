package com.example.bodhakfrontend.ui.rightPanel;

import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

public class RightPanelTabManager {
    private final TabPane rightTabPane;
    private  final Map<String, Tab> singletonTabs=new HashMap<>();
    private final Map<String, Tab> overviewTabs=new HashMap<>();
    public RightPanelTabManager(TabPane rightTabPane) {
        this.rightTabPane = rightTabPane;
    }

    public void openAnalyzeTab(Supplier<Node> analyzeContent) {
        Tab tab = singletonTabs.get("ANALYZE");

        if (tab == null) {
            tab = new Tab("Analyze");
            tab.setClosable(true);
            singletonTabs.put("ANALYZE", tab);
            rightTabPane.getTabs().add(tab);
        }

        tab.setContent(null);
        tab.setContent(analyzeContent.get());
        rightTabPane.getSelectionModel().select(tab);
        tab.setOnClosed(event -> {
            singletonTabs.remove("ANALYZE");
        });
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


    public void clear() {
        singletonTabs.clear();
        overviewTabs.clear();
    }
}
