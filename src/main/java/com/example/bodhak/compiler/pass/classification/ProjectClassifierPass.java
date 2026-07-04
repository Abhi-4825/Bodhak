package com.example.bodhak.compiler.pass.classification;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.compiler.CompilerPass;
import com.example.bodhak.compiler.PipelineContext;
import com.example.bodhak.classification.ProjectType;
import com.example.bodhak.classification.classifier.ProjectClassificationResult;
import java.util.*;

/**
 * Compiler pass that classifies the project archetype by scanning entity tags and capabilities.
 */
public class ProjectClassifierPass implements CompilerPass {

    @Override
    public String getName() {
        return "ProjectClassifierPass";
    }

    @Override
    public void execute(PipelineContext context) {
        Map<ProjectType, Double> typeScores = new HashMap<>();
        
        boolean hasRoutes = false;
        boolean hasGui = false;
        boolean hasMain = false;

        for (CompilationUnit cu : context.getCompilationUnits()) {
            for (EntityInfo entity : cu.getEntities()) {
                Set<String> tags = entity.getContribution().tags();
                for (String tag : tags) {
                    String lower = tag.toLowerCase();
                    if (lower.contains("route") || lower.contains("controller") || lower.contains("rest")) {
                        hasRoutes = true;
                    }
                    if (lower.contains("javafx") || lower.contains("swing") || lower.contains("gui")) {
                        hasGui = true;
                    }
                    if (lower.contains("main")) {
                        hasMain = true;
                    }
                }
            }
        }

        if (hasRoutes) {
            typeScores.put(ProjectType.REST_API, 0.9);
            typeScores.put(ProjectType.WEB_APP, 0.8);
        }
        if (hasGui) {
            typeScores.put(ProjectType.DESKTOP_APP, 0.95);
        }
        if (hasMain && !hasRoutes && !hasGui) {
            typeScores.put(ProjectType.CLI_APP, 0.9);
        }
        if (typeScores.isEmpty()) {
            typeScores.put(ProjectType.LIBRARY, 0.7);
        }

        ProjectType primary = typeScores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(ProjectType.UNKNOWN);

        ProjectClassificationResult result = new ProjectClassificationResult(
            typeScores,
            primary,
            null, // capabilityProfile
            Collections.emptyList() // framework detections
        );

        context.setAttribute("project_classification", result);
    }
}
