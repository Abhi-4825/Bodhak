package com.example.anuviya.analyzer.ai.prompt;

import com.example.anuviya.analyzer.ai.evidence.model.architectur.ArchitectureAnalysisEvidence;
import com.example.anuviya.analyzer.ai.evidence.model.CircularDependencyEvidence;
import com.example.anuviya.analyzer.ai.evidence.model.PackageCycleEvidence;
import com.example.anuviya.analyzer.ai.evidence.model.PackageStabilityEvidence;

// TODO: Future evidence model extensions (roadmap):
// - Layering/boundary violation detection (UI -> persistence skipping service layer)
// - Cohesion metrics (LCOM-style) to distinguish "big but cohesive" from true God Classes
public class ArchitecturePromptBuilder implements PromptBuilder<ArchitectureAnalysisEvidence> {
    private static final int MAX_ENTITIES_PER_SECTION = 10;
    private static final String RESPONSE_PREFIX = "";

    @Override
    public PromptPair build(ArchitectureAnalysisEvidence evidence) {
        StringBuilder systemPrompt = new StringBuilder();
        appendSystemPrompt(systemPrompt);

        // User prompt structure: EVIDENCE (reference only) → TASK REMINDER → OUTPUT SCHEMA → "Begin now"
        // This ordering exploits recency bias: the model sees the output instructions last,
        // making it far less likely to echo the evidence block.
        StringBuilder userPrompt = new StringBuilder();
        appendProjectSummary(userPrompt, evidence);
        appendCircularGroups(userPrompt, evidence);
        appendPackageCycles(userPrompt, evidence);
        appendPackageStabilities(userPrompt, evidence);
        appendGodClasses(userPrompt, evidence);
        appendHighlyCoupledEntities(userPrompt, evidence);
        appendGrowthMetrics(userPrompt, evidence);
        appendBetweennessEvidence(userPrompt, evidence);
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
You are an expert software architect specializing in software design analysis.

Analysis Type: ARCHITECTURE

Role & Responsibilities:
1. Identify major architectural defects in the codebase using only the provided structural and dependency evidence.
2. Ground all defects in the supplied evidence. Do not invent defects or assume runtime behavior not specified in the metrics.
3. Every explanation MUST cite the specific metric values (e.g., lines of code, fan-in, fan-out, centrality, instability, distance) from the evidence that justify the defect.
4. When multiple evidence signals (e.g., a class flagged as a God Class and also having High Coupling) point to one underlying architectural problem, merge them into a single defect rather than reporting them as separate line items.
5. Identify the specific responsible entities (qualified class or package names) for each defect.
6. Suggest concrete, actionable fixes.
7. Constrain severity to exactly one of: "Critical", "High", "Medium", "Low".
8. Limit the response to a maximum of 8 defects, ordered from highest to lowest severity.
9. Provide an "overallAssessment" summarizing the architectural health of the codebase in 1-2 sentences.
10. If the codebase is healthy or there are no significant design problems, the "defects" list MUST be an empty array, and the "overallAssessment" must explicitly explain why it is healthy.

Return valid JSON ONLY. Do not wrap the JSON in markdown code blocks or code fences (e.g. do not use ```json or ```). Do not include any introductory or concluding text outside of the JSON structure itself.
""");
    }

    // ── EVIDENCE SECTIONS ─────────────────────────────────────────────────────
    // All evidence is formatted as plain labeled text — deliberately non-JSON-shaped
    // to prevent the model from copy-echoing it as its response.

    private void appendProjectSummary(StringBuilder prompt, ArchitectureAnalysisEvidence evidence) {
        prompt.append("""
=== PROJECT SUMMARY (for reference only — do not output this section) ===
""");
        prompt.append("Total Entities: ").append(evidence.summary().totalEntities()).append("\n");
        prompt.append("Healthy Entities: ").append(evidence.summary().healthyEntities()).append("\n");
        prompt.append("Entities With Warnings: ").append(evidence.summary().entitiesWithWarnings()).append("\n");
        prompt.append("God Classes: ").append(evidence.summary().godClassCount()).append("\n");
        prompt.append("Highly Coupled Entities: ").append(evidence.summary().highlyCoupledCount()).append("\n");
        prompt.append("Circular Entities: ").append(evidence.summary().circularEntityCount()).append("\n");
        prompt.append("Circular Dependency Groups: ").append(evidence.summary().circularGroupCount()).append("\n\n");
    }

    private void appendCircularGroups(StringBuilder prompt, ArchitectureAnalysisEvidence evidence) {
        prompt.append("=== CLASS-LEVEL CIRCULAR DEPENDENCY GROUPS (for reference only) ===\n");
        int groupIndex = 1;
        for (CircularDependencyEvidence group : evidence.circularDependencies()) {
            prompt.append("  Group ").append(groupIndex++).append(" (").append(group.cycleSize()).append(" entities): ");
            int count = 0;
            for (String entity : group.entities()) {
                if (count++ >= 10) {
                    prompt.append("... and ").append(group.cycleSize() - 10).append(" more");
                    break;
                }
                if (count > 1) prompt.append(", ");
                prompt.append(entity);
            }
            prompt.append("\n");
        }
        prompt.append("\n");
    }

    private void appendPackageCycles(StringBuilder prompt, ArchitectureAnalysisEvidence evidence) {
        prompt.append("=== PACKAGE-LEVEL CIRCULAR DEPENDENCY GROUPS (for reference only) ===\n");
        if (evidence.packageCycles() == null || evidence.packageCycles().isEmpty()) {
            prompt.append("  None detected.\n\n");
            return;
        }
        int groupIndex = 1;
        for (PackageCycleEvidence group : evidence.packageCycles()) {
            prompt.append("  Group ").append(groupIndex++).append(" (").append(group.cycleSize()).append(" packages): ");
            int count = 0;
            for (String pkg : group.packages()) {
                if (count++ > 0) prompt.append(", ");
                prompt.append(pkg);
            }
            prompt.append("\n");
        }
        prompt.append("\n");
    }

    private void appendPackageStabilities(StringBuilder prompt, ArchitectureAnalysisEvidence evidence) {
        prompt.append("=== PACKAGE STABILITY & DISTANCE — MARTIN'S METRICS (for reference only) ===\n");
        prompt.append("  Key: I=Instability(0=stable,1=unstable) A=Abstractness(0=concrete,1=abstract) D=Distance(0=ideal,1=problematic)\n");
        if (evidence.packageStabilities() == null || evidence.packageStabilities().isEmpty()) {
            prompt.append("  No package stability data available.\n\n");
            return;
        }
        for (PackageStabilityEvidence stat : evidence.packageStabilities()) {
            prompt.append("  - ").append(stat.packageName())
                  .append(": Fan-In=").append(stat.fanIn())
                  .append(", Fan-Out=").append(stat.fanOut())
                  .append(", I=").append(String.format("%.2f", stat.instability()))
                  .append(", A=").append(String.format("%.2f", stat.abstractness()))
                  .append(", D=").append(String.format("%.2f", stat.distance()))
                  .append("\n");
        }
        prompt.append("\n");
    }

    private void appendGodClasses(StringBuilder prompt, ArchitectureAnalysisEvidence evidence) {
        prompt.append("=== GOD CLASS EVIDENCE (for reference only — do not output this list) ===\n");
        evidence.godClasses().stream()
                .limit(MAX_ENTITIES_PER_SECTION)
                .forEach(godClass ->
                    prompt.append("  - ").append(godClass.entityName())
                          .append(": ").append(godClass.linesOfCode()).append(" LOC")
                          .append(", ").append(godClass.methodCount()).append(" methods")
                          .append(", ").append(godClass.fieldCount()).append(" fields")
                          .append(", ").append(godClass.dependencyCount()).append(" dependencies")
                          .append("\n")
                );
        prompt.append("\n");
    }

    private void appendHighlyCoupledEntities(StringBuilder prompt, ArchitectureAnalysisEvidence evidence) {
        prompt.append("=== HIGH COUPLING EVIDENCE (for reference only — do not output this list) ===\n");
        evidence.highCouplings().stream()
                .limit(MAX_ENTITIES_PER_SECTION)
                .forEach(coupling ->
                    prompt.append("  - ").append(coupling.entityName())
                          .append(": Fan-In=").append(coupling.fanIn())
                          .append(", Fan-Out=").append(coupling.fanOut())
                          .append(", Coupling Score=").append(coupling.couplingScore())
                          .append("\n")
                );
        prompt.append("\n");
    }

    private void appendGrowthMetrics(StringBuilder prompt, ArchitectureAnalysisEvidence evidence) {
        prompt.append("=== GROWTH METRICS EVIDENCE (for reference only — do not output this list) ===\n");
        evidence.growthRisks().forEach(metric ->
            prompt.append("  - ").append(metric.entityName())
                  .append(": Depth=").append(String.format("%.2f", metric.dependencyDepth()))
                  .append(", Propagation=").append(String.format("%.2f", metric.propagationDepth()))
                  .append(", NormDepth=").append(String.format("%.2f", metric.normalizedDepth()))
                  .append(", NormPropagation=").append(String.format("%.2f", metric.normalizedPropagation()))
                  .append("\n")
        );
        prompt.append("\n");
    }

    private void appendBetweennessEvidence(StringBuilder prompt, ArchitectureAnalysisEvidence evidence) {
        prompt.append("=== BETWEENNESS CENTRALITY EVIDENCE (for reference only — do not output this list) ===\n");
        prompt.append("  Higher centrality = entity appears on many dependency paths between other entities.\n");
        evidence.betweennessCentralities().stream()
                .limit(MAX_ENTITIES_PER_SECTION)
                .forEach(node ->
                    prompt.append("  - ").append(node.entity())
                          .append(": Centrality=").append(String.format("%.2f", node.score()))
                          .append("\n")
                );
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
It must NOT contain "entityName", "linesOfCode", "methodCount", "fieldCount",
"dependencyCount", "fanIn", "fanOut", "couplingScore", or "centralityScore"
as top-level output keys. If your output looks like the evidence list, you have
failed the task.

""");
    }

    // ── OUTPUT SCHEMA ─────────────────────────────────────────────────────────
    // Restated AFTER evidence (recency bias: model sees this right before generating).
    private void appendOutputSchema(StringBuilder prompt) {
        prompt.append("""
=== YOUR TASK (restated) ===
Analyze the evidence above and produce exactly one JSON object with this schema:
{
  "overallAssessment": "1-2 sentence summary of overall design health, grounded in metrics.",
  "defects": [
    {
      "defectType": "Real architectural problem name (e.g., Separation of Concerns Violation, Tight Coupling Cascade, Change Amplification Risk)",
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
