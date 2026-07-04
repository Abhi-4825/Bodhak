package com.example.bodhak.analyzer.ai.evidence.model;

import com.example.bodhak.model.entity.EntityInfo;

import java.util.List;
import java.util.Set;

public record ArchitectureAnalysisEvidence(

        int totalEntities,

        int healthyEntities,

        int entitiesWithWarnings,

        List<EntityInfo> godClasses,

        List<EntityInfo> highlyCoupledEntities,

        List<EntityInfo> circularEntities,

        List<EntityInfo> anemicEntities,

        Set<Set<String>> circularGroups

) {}
