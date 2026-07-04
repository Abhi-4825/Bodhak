package com.example.bodhak.compiler.pass.flag;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.quality.flag.*;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.compiler.CompilerPass;
import com.example.bodhak.compiler.PipelineContext;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Compiler pass that analyzes entity metrics and structures to detect design smells
 * (e.g. God Class, Anemic Domain Model) and sets entity characteristics.
 */
public class QualityAnalysisPass implements CompilerPass {

    private final ProjectBaselineCalculator baselineCalculator = new ProjectBaselineCalculator();

    @Override
    public String getName() {
        return "QualityAnalysisPass";
    }

    @Override
    public void execute(PipelineContext context) {
        List<EntityInfo> allEntities = new ArrayList<>();
        for (CompilationUnit cu : context.getCompilationUnits()) {
            allEntities.addAll(cu.getEntities());
        }

        ProjectBaselines baselines = baselineCalculator.calculate(allEntities);
        EntityFlagAnalyzer flagAnalyzer = new EntityFlagAnalyzer(ArchitectureThresholds.defaults(), baselines);

        List<EntityCharacteristics> characteristics = allEntities.stream()
                .map(entity -> new EntityCharacteristics(entity, flagAnalyzer.analyze(entity)))
                .collect(Collectors.toList());

        context.setAttribute("project_baselines", baselines);
        context.setAttribute("entity_characteristics", characteristics);
    }
}
