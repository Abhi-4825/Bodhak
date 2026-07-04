package com.example.bodhak.compiler.root;

import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.compiler.symbol.SymbolTable;
import com.example.bodhak.compiler.symbol.Symbol;
import com.example.bodhak.model.reference.*;
import com.example.bodhak.model.reference.payload.AnnotationPayload;
import com.example.bodhak.model.project.Evidence;
import com.example.bodhak.model.project.RootCapability;

import java.io.File;
import java.util.List;

/**
 * Scans framework annotation metadata and decorators from the semantic ReferenceDatabase.
 */
public class FrameworkScanner implements RootEvidenceScanner {

    @Override
    public void scan(File projectRoot, ReferenceDatabase database, SymbolTable symbolTable, EvidenceAccumulator accumulator) {
        List<SemanticReference> annotations = database.getByKind(ReferenceKind.ANNOTATION);
        for (SemanticReference ref : annotations) {
            if (ref.payload() instanceof AnnotationPayload ap) {
                String name = ap.annotationName();
                Symbol source = ref.sourceSymbol();

                if ("SpringBootApplication".equalsIgnoreCase(name)) {
                    accumulator.contribute(
                        source,
                        RootCapability.FRAMEWORK_BOOTSTRAP,
                        new Evidence("FrameworkScanner", "Found @SpringBootApplication annotation", 0.6),
                        "Spring Boot App"
                    );
                    accumulator.contribute(
                        source,
                        RootCapability.EXECUTABLE,
                        new Evidence("FrameworkScanner", "Spring Boot configuration acts as runtime entry", 0.3),
                        null
                    );
                }
                else if ("RestController".equalsIgnoreCase(name) || "Controller".equalsIgnoreCase(name)) {
                    accumulator.contribute(
                        source,
                        RootCapability.API_PROVIDER,
                        new Evidence("FrameworkScanner", "Found @" + name + " controller annotation", 0.5),
                        "REST API"
                    );
                }
                else if ("Configuration".equalsIgnoreCase(name)) {
                    accumulator.contribute(
                        source,
                        RootCapability.CONFIGURATION_PROVIDER,
                        new Evidence("FrameworkScanner", "Found @" + name + " service configuration", 0.5),
                        null
                    );
                }
                else if ("Test".equalsIgnoreCase(name) || "ParameterizedTest".equalsIgnoreCase(name)) {
                    accumulator.contribute(
                        source,
                        RootCapability.TEST_ENTRY,
                        new Evidence("FrameworkScanner", "Found @" + name + " test case annotation", 0.5),
                        "Tests"
                    );
                }
            }
        }
    }
}
