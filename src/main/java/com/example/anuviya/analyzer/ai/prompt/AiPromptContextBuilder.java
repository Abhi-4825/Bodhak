package com.example.anuviya.analyzer.ai.prompt;

import com.example.anuviya.analyzer.ai.analysis.AnalysisType;
import com.example.anuviya.model.project.ProjectInfo;
import com.example.anuviya.context.GraphSnapshot;

public class AiPromptContextBuilder {
    public AiPromptContext build(
            AnalysisType analysisType,
            ProjectInfo projectInfo,
            GraphSnapshot graphSnapshot
    ) {

        return new AiPromptContext(
                analysisType,
                projectInfo,
                graphSnapshot
        );
    }
}
