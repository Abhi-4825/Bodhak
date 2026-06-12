package com.example.bodhakfrontend.ai.prompt;
import com.example.bodhakfrontend.ai.evidence.builder.ArchitectureEvidenceBuilder;
import com.example.bodhakfrontend.ai.evidence.model.ArchitectureAnalysisEvidence;
import com.example.bodhakfrontend.core.Analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;

import java.util.List;
import java.util.Set;

public class ArchitecturePromptBuilder implements PromptBuilder <ArchitectureAnalysisEvidence>{

    private static final int MAX_ENTITIES_PER_SECTION = 10;

    private final ArchitectureEvidenceBuilder evidenceBuilder =
            new ArchitectureEvidenceBuilder();

    @Override
    public String build(ArchitectureAnalysisEvidence evidence) {



        StringBuilder prompt = new StringBuilder();

        appendHeader(prompt);
        appendProjectSummary(prompt, evidence);
        appendCircularGroups(prompt, evidence);
        appendGodClasses(prompt, evidence);
        appendHighlyCoupledEntities(prompt, evidence);
        appendAnemicEntities(prompt, evidence);
        appendInstructions(prompt);

        return prompt.toString();
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
                .append(evidence.totalEntities())
                .append("\n");

        prompt.append("Healthy Entities: ")
                .append(evidence.healthyEntities())
                .append("\n");

        prompt.append("Entities With Warnings: ")
                .append(evidence.entitiesWithWarnings())
                .append("\n");

        prompt.append("God Classes: ")
                .append(evidence.godClasses().size())
                .append("\n");

        prompt.append("Highly Coupled Entities: ")
                .append(evidence.highlyCoupledEntities().size())
                .append("\n");

        prompt.append("Circular Entities: ")
                .append(evidence.circularEntities().size())
                .append("\n");

        prompt.append("Anemic Entities: ")
                .append(evidence.anemicEntities().size())
                .append("\n");

        prompt.append("Circular Dependency Groups: ")
                .append(evidence.circularGroups().size())
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

        for (Set<String> group : evidence.circularGroups()) {

            prompt.append("\nGroup ")
                    .append(groupIndex++)
                    .append(" (")
                    .append(group.size())
                    .append(" entities)\n");

            int count = 0;

            for (String entity : group) {

                if (count++ >= 10) {
                    prompt.append("... and ")
                            .append(group.size() - 10)
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

                GOD CLASSES

                """);

        appendEntitySection(
                prompt,
                evidence.godClasses()
        );
    }

    private void appendHighlyCoupledEntities(
            StringBuilder prompt,
            ArchitectureAnalysisEvidence evidence
    ) {

        prompt.append("""

                HIGHLY COUPLED ENTITIES

                """);

        appendEntitySection(
                prompt,
                evidence.highlyCoupledEntities()
        );
    }

    private void appendAnemicEntities(
            StringBuilder prompt,
            ArchitectureAnalysisEvidence evidence
    ) {

        prompt.append("""

                ANEMIC DOMAIN ENTITIES

                """);

        appendEntitySection(
                prompt,
                evidence.anemicEntities()
        );
    }

    private void appendEntitySection(
            StringBuilder prompt,
            List<EntityInfo> entities
    ) {

        entities.stream()
                .limit(MAX_ENTITIES_PER_SECTION)
                .forEach(entity -> {

                    prompt.append("\n----------------------------------\n");

                    prompt.append("Entity: ")
                            .append(entity.getEntityName())
                            .append("\n");

                    prompt.append("Issues: ")
                            .append(entity.getIssueType())
                            .append("\n");

                    prompt.append("Depends On: ")
                            .append(entity.getDependsOn().size())
                            .append("\n");

                    prompt.append("Used By: ")
                            .append(entity.getUsedBy().size())
                            .append("\n");

                    prompt.append("Lines Of Code: ")
                            .append(entity.getLinesOfCode())
                            .append("\n");

                    prompt.append("Methods: ")
                            .append(entity.getMethodCount())
                            .append("\n");

                    prompt.append("Fields: ")
                            .append(entity.getFields().size())
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
}