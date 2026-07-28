package com.example.anuviya.analyzer.growth.risk;

import com.example.anuviya.analyzer.growth.model.GrowthQuadrant;

public record ArchitecturalGrowthRisk(GrowthQuadrant quadrant,

                                      double score,

                                      double normalizedDepth,

                                      double normalizedImpact) {
}
