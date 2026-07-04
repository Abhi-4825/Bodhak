package com.example.bodhak.analyzer.ai.prompt;

import com.example.bodhak.analyzer.ai.analysis.AnalysisType;
import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.context.GraphSnapshot;

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
