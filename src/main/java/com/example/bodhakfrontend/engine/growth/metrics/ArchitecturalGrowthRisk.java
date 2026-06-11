package com.example.bodhakfrontend.engine.growth.metrics;

import com.example.bodhakfrontend.engine.growth.model.GrowthQuadrant;

public record ArchitecturalGrowthRisk(GrowthQuadrant quadrant,

                                      double score,

                                      double normalizedDepth,

                                      double normalizedImpact) {
}
