package com.example.bodhakfrontend.ui.rightPanel;

import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.File;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
public class RightPanelTabManager {

    private final TabPane rightTabPane;
    private final Map<String, Tab> singletonTabs = new HashMap<>();
    private final Map<Path, Tab> overviewTabs = new HashMap<>();

    public RightPanelTabManager(TabPane rightTabPane) {
        this.rightTabPane = rightTabPane;
    }

    private Node wrapWithSlideIn(Node content) {
        StackPane wrapper = new StackPane(content);
        content.setTranslateX(40);
        TranslateTransition tt = new TranslateTransition(Duration.millis(220), content);
        tt.setFromX(40);
        tt.setToX(0);
        tt.play();
        return wrapper;
    }

    public void openAnalyzeTab(Supplier<Node> analyzeContent) {
        Tab tab = singletonTabs.get("ANALYZE");

        if (tab == null) {
            tab = new Tab("Analyze");
            tab.setClosable(true);
            singletonTabs.put("ANALYZE", tab);
            rightTabPane.getTabs().add(tab);

            tab.setOnClosed(e -> singletonTabs.remove("ANALYZE"));
        }

        tab.setContent(wrapWithSlideIn(analyzeContent.get()));
        rightTabPane.getSelectionModel().select(tab);
    }

    public void openOverviewTab(File file, Node overviewContent) {
        Path path = file.toPath().toAbsolutePath().normalize();
        Tab tab = overviewTabs.get(path);

        if (tab == null) {
            tab = new Tab(file.getName());
            tab.setClosable(true);
            tab.setContent(wrapWithSlideIn(overviewContent));
            tab.setUserData(path);

            overviewTabs.put(path, tab);
            rightTabPane.getTabs().add(tab);

            tab.setOnClosed(e -> overviewTabs.remove(path));
        }

        rightTabPane.getSelectionModel().select(tab);
    }

    public void refreshOverviewTabs(Function<File, Node> builder) {
        overviewTabs.forEach((path, tab) -> {
            tab.setContent(builder.apply(path.toFile()));
        });
    }

    public void closeOverviewTabs(Path path) {
        if (path == null) return;

        Path normalized = path.toAbsolutePath().normalize();

        rightTabPane.getTabs().removeIf(tab ->
                tab.getUserData() instanceof Path p &&
                        p.equals(normalized)
        );

        overviewTabs.remove(normalized);
    }


    public void refreshAnalyzeTabIfOpen(Supplier<Node> analyzeContent) {

        Tab tab = singletonTabs.get("ANALYZE");

        if (tab == null) {
            return;
        }
        boolean wasSelected =
                rightTabPane.getSelectionModel().getSelectedItem() == tab;

        // Refresh content
        tab.setContent(analyzeContent.get());


        if (wasSelected) {
            rightTabPane.getSelectionModel().select(tab);
        }

    }


    public void clear() {
        singletonTabs.clear();
        overviewTabs.clear();
        rightTabPane.getTabs().clear();
    }

    public void openOptimizationTab(Supplier<Node> contentSupplier) {

        Tab tab = new Tab("Optimization");
        tab.setContent(contentSupplier.get());

        rightTabPane.getTabs().removeIf(t ->
                t.getText().equals("Optimization"));

        rightTabPane.getTabs().add(tab);
        rightTabPane.getSelectionModel().select(tab);
    }




}
