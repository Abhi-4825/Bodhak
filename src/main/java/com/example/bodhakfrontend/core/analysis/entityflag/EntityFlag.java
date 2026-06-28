package com.example.bodhakfrontend.core.analysis.entityflag;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EntityFlag {

    OVERSIZED_ENTITY,

    HIGH_COUPLING,

    CIRCULAR_DEPENDENCY,

    ANEMIC_DOMAIN,

    HIGH_FAN_IN,

    HIGH_FAN_OUT;

    @JsonCreator
    public static EntityFlag fromString(String value) {
        if (value == null) return null;
        String normalized = value.trim().toUpperCase();
        if ("GOD_CLASS".equals(normalized)) {
            return OVERSIZED_ENTITY;
        }
        try {
            return EntityFlag.valueOf(normalized);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
