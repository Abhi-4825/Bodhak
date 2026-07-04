package com.example.bodhak.analyzer.ai.prompt;

import com.example.bodhak.analyzer.ai.evidence.model.architectur.ArchitectureAnalysisEvidence;
import com.example.bodhak.analyzer.ai.evidence.model.CircularDependencyEvidence;


import java.util.List;


public class ArchitecturePromptBuilder implements PromptBuilder <ArchitectureAnalysisEvidence>{

    private static final int MAX_ENTITIES_PER_SECTION = 10;
    @Override
    public String build(ArchitectureAnalysisEvidence evidence) {



        StringBuilder prompt = new StringBuilder();

        appendHeader(prompt);
        appendProjectSummary(prompt, evidence);
        appendCircularGroups(prompt, evidence);
        appendGodClasses(prompt, evidence);
        appendHighlyCoupledEntities(prompt, evidence);
        appendGrowthMetrics(
                prompt,
                evidence
        );
        appendBetweennessEvidence(
                prompt,
                evidence
        );
        appendInstructions(prompt);

        return prompt.toString();
    }
    private void appendBetweennessEvidence(

            StringBuilder prompt,

            ArchitectureAnalysisEvidence evidence

    ) {

        prompt.append("""

BETWEENNESS CENTRALITY EVIDENCE

Higher centrality means the entity appears on many dependency paths between other entities.

Entities with unusually high centrality may become architectural bottlenecks or change propagation hubs.

""");

        evidence.betweennessCentralities()
                .stream()
                .limit(10)
                .forEach(node -> {

                    prompt.append("\n----------------------------------\n");

                    prompt.append("Entity: ")
                            .append(node.entity())
                            .append("\n");

                    prompt.append("Centrality Score: ")
                            .append(
                                    String.format(
                                            "%.2f",
                                            node.score()
                                    )
                            )
                            .append("\n");
                });
    }

    private void appendHeader(StringBuilder prompt) {

        prompt.append("""
                You are Bodhak AI.

                You are an expert software architect.

                Analysis Type: ARCHITECTURE

                Important Rules:
                - Use only the supplied evidence.
                - Do not invent defects.
                - Do not assume information not present.
                - Explain findings using the provided metrics.
                - Return ONLY valid JSON.

                ==================================================
                """);
    }

    private void appendProjectSummary(
            StringBuilder prompt,
            ArchitectureAnalysisEvidence evidence
    ) {

        prompt.append("""

                PROJECT SUMMARY

                """);

        prompt.append("Total Entities: ")
                .append(evidence.summary().totalEntities())
                .append("\n");

        prompt.append("Healthy Entities: ")
                .append(evidence.summary().healthyEntities())
                .append("\n");

        prompt.append("Entities With Warnings: ")
                .append(evidence.summary().entitiesWithWarnings())
                .append("\n");

        prompt.append("God Classes: ")
                .append(evidence.summary().godClassCount())
                .append("\n");

        prompt.append("Highly Coupled Entities: ")
                .append(evidence.summary().highlyCoupledCount())
                .append("\n");

        prompt.append("Circular Entities: ")
                .append(evidence.summary().circularEntityCount())
                .append("\n");
//
//        prompt.append("Anemic Entities: ")
//                .append(evidence.anemicEntities().size())
//                .append("\n");

        prompt.append("Circular Dependency Groups: ")
                .append(evidence.summary().circularGroupCount())
                .append("\n");
    }

    private void appendCircularGroups(
            StringBuilder prompt,
            ArchitectureAnalysisEvidence evidence
    ) {

        prompt.append("""

                DETECTED CIRCULAR DEPENDENCY GROUPS

                """);

        int groupIndex = 1;
        List<CircularDependencyEvidence> circularDependencyEvidences=evidence.circularDependencies();

        for (CircularDependencyEvidence group : circularDependencyEvidences) {

            prompt.append("\nGroup ")
                    .append(groupIndex++)
                    .append(" (")
                    .append(group.cycleSize())
                    .append(" entities)\n");

            int count = 0;

            for (String entity : group.entities()) {

                if (count++ >= 10) {
                    prompt.append("... and ")
                            .append(group.cycleSize() - 10)
                            .append(" more entities\n");
                    break;
                }

                prompt.append(" - ")
                        .append(entity)
                        .append("\n");
            }
        }

    }


    private void appendGodClasses(
            StringBuilder prompt,
            ArchitectureAnalysisEvidence evidence
    ) {

        prompt.append("""

            GOD CLASS EVIDENCE

            """);

        evidence.godClasses()
                .stream()
                .limit(MAX_ENTITIES_PER_SECTION)
                .forEach(godClass -> {

                    prompt.append("\n----------------------------------\n");

                    prompt.append("Entity: ")
                            .append(godClass.entityName())
                            .append("\n");

                    prompt.append("Lines Of Code: ")
                            .append(godClass.linesOfCode())
                            .append("\n");

                    prompt.append("Method Count: ")
                            .append(godClass.methodCount())
                            .append("\n");

                    prompt.append("Field Count: ")
                            .append(godClass.fieldCount())
                            .append("\n");

                    prompt.append("Dependency Count: ")
                            .append(godClass.dependencyCount())
                            .append("\n");
                });
    }

    private void appendHighlyCoupledEntities(
            StringBuilder prompt,
            ArchitectureAnalysisEvidence evidence
    ) {

        prompt.append("""

            HIGH COUPLING EVIDENCE

            """);

        evidence.highCouplings()
                .stream()
                .limit(MAX_ENTITIES_PER_SECTION)
                .forEach(coupling -> {

                    prompt.append("\n----------------------------------\n");

                    prompt.append("Entity: ")
                            .append(coupling.entityName())
                            .append("\n");

                    prompt.append("Fan In: ")
                            .append(coupling.fanIn())
                            .append("\n");

                    prompt.append("Fan Out: ")
                            .append(coupling.fanOut())
                            .append("\n");

                    prompt.append("Coupling Score: ")
                            .append(coupling.couplingScore())
                            .append("\n");
                });
    }





    private void appendInstructions(StringBuilder prompt) {

        prompt.append("""

                ==================================================

                TASKS

                1. Identify the architectural defects.
                2. Explain why each defect exists.
                3. Identify responsible entities.
                4. Estimate severity.
                5. Suggest concrete fixes.
                6. Use only supplied evidence.

                Return JSON ONLY.

                Schema:

                {
                  "defects": [
                    {
                      "defectType": "",
                      "responsibleEntities": [],
                      "severity": "",
                      "explanation": "",
                      "possibleFixes": []
                    }
                  ]
                }
                """);
    }
    private void appendGrowthMetrics(
            StringBuilder prompt,
            ArchitectureAnalysisEvidence evidence
    ) {

        prompt.append("""

            GROWTH METRICS EVIDENCE

            """);

        evidence.growthRisks()
                .forEach(metric -> {

                    prompt.append("\n----------------------------------\n");

                    prompt.append("Entity: ")
                            .append(metric.entityName())
                            .append("\n");

                    prompt.append("Dependency Depth: ")
                            .append(
                                    String.format(
                                            "%.2f",
                                            metric.dependencyDepth()
                                    )
                            )
                            .append("\n");

                    prompt.append("Propagation Depth: ")
                            .append(
                                    String.format(
                                            "%.2f",
                                            metric.propagationDepth()
                                    )
                            )
                            .append("\n");

                    prompt.append("Normalized Depth: ")
                            .append(
                                    String.format(
                                            "%.2f",
                                            metric.normalizedDepth()
                                    )
                            )
                            .append("\n");

                    prompt.append("Normalized Propagation: ")
                            .append(
                                    String.format(
                                            "%.2f",
                                            metric.normalizedPropagation()
                                    )
                            )
                            .append("\n");
                });
    }
}
