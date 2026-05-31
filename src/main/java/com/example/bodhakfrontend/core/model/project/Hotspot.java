package com.example.bodhakfrontend.core.model.project;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;

import java.util.List;
import java.util.Set;

/**
 * A risk hotspot: an entity with a high combined score based on
 * LOC, coupling, member count, and circular dependencies.
 */
public class Hotspot {
    private final EntityInfo entity;
    private final Set<String> reasons;
    private final double score;

    public Hotspot(EntityInfo entity, Set<String> reasons, double score) {
        this.entity  = entity;
        this.reasons = reasons;
        this.score   = score;
    }

    public EntityInfo getEntity()   { return entity;  }
    public Set<String> getReasons() { return reasons; }
    public double getScore()           { return score;   }
}
