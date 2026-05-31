package com.example.bodhakfrontend.core.model.entity;

/**
 * Language-neutral code issue types detected by analysers.
 */
public enum IssueType {
    /** Entity is excessively large (LOC, member count, or field count). */
    GOD_CLASS,
    /** Entity is coupled to too many other entities. */
    HIGH_COUPLING,
    /** Entity is part of a circular dependency cycle. */
    CIRCULAR_DEPENDENCY,
    /** Entity has many fields but almost no behaviour (no members). */
    ANEMIC_DOMAIN
}
