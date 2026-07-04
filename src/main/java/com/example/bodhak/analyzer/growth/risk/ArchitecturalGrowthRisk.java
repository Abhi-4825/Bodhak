package com.example.bodhak.analyzer.growth.risk;

import com.example.bodhak.analyzer.growth.model.GrowthQuadrant;

public record ArchitecturalGrowthRisk(GrowthQuadrant quadrant,

                                      double score,

                                      double normalizedDepth,

                                      double normalizedImpact) {
}
