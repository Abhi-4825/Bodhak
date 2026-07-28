package com.example.anuviya;

import static org.junit.jupiter.api.Assertions.*;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.compiler.AnalysisPipeline;
import org.junit.jupiter.api.Test;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Validates the new isolated AnalysisPipeline components.
 */
public class AnalysisPipelineTest {

    @Test
    public void testJavaPipeline() {
        Path sourceRoot = Paths.get("src/main/java").toAbsolutePath().normalize();
        Path mainFile = sourceRoot.resolve("com/example/anuviya/ui/Main.java");

        com.example.anuviya.frontend.FrontendRegistry registry =
                new com.example.anuviya.frontend.FrontendRegistry();
        registry.register(new com.example.anuviya.frontend.java.JavaLanguageFrontend(List.of(sourceRoot)));

        AnalysisPipeline pipeline = new AnalysisPipeline(registry);
        com.example.anuviya.compiler.PipelineContext context = pipeline.analyze(List.of(mainFile));
        List<CompilationUnit> units = context.getCompilationUnits();

        assertNotNull(units);
        assertFalse(units.isEmpty(), "Should have at least one CompilationUnit");

        CompilationUnit cu = units.get(0);
        assertEquals(mainFile.toAbsolutePath().normalize(), cu.getFilePath().toAbsolutePath().normalize());
        assertNotNull(cu.getIntermediateRepresentation(), "IRNode should not be null");
        assertFalse(cu.getEntities().isEmpty(), "Should extract entities for Main class");

        // Verify metrics
        cu.getEntities().forEach(entity -> {
            assertNotNull(entity.getMetrics());
            assertTrue(entity.getMetrics().linesOfCode() > 0, "LOC should be greater than 0");
        });
    }
}
