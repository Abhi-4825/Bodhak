package com.example.anuviya.analyzer.ai.prompt;

import com.example.anuviya.analyzer.ai.evidence.model.scalability.ScalabilityAnalysisEvidence;
import com.example.anuviya.analyzer.ai.evidence.model.BetweennessCentralityEvidence;
import com.example.anuviya.analyzer.ai.evidence.model.CircularDependencyEvidence;
import com.example.anuviya.analyzer.ai.evidence.model.DependencyGraphEvidence;
import com.example.anuviya.analyzer.ai.evidence.model.GrowthRiskEvidence;

// TODO: Future evidence model extensions (roadmap):
// - Layering/boundary violation detection (UI -> persistence skipping service layer)
// - Cohesion metrics (LCOM-style) to distinguish "big but cohesive" from true God Classes
public class ScalabilityPromptBuilder implements PromptBuilder<ScalabilityAnalysisEvidence> {

    private static final int MAX_ITEMS = 10;
    private static final String RESPONSE_PREFIX = "";

    @Override
    public PromptPair build(ScalabilityAnalysisEvidence evidence) {
        StringBuilder systemPrompt = new StringBuilder();
        appendSystemPrompt(systemPrompt);

        // User prompt structure: EVIDENCE (reference only) → TASK REMINDER → OUTPUT SCHEMA → "Begin now"
        // This ordering exploits recency bias: the model sees the output instructions last,
        // making it far less likely to echo the evidence block.
        StringBuilder userPrompt = new StringBuilder();
        appendSummary(userPrompt, evidence);
        appendFanOutEvidence(userPrompt, evidence);
        appendFanInEvidence(userPrompt, evidence);
        appendGrowthMetrics(userPrompt, evidence);
        appendBetweennessEvidence(userPrompt, evidence);
        appendCircularDependencies(userPrompt, evidence);
        appendAntiEchoRule(userPrompt);
        appendOutputSchema(userPrompt);

        return new PromptPair(systemPrompt.toString(), userPrompt.toString(), RESPONSE_PREFIX);
    }

    // ── SYSTEM PROMPT ─────────────────────────────────────────────────────────
    // System prompt contains ONLY the persona and high-level rules.
    // Evidence, schema, and generation trigger are in the user prompt.
    private void appendSystemPrompt(StringBuilder system) {
        system.append("""
You are Anuviya AI.
You are an expert software architect specializing in scalability analysis.

Analysis Type: SCALABILITY

Role & Responsibilities:
1. Identify major scalability bottlenecks and architectural hot spots in the codebase using only the provided structural and dependency evidence.
2. Ground all defects/risks in the supplied evidence. Do not invent bottlenecks or assume runtime behavior not specified in the metrics.
3. Every explanation MUST cite the specific metric values (e.g., fan-in, fan-out, dependency depth, propagation depth, centrality score) from the evidence that justify the bottleneck.
4. When multiple evidence signals (e.g., a class with high fan-out and also high betweenness centrality) point to one underlying scalability issue, merge them into a single bottleneck/defect rather than reporting them as separate line items.
5. Identify the specific responsible entities (qualified class or package names) for each bottleneck.
6. Suggest concrete, actionable fixes.
7. Constrain severity to exactly one of: "Critical", "High", "Medium", "Low".
8. Limit the response to a maximum of 8 defects, ordered from highest to lowest severity.
9. Provide an "overallAssessment" summarizing the scalability health of the codebase in 1-2 sentences.
10. If the codebase has no scalability bottlenecks or is fully optimized for growth based on the evidence, the "defects" list MUST be an empty array, and the "overallAssessment" must explicitly explain why it is optimized.

Return valid JSON ONLY. Do not wrap the JSON in markdown code blocks or code fences (e.g. do not use ```json or ```). Do not include any introductory or concluding text outside of the JSON structure itself.
""");
    }

    // ── EVIDENCE SECTIONS ─────────────────────────────────────────────────────
    // All evidence is formatted as plain labeled text — deliberately non-JSON-shaped
    // to prevent the model from copy-echoing it as its response.

    private void appendSummary(StringBuilder prompt, ScalabilityAnalysisEvidence evidence) {
        DependencyGraphEvidence graph = evidence.graph();
        double maxDepth = evidence.growthMetrics().stream()
                .mapToDouble(GrowthRiskEvidence::dependencyDepth)
                .max()
                .orElse(0);
        double maxPropagation = evidence.growthMetrics().stream()
                .mapToDouble(GrowthRiskEvidence::propagationDepth)
                .max()
                .orElse(0);

        prompt.append("""
=== SCALABILITY SUMMARY (for reference only — do not output this section) ===
""");
        prompt.append("Total Entities: ").append(graph.totalEntities()).append("\n");
        prompt.append("Dependency Edges: ").append(graph.totalEdges()).append("\n");
        prompt.append("Circular Groups: ").append(graph.circularGroups()).append("\n");
        prompt.append("Largest Cycle Size: ").append(graph.largestCycleSize()).append("\n");
        prompt.append("Maximum Fan In: ").append(graph.maxFanIn()).append("\n");
        prompt.append("Maximum Fan Out: ").append(graph.maxFanOut()).append("\n");
        prompt.append("Average Fan Out: ").append(String.format("%.2f", graph.averageFanOut())).append("\n");
        prompt.append("Maximum Dependency Depth: ").append(String.format("%.2f", maxDepth)).append("\n");
        prompt.append("Maximum Propagation Depth: ").append(String.format("%.2f", maxPropagation)).append("\n\n");
    }

    private void appendFanOutEvidence(StringBuilder prompt, ScalabilityAnalysisEvidence evidence) {
        prompt.append("=== HIGH FAN OUT EVIDENCE (for reference only — do not output this list) ===\n");
        evidence.highFanOuts().stream()
                .limit(MAX_ITEMS)
                .forEach(item ->
                    prompt.append("  - ").append(item.entityName())
                          .append(": Fan-Out=").append(item.fanOut())
                          .append("\n")
                );
        prompt.append("\n");
    }

    private void appendFanInEvidence(StringBuilder prompt, ScalabilityAnalysisEvidence evidence) {
        prompt.append("=== HIGH FAN IN EVIDENCE (for reference only — do not output this list) ===\n");
        evidence.highFanIns().stream()
                .limit(MAX_ITEMS)
                .forEach(item ->
                    prompt.append("  - ").append(item.entityName())
                          .append(": Fan-In=").append(item.fanIn())
                          .append("\n")
                );
        prompt.append("\n");
    }

    private void appendGrowthMetrics(StringBuilder prompt, ScalabilityAnalysisEvidence evidence) {
        prompt.append("=== GROWTH METRICS EVIDENCE (for reference only — do not output this list) ===\n");
        evidence.growthMetrics().stream()
                .limit(MAX_ITEMS)
                .forEach(metric ->
                    prompt.append("  - ").append(metric.entityName())
                          .append(": Depth=").append(String.format("%.2f", metric.dependencyDepth()))
                          .append(", Propagation=").append(String.format("%.2f", metric.propagationDepth()))
                          .append("\n")
                );
        prompt.append("\n");
    }

    private void appendBetweennessEvidence(StringBuilder prompt, ScalabilityAnalysisEvidence evidence) {
        prompt.append("=== BETWEENNESS CENTRALITY EVIDENCE (for reference only — do not output this list) ===\n");
        prompt.append("  Higher centrality = entity appears on many dependency paths between other entities.\n");
        int rank = 1;
        for (BetweennessCentralityEvidence node : evidence.centralities().stream().limit(MAX_ITEMS).toList()) {
            prompt.append("  - Rank ").append(rank++).append(": ").append(node.entity())
                  .append(", Centrality=").append(String.format("%.2f", node.score()))
                  .append("\n");
        }
        prompt.append("\n");
    }

    private void appendCircularDependencies(StringBuilder prompt, ScalabilityAnalysisEvidence evidence) {
        prompt.append("=== CIRCULAR DEPENDENCY EVIDENCE (for reference only) ===\n");
        int group = 1;
        for (CircularDependencyEvidence cycle : evidence.circularDependencies()) {
            prompt.append("  - Group ").append(group++).append(": ").append(cycle.cycleSize()).append(" entities in cycle\n");
        }
        prompt.append("\n");
    }

    // ── ANTI-ECHO RULE ────────────────────────────────────────────────────────
    // Placed AFTER evidence, BEFORE output schema — exploits recency bias.
    private void appendAntiEchoRule(StringBuilder prompt) {
        prompt.append("""
=== CRITICAL INSTRUCTION ===
The evidence sections above are INPUT DATA for your analysis, not a template.
Do NOT reprint, copy, reformat, or restate the evidence lists in your output.
Your output is a NEW JSON object containing only "overallAssessment" and "defects".
It must NOT contain "entityName", "fanIn", "fanOut", "dependencyDepth",
"propagationDepth", or "centralityScore" as top-level output keys.
If your output looks like the evidence list, you have failed the task.

""");
    }

    // ── OUTPUT SCHEMA ─────────────────────────────────────────────────────────
    // Restated AFTER evidence (recency bias: model sees this right before generating).
    private void appendOutputSchema(StringBuilder prompt) {
        prompt.append("""
=== YOUR TASK (restated) ===
Analyze the evidence above and produce exactly one JSON object with this schema:
{
  "overallAssessment": "1-2 sentence summary of overall scalability, grounded in metrics.",
  "defects": [
    {
      "defectType": "Real scalability problem name (e.g., Coupling Bottleneck, Central Dependency Hub, High Propagation Risk)",
      "responsibleEntities": ["qualified.name.of.EntityOrPackage"],
      "severity": "Critical or High or Medium or Low",
      "explanation": "Detailed explanation citing specific metric values from the evidence.",
      "possibleFixes": [
        "Concrete, actionable fix step 1",
        "Concrete, actionable fix step 2"
      ]
    }
  ]
}
Max 8 defects, ordered highest to lowest severity. Empty array if codebase is healthy.

""");
    }
}
