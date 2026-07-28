package com.example.anuviya.compiler.root.build;

import com.example.anuviya.compiler.root.EvidenceAccumulator;
import com.example.anuviya.compiler.symbol.SymbolTable;
import com.example.anuviya.compiler.symbol.ModuleSymbol;
import com.example.anuviya.model.project.Evidence;
import com.example.anuviya.model.project.RootCapability;

import java.io.File;
import java.nio.file.Files;

/**
 * Extracts evidence from Gradle build.gradle files.
 */
public class GradleScanner implements BuildEvidenceProvider {

    @Override
    public boolean matches(File projectRoot) {
        return new File(projectRoot, "build.gradle").exists() || new File(projectRoot, "build.gradle.kts").exists();
    }

    @Override
    public void extract(File projectRoot, SymbolTable symbolTable, EvidenceAccumulator accumulator) {
        File gradle = new File(projectRoot, "build.gradle");
        if (!gradle.exists()) {
            gradle = new File(projectRoot, "build.gradle.kts");
        }
        
        ModuleSymbol projectSym = symbolTable.getOrCreateModule(projectRoot.getName());
        accumulator.contribute(
            projectSym, 
            RootCapability.LIBRARY_EXPORT, 
            new Evidence("BuildFileScanner", "Detected Gradle build script", 0.3), 
            "Gradle Project"
        );

        try {
            String content = Files.readString(gradle.toPath());
            if (content.contains("org.springframework.boot") || content.contains("springBoot")) {
                accumulator.contribute(
                    projectSym,
                    RootCapability.FRAMEWORK_BOOTSTRAP,
                    new Evidence("BuildFileScanner", "Gradle configures Spring Boot startup hooks", 0.5),
                    "Spring Boot App"
                );
            }
            if (content.contains("application") || content.contains("mainClass")) {
                accumulator.contribute(
                    projectSym,
                    RootCapability.EXECUTABLE,
                    new Evidence("BuildFileScanner", "Gradle configures executable run targets", 0.5),
                    "CLI App"
                );
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
