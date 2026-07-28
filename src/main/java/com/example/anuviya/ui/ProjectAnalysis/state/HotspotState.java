package com.example.anuviya.ui.ProjectAnalysis.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.quality.hotspot.HotspotAnalyzer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class HotspotState {

    private final ObservableList<HotspotItem> hotspots =
            FXCollections.observableArrayList();

    private final HotspotAnalyzer analyzer =
            new HotspotAnalyzer();

    public void update(AnalysisContext context) {

        hotspots.setAll(

                analyzer.analyze(context)
                        .stream()
                        .map(HotspotItem::new)
                        .toList()

        );
    }

    public ObservableList<HotspotItem> getHotspots() {
        return hotspots;
    }
}
