package com.example.bodhak.orchestration.progress;

import java.nio.file.Path;
import java.util.List;

public final class AnalysisProgressEvents {

    public record ProjectDiscoveryStarted(Path projectPath) implements AnalysisProgressEvent {}

    public record FilesDiscovered(List<Path> files) implements AnalysisProgressEvent {}

    public record ParsingStarted(int totalFiles) implements AnalysisProgressEvent {}

    public record CompilationUnitParsed(Path file, String name, int parsedCount, int totalFiles) implements AnalysisProgressEvent {}

    public record EntityExtracted(String entityName, int count) implements AnalysisProgressEvent {}

    public record ReferenceDatabaseBuilt(int totalReferences) implements AnalysisProgressEvent {}

    public record DependencyGraphBuilt(int totalDependencies) implements AnalysisProgressEvent {}

    public record GraphIndexBuilt() implements AnalysisProgressEvent {}

    public record TechnologyDetectionStarted() implements AnalysisProgressEvent {}

    public record MetricsComputed(int totalEntities, long totalLoc) implements AnalysisProgressEvent {}

    public record AnalysisCompleted(
            int totalFiles,
            int totalEntities,
            int totalReferences,
            int totalNamespaces,
            int totalFrameworks,
            int totalMetrics
    ) implements AnalysisProgressEvent {}

    public record AnalysisFailed(String errorMessage) implements AnalysisProgressEvent {}
}
