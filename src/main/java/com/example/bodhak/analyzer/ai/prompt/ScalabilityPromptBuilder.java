package com.example.bodhak.analyzer.ai.prompt;

import com.example.bodhak.analyzer.ai.evidence.model.scalability.ScalabilityAnalysisEvidence;



import com.example.bodhak.analyzer.ai.evidence.model.*;

public class ScalabilityPromptBuilder
        implements PromptBuilder<ScalabilityAnalysisEvidence> {

    private static final int MAX_ITEMS = 10;

    @Override
    public String build(
            ScalabilityAnalysisEvidence evidence
    ) {

        StringBuilder prompt = new StringBuilder();

        appendHeader(prompt);

        appendSummary(prompt, evidence);

        appendFanOutEvidence(prompt, evidence);

        appendFanInEvidence(prompt, evidence);

        appendGrowthMetrics(prompt, evidence);

        appendBetweennessEvidence(prompt, evidence);

        appendCircularDependencies(prompt, evidence);

        appendInstructions(prompt);

        return prompt.toString();
    }

    // ============================================================
    // HEADER
    // ============================================================

    private void appendHeader(StringBuilder prompt) {

        prompt.append("""
                You are Bodhak AI.

                You are an expert software architect specializing in scalability analysis.

                Analysis Type: SCALABILITY

                Important Rules:

                - Use only supplied evidence.
                - Do not invent bottlenecks.
                - Do not assume runtime behavior.
                - Infer risks from dependency structure and graph metrics.
                - Return valid JSON only.

                ==================================================
                """);
    }

    // ============================================================
    // SUMMARY
    // ============================================================

    private void appendSummary(

            StringBuilder prompt,

            ScalabilityAnalysisEvidence evidence

    ) {

        DependencyGraphEvidence graph =
                evidence.graph();

        double maxDepth =
                evidence.growthMetrics()
                        .stream()
                        .mapToDouble(
                                GrowthRiskEvidence::dependencyDepth
                        )
                        .max()
                        .orElse(0);

        double maxPropagation =
                evidence.growthMetrics()
                        .stream()
                        .mapToDouble(
                                GrowthRiskEvidence::propagationDepth
                        )
                        .max()
                        .orElse(0);

        prompt.append("""

                SCALABILITY SUMMARY

                """);

        prompt.append("Total Entities: ")
                .append(graph.totalEntities())
                .append("\n");

        prompt.append("Dependency Edges: ")
                .append(graph.totalEdges())
                .append("\n");

        prompt.append("Circular Groups: ")
                .append(graph.circularGroups())
                .append("\n");

        prompt.append("Largest Cycle Size: ")
                .append(graph.largestCycleSize())
                .append("\n");

        prompt.append("Maximum Fan In: ")
                .append(graph.maxFanIn())
                .append("\n");

        prompt.append("Maximum Fan Out: ")
                .append(graph.maxFanOut())
                .append("\n");

        prompt.append("Average Fan Out: ")
                .append(String.format("%.2f", graph.averageFanOut()))
                .append("\n");

        prompt.append("Maximum Dependency Depth: ")
                .append(String.format("%.2f", maxDepth))
                .append("\n");

        prompt.append("Maximum Propagation Depth: ")
                .append(String.format("%.2f", maxPropagation))
                .append("\n");
    }

    // ============================================================
    // FAN OUT
    // ============================================================

    private void appendFanOutEvidence(

            StringBuilder prompt,

            ScalabilityAnalysisEvidence evidence

    ) {

        prompt.append("""

                HIGH FAN OUT EVIDENCE

                """);

        evidence.highFanOuts()
                .stream()
                .limit(MAX_ITEMS)
                .forEach(item -> {

                    prompt.append("\n----------------------------------\n");

                    prompt.append("Entity: ")
                            .append(item.entityName())
                            .append("\n");

                    prompt.append("Fan Out: ")
                            .append(item.fanOut())
                            .append("\n");
                });
    }

    // ============================================================
    // FAN IN
    // ============================================================

    private void appendFanInEvidence(

            StringBuilder prompt,

            ScalabilityAnalysisEvidence evidence

    ) {

        prompt.append("""

                HIGH FAN IN EVIDENCE

                """);

        evidence.highFanIns()
                .stream()
                .limit(MAX_ITEMS)
                .forEach(item -> {

                    prompt.append("\n----------------------------------\n");

                    prompt.append("Entity: ")
                            .append(item.entityName())
                            .append("\n");

                    prompt.append("Fan In: ")
                            .append(item.fanIn())
                            .append("\n");
                });
    }

    // ============================================================
    // GROWTH METRICS
    // ============================================================

    private void appendGrowthMetrics(

            StringBuilder prompt,

            ScalabilityAnalysisEvidence evidence

    ) {

        prompt.append("""

                GROWTH METRICS EVIDENCE

                """);

        evidence.growthMetrics()
                .stream()
                .limit(MAX_ITEMS)
                .forEach(metric -> {

                    prompt.append("\n----------------------------------\n");

                    prompt.append("Entity: ")
                            .append(metric.entityName())
                            .append("\n");

                    prompt.append("Dependency Depth: ")
                            .append(metric.dependencyDepth())
                            .append("\n");

                    prompt.append("Propagation Depth: ")
                            .append(metric.propagationDepth())
                            .append("\n");
                });
    }

    // ============================================================
    // BETWEENNESS
    // ============================================================

    private void appendBetweennessEvidence(

            StringBuilder prompt,

            ScalabilityAnalysisEvidence evidence

    ) {

        prompt.append("""

                BETWEENNESS CENTRALITY EVIDENCE

                Higher centrality means the entity appears on many
                dependency paths between other entities.

                """);

        int rank = 1;

        for (BetweennessCentralityEvidence node
                : evidence.centralities()
                .stream()
                .limit(MAX_ITEMS)
                .toList()) {

            prompt.append("\n----------------------------------\n");

            prompt.append("Rank: ")
                    .append(rank++)
                    .append("\n");

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
        }
    }

    // ============================================================
    // CYCLES
    // ============================================================

    private void appendCircularDependencies(

            StringBuilder prompt,

            ScalabilityAnalysisEvidence evidence

    ) {

        prompt.append("""

                CIRCULAR DEPENDENCY EVIDENCE

                """);

        int group = 1;

        for (CircularDependencyEvidence cycle
                : evidence.circularDependencies()) {

            prompt.append("\nGroup ")
                    .append(group++)
                    .append("\n");

            prompt.append("Cycle Size: ")
                    .append(cycle.cycleSize())
                    .append("\n");
        }
    }

    // ============================================================
    // TASKS
    // ============================================================

    private void appendInstructions(
            StringBuilder prompt
    ) {

        prompt.append("""

                ==================================================

                TASKS

                1. Identify scalability risks.

                2. Identify entities that may become bottlenecks
                   as the system grows.

                3. Consider:
                   - dependency depth
                   - propagation depth
                   - fan-in
                   - fan-out
                   - betweenness centrality
                   - circular dependency clusters

                4. Explain why the risk may impact future growth.

                5. Suggest scalability improvements.

                6. Use only supplied evidence.

                Return JSON ONLY.

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
}
