package com.example.anuviya;

import com.example.anuviya.compiler.symbol.*;
import com.example.anuviya.compiler.root.*;
import com.example.anuviya.model.project.*;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.model.reference.*;
import com.example.anuviya.model.reference.payload.AnnotationPayload;
import com.example.anuviya.ir.SourceRange;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ProjectRootsBuilderTest {

    @Test
    public void testMavenScannerMatchingAndExtraction(@TempDir Path tempDir) throws IOException {
        // Create mock pom.xml
        Path pom = tempDir.resolve("pom.xml");
        Files.writeString(pom, "<project><dependencies><dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency></dependencies></project>");

        SymbolTable table = new SymbolTable();
        EvidenceAccumulator accumulator = new EvidenceAccumulator();
        
        BuildFileScanner scanner = new BuildFileScanner();
        scanner.scan(tempDir.toFile(), new ReferenceDatabase(), table, accumulator);

        assertFalse(accumulator.getCandidates().isEmpty());
        
        // Resolve using the resolver
        RootResolver resolver = new RootResolver();
        ProjectRootInfo info = resolver.resolve(accumulator);
        
        assertTrue(info.detectedArchetypes().contains("Spring Boot App"));
    }

    @Test
    public void testFrameworkScannerExtraction() {
        SymbolTable table = new SymbolTable();
        ReferenceDatabase database = new ReferenceDatabase();
        EvidenceAccumulator accumulator = new EvidenceAccumulator();

        EntitySymbol source = table.getOrCreateEntity("com.example.App", SymbolKind.CLASS);

        // Add a mock Spring Boot annotation reference
        SemanticReference ref = new SemanticReference(
            source,
            source,
            new SymbolId(0),
            ReferenceKind.ANNOTATION,
            SemanticRole.AnnotationReferenceRole.DECORATED_BY,
            new ReferenceCharacteristics(false, false, false, false),
            new File("App.java"),
            new SourceRange(1, 1, 1, 10),
            new AnnotationPayload("SpringBootApplication", java.util.Map.of())
        );
        database.addReference(ref);

        FrameworkScanner scanner = new FrameworkScanner();
        scanner.scan(new File("."), database, table, accumulator);

        RootResolver resolver = new RootResolver();
        ProjectRootInfo info = resolver.resolve(accumulator);

        var surfaces = info.surfaces();
        assertEquals(1, surfaces.size());
        ProjectSurface surface = surfaces.get(0);
        assertEquals(source, surface.symbol());
        assertTrue(surface.capabilities().contains(RootCapability.FRAMEWORK_BOOTSTRAP));
        assertTrue(surface.confidence() > 0.5);
    }

    @Test
    public void testIRScannerMainMethodAndExports() {
        SymbolTable table = new SymbolTable();
        ReferenceDatabase database = new ReferenceDatabase();
        EvidenceAccumulator accumulator = new EvidenceAccumulator();

        EntitySymbol cls = table.getOrCreateEntity("com.example.MainClass", SymbolKind.CLASS);
        MemberSymbol main = table.getOrCreateMember(cls, "main", SymbolKind.METHOD);

        IRScanner scanner = new IRScanner();
        scanner.scan(new File("."), database, table, accumulator);

        RootResolver resolver = new RootResolver();
        ProjectRootInfo info = resolver.resolve(accumulator);

        // Should find main class as EXECUTABLE surface
        var executableSurfaces = info.getByCapability(RootCapability.EXECUTABLE);
        assertEquals(1, executableSurfaces.size());
        assertEquals(cls, executableSurfaces.get(0).symbol());
    }
}
