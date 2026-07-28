package com.example.anuviya.ui.analysisReport.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.quality.flag.EntityFlag;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.stream.Collectors;

public class RiskHotspotsState implements AnalysisReportSection {

    public record OversizedEntry(
        String name,
        String pkg,
        int loc,
        long methodCount,
        int fieldCount
    ) {}

    public record InstableEntry(
        String name,
        String pkg,
        double instability,
        int fanOut,
        int fanIn,
        String stability
    ) {}

    public record InspectorEntry(
        String name,
        String pkg,
        String flags,
        String riskLevel,
        int loc,
        long methodCount,
        int fieldCount,
        int fanOut,
        int fanIn,
        double instability
    ) {}

    private final ObservableList<OversizedEntry> oversizedHotspots = FXCollections.observableArrayList();
    private final ObservableList<InstableEntry> instableHotspots = FXCollections.observableArrayList();
    private final ObservableList<InspectorEntry> allHotspots = FXCollections.observableArrayList();

    @Override
    public void update(AnalysisContext context) {
        oversizedHotspots.clear();
        instableHotspots.clear();
        allHotspots.clear();

        if (context == null || context.getCharacteristics() == null) return;

        var characteristics = context.getCharacteristics();

        // 1. Oversized Entities: Filter by OVERSIZED_ENTITY flag
        characteristics.stream()
                .filter(c -> c.flags().contains(EntityFlag.OVERSIZED_ENTITY))
                .sorted((a, b) -> Integer.compare(b.entityInfo().getLinesOfCode(), a.entityInfo().getLinesOfCode()))
                .limit(2) // only show up to 2
                .forEach(c -> {
                    var info = c.entityInfo();
                    oversizedHotspots.add(new OversizedEntry(
                        info.getSimpleName(),
                        info.getNamespaceName(),
                        info.getLinesOfCode(),
                        info.getMethodCount(),
                        info.getFields().size()
                    ));
                });

        // 2. Highly Instable Entities: Filter by HIGH_INSTABILITY flag
        characteristics.stream()
                .filter(c -> c.flags().contains(EntityFlag.HIGH_INSTABILITY))
                .sorted((a, b) -> {
                    double instA = calculateInstability(a.entityInfo());
                    double instB = calculateInstability(b.entityInfo());
                    return Double.compare(instB, instA);
                })
                .limit(2) // only show up to 2
                .forEach(c -> {
                    var info = c.entityInfo();
                    double inst = calculateInstability(info);
                    String stability = inst >= 0.9 ? "Very Low" : (inst >= 0.7 ? "Low" : "Medium");
                    instableHotspots.add(new InstableEntry(
                        info.getSimpleName(),
                        info.getNamespaceName(),
                        inst,
                        info.getDependsOn().size(),
                        info.getUsedBy().size(),
                        stability
                    ));
                });

        // 3. All Hotspots (with any flags) for Inspector
        characteristics.stream()
                .filter(c -> !c.flags().isEmpty())
                .sorted((a, b) -> Integer.compare(b.flags().size(), a.flags().size()))
                .forEach(c -> {
                    var info = c.entityInfo();
                    String flagsStr = c.flags().stream()
                            .map(Enum::name)
                            .collect(Collectors.joining(", "));
                    String riskLevel = c.flags().size() >= 3 ? "HIGH" : (c.flags().size() == 2 ? "MEDIUM" : "LOW");
                    double inst = calculateInstability(info);
                    
                    allHotspots.add(new InspectorEntry(
                        info.getSimpleName(),
                        info.getNamespaceName(),
                        flagsStr,
                        riskLevel,
                        info.getLinesOfCode(),
                        info.getMethodCount(),
                        info.getFields().size(),
                        info.getDependsOn().size(),
                        info.getUsedBy().size(),
                        inst
                    ));
                });
    }

    private double calculateInstability(com.example.anuviya.model.entity.EntityInfo info) {
        double fanOut = info.getDependsOn().size();
        double fanIn = info.getUsedBy().size();
        return (fanIn + fanOut) > 0 ? fanOut / (fanIn + fanOut) : 0.0;
    }

    public ObservableList<OversizedEntry> getOversizedHotspots() { return oversizedHotspots; }
    public ObservableList<InstableEntry> getInstableHotspots() { return instableHotspots; }
    public ObservableList<InspectorEntry> getAllHotspots() { return allHotspots; }
}
