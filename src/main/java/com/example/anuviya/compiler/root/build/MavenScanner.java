package com.example.anuviya.compiler.root.build;

import com.example.anuviya.compiler.root.EvidenceAccumulator;
import com.example.anuviya.compiler.symbol.SymbolTable;
import com.example.anuviya.compiler.symbol.ModuleSymbol;
import com.example.anuviya.model.project.Evidence;
import com.example.anuviya.model.project.RootCapability;

import java.io.File;
import java.nio.file.Files;

/**
 * Extracts project archetype evidence from Maven pom.xml configuration files.
 */
public class MavenScanner implements BuildEvidenceProvider {

    @Override
    public boolean matches(File projectRoot) {
        return new File(projectRoot, "pom.xml").exists();
    }

    @Override
    public void extract(File projectRoot, SymbolTable symbolTable, EvidenceAccumulator accumulator) {
        File pom = new File(projectRoot, "pom.xml");
        ModuleSymbol projectSym = symbolTable.getOrCreateModule(projectRoot.getName());
        
        accumulator.contribute(
            projectSym, 
            RootCapability.LIBRARY_EXPORT, 
            new Evidence("BuildFileScanner", "Detected Maven pom.xml project", 0.3), 
            "Maven Project"
        );

        try {
            String content = Files.readString(pom.toPath());
            if (content.contains("spring-boot-maven-plugin") || content.contains("spring-boot")) {
                accumulator.contribute(
                    projectSym,
                    RootCapability.FRAMEWORK_BOOTSTRAP,
                    new Evidence("BuildFileScanner", "Maven configures Spring Boot bootstrap plugins", 0.5),
                    "Spring Boot App"
                );
            }
            if (content.contains("exec-maven-plugin") || content.contains("mainClass")) {
                accumulator.contribute(
                    projectSym,
                    RootCapability.EXECUTABLE,
                    new Evidence("BuildFileScanner", "Maven configures executable mainClass execution", 0.5),
                    "CLI App"
                );
            }
        } catch (Exception e) {
            // Ignore parse errors
        }
    }
}
