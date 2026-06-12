package com.example.bodhakfrontend.ai.context;

import com.example.bodhakfrontend.ai.model.AnalysisType;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.engine.GraphSnapshot;

public record AiPromptContext(

        AnalysisType analysisType,

        ProjectInfo projectInfo,

        GraphSnapshot graphSnapshot

) {}
