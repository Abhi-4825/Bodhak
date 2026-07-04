package com.example.bodhak.compiler.root.build;

import com.example.bodhak.compiler.root.EvidenceAccumulator;
import com.example.bodhak.compiler.symbol.SymbolTable;
import com.example.bodhak.compiler.symbol.ModuleSymbol;
import com.example.bodhak.model.project.Evidence;
import com.example.bodhak.model.project.RootCapability;

import java.io.File;
import java.nio.file.Files;

/**
 * Extracts evidence from NPM package.json configuration files.
 */
public class PackageJsonScanner implements BuildEvidenceProvider {

    @Override
    public boolean matches(File projectRoot) {
        return new File(projectRoot, "package.json").exists();
    }

    @Override
    public void extract(File projectRoot, SymbolTable symbolTable, EvidenceAccumulator accumulator) {
        File pkgJson = new File(projectRoot, "package.json");
        ModuleSymbol projectSym = symbolTable.getOrCreateModule(projectRoot.getName());
        
        accumulator.contribute(
            projectSym, 
            RootCapability.LIBRARY_EXPORT, 
            new Evidence("BuildFileScanner", "Detected Node package.json file", 0.3), 
            "Node Project"
        );

        try {
            String content = Files.readString(pkgJson.toPath());
            if (content.contains("\"react\"")) {
                accumulator.contribute(
                    projectSym,
                    RootCapability.API_PROVIDER,
                    new Evidence("BuildFileScanner", "Node package targets React frontend modules", 0.5),
                    "React Web App"
                );
            }
            if (content.contains("\"bin\"") || content.contains("\"main\"")) {
                accumulator.contribute(
                    projectSym,
                    RootCapability.EXECUTABLE,
                    new Evidence("BuildFileScanner", "Node package configures bin/main run commands", 0.5),
                    "CLI App"
                );
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
