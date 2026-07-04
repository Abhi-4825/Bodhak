package com.example.bodhak.model.project;

/**
 * Supporting evidence justifying why a project surface was resolved.
 */
public record Evidence(
    String sourceScanner,  // e.g. "BuildFileScanner", "FrameworkScanner"
    String description,    // e.g. "Found @SpringBootApplication annotation"
    double score           // confidence contribution score (e.g. 0.5)
) {}
