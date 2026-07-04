package com.example.bodhak.analyzer.ai.prompt;

import com.example.bodhak.analyzer.ai.analysis.AnalysisType;
import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.context.GraphSnapshot;

public record AiPromptContext(

        AnalysisType analysisType,

        ProjectInfo projectInfo,

        GraphSnapshot graphSnapshot

) {}
