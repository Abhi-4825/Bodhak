package com.example.anuviya.classification.intelligence;

import com.example.anuviya.model.project.ProjectInfo;
import java.util.*;

public class ProjectArchetypeResolver {
    public Set<String> resolve(ProjectInfo projectInfo) {
        if (projectInfo == null || projectInfo.projectRootInfo() == null) {
            return Set.of();
        }
        return projectInfo.projectRootInfo().detectedArchetypes();
    }
}
