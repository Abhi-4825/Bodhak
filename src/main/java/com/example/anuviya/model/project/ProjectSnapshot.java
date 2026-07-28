package com.example.anuviya.model.project;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.namespace.NamespaceInfo;

import java.util.List;
import java.util.Map;

/**
 * Represents the structural state of a project:
 * the project metadata, all parsed entities, and namespace groupings.
 *
 * Does NOT contain analysis results (ProjectType, baselines, hotspots, etc.).
 * Those live in AnalysisContext.
 */
public record ProjectSnapshot(
        ProjectInfo projectInfo,
        List<EntityInfo> entities,
        Map<String, NamespaceInfo> namespaces
) {}
