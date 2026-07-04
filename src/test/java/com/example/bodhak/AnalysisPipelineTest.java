package com.example.bodhak;
import com.example.bodhak.ir.IRNode;
import com.example.bodhak.compiler.PipelineContext;
import com.example.bodhak.frontend.FrontendRegistry;

import static org.junit.jupiter.api.Assertions.*;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.compiler.AnalysisPipeline;
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
        Path mainFile = sourceRoot.resolve("com/example/bodhak/ui/Main.java");

        FrontendRegistry registry =
                new FrontendRegistry();
        registry.register(new com.example.bodhak.frontend.java.JavaLanguageFrontend(List.of(sourceRoot)));

        AnalysisPipeline pipeline = new AnalysisPipeline(registry);
        PipelineContext context = pipeline.analyze(List.of(mainFile));
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
