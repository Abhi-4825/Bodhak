package com.example.bodhakfrontend.core.model.project;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.warning.UsageStatus;

/**
 * An entity that appears unused / suspicious after reachability analysis.
 */
public class UnusedEntityInfo {
    public enum Confidence {
        LOW, MEDIUM, HIGH
    }

    private final EntityInfo entity;
    private final UsageStatus status;
    private final Confidence confidence;
    private final String reason;

    public UnusedEntityInfo(EntityInfo entity, UsageStatus status, Confidence confidence, String reason) {
        this.entity = entity;
        this.status = status;
        this.confidence = confidence;
        this.reason = reason;
    }

    public EntityInfo getEntity()       { return entity; }
    public UsageStatus getStatus()      { return status; }
    public Confidence getConfidence()   { return confidence; }
    public String getReason()           { return reason; }
}
