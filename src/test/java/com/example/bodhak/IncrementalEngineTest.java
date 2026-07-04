package com.example.bodhak;

import com.example.bodhak.orchestration.incremental.engine.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class IncrementalEngineTest {

    private FileContentHasher hasher;
    private ChangeClassifier classifier;
    private ArtifactDependencyGraph graph;
    private DependencyImpactAnalyzer analyzer;
    private InvalidationPlanner planner;

    @BeforeEach
    public void setUp() {
        hasher = new FileContentHasher();
        classifier = new ChangeClassifier(hasher);
        graph = new ArtifactDependencyGraph();
        analyzer = new DependencyImpactAnalyzer(graph);
        planner = new InvalidationPlanner(analyzer);
    }

    @Test
    public void testFileContentHasher() throws IOException {
        Path tempFile = Files.createTempFile("test_hasher", ".java");
        try {
            Files.writeString(tempFile, "public class Test {}");
            String hash1 = hasher.computeHash(tempFile);
            assertFalse(hash1.isEmpty());

            String cachedHash = hasher.getCachedHash(tempFile);
            assertEquals(hash1, cachedHash);

            // Change content
            Files.writeString(tempFile, "public class Test { int x; }");
            String hash2 = hasher.computeHash(tempFile);
            assertNotEquals(hash1, hash2);
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testChangeClassifier() throws IOException {
        Path tempFile = Files.createTempFile("test_classifier", ".java");
        try {
            Files.writeString(tempFile, "public class Test {}");
            
            // First time it should be NEW_FILE
            ChangeEvent event1 = classifier.classify(tempFile, ChangeClassifier.WatchEventKind.CREATE);
            assertEquals(ChangeType.NEW_FILE, event1.changeType());

            // Second time with same content should be GENERATED_FILE_CHANGE (meaning no logical change)
            ChangeEvent event2 = classifier.classify(tempFile, ChangeClassifier.WatchEventKind.MODIFY);
            assertEquals(ChangeType.GENERATED_FILE_CHANGE, event2.changeType());

            // Modify content
            Files.writeString(tempFile, "public class Test { int x; }");
            ChangeEvent event3 = classifier.classify(tempFile, ChangeClassifier.WatchEventKind.MODIFY);
            assertEquals(ChangeType.CONTENT_CHANGE, event3.changeType());

            // Non-source files
            Path resourceFile = Files.createTempFile("test", ".properties");
            try {
                ChangeEvent event4 = classifier.classify(resourceFile, ChangeClassifier.WatchEventKind.CREATE);
                assertEquals(ChangeType.RESOURCE_CHANGE, event4.changeType());
            } finally {
                Files.deleteIfExists(resourceFile);
            }
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }

    @Test
    public void testDependencyImpactAnalysis() {
        Set<ArtifactId> directlyStale = Set.of(ArtifactId.PARSED_IR);
        Set<ArtifactId> transitiveClosure = analyzer.analyze(directlyStale);

        assertTrue(transitiveClosure.contains(ArtifactId.PARSED_IR));
        assertTrue(transitiveClosure.contains(ArtifactId.COMPILATION_UNIT));
        assertTrue(transitiveClosure.contains(ArtifactId.SYMBOL_TABLE));
        assertTrue(transitiveClosure.contains(ArtifactId.ENTITY_INDEX));
        assertTrue(transitiveClosure.contains(ArtifactId.RELATIONSHIP_GRAPH));
        assertTrue(transitiveClosure.contains(ArtifactId.REFERENCE_DATABASE));
        assertTrue(transitiveClosure.contains(ArtifactId.NAMESPACE_INDEX));
        assertTrue(transitiveClosure.contains(ArtifactId.PROJECT_SNAPSHOT));
        assertTrue(transitiveClosure.contains(ArtifactId.CLASSIFICATION_RESULT));
        assertTrue(transitiveClosure.contains(ArtifactId.ANALYSIS_CONTEXT));
    }

    @Test
    public void testInvalidationPlanner() throws IOException {
        Path tempFile = Files.createTempFile("test_planner", ".java");
        try {
            Files.writeString(tempFile, "public class Test {}");
            ChangeEvent event = classifier.classify(tempFile, ChangeClassifier.WatchEventKind.CREATE);
            
            InvalidationPlan plan = planner.plan(Set.of(event));
            
            assertTrue(plan.filesToProcess().contains(tempFile));
            assertTrue(plan.staleArtifacts().contains(ArtifactId.COMPILATION_UNIT));
            assertTrue(plan.staleArtifacts().contains(ArtifactId.ANALYSIS_CONTEXT));
        } finally {
            Files.deleteIfExists(tempFile);
        }
    }
}
