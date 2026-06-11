package com.example.bodhakfrontend.core.Metrics.architecture;


public final class ArchitectureMetrics {

    private ArchitectureMetrics() {}

    public static final ArchitectureHealthMetric
            HEALTH_SCORE =
            new ArchitectureHealthMetric();

    public static final CycleCountMetric
            CYCLE_COUNT =
            new CycleCountMetric();

    public static final DependencyCountMetric
            DEPENDENCY_COUNT =
            new DependencyCountMetric();

    public static final NamespaceCountMetric
            NAMESPACE_COUNT =
            new NamespaceCountMetric();

    public static final ClassCountMetric
            CLASS_COUNT =
            new ClassCountMetric();
}