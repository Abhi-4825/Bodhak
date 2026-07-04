package com.example.bodhak.compiler.root.build;

import com.example.bodhak.compiler.root.EvidenceAccumulator;
import com.example.bodhak.compiler.symbol.SymbolTable;
import com.example.bodhak.compiler.symbol.ModuleSymbol;
import com.example.bodhak.model.project.Evidence;
import com.example.bodhak.model.project.RootCapability;

import java.io.File;
import java.nio.file.Files;

/**
 * Extracts evidence from python pyproject.toml or setup.py script configurations.
 */
public class PyProjectScanner implements BuildEvidenceProvider {

    @Override
    public boolean matches(File projectRoot) {
        return new File(projectRoot, "pyproject.toml").exists() || 
               new File(projectRoot, "setup.py").exists() || 
               new File(projectRoot, "requirements.txt").exists();
    }

    @Override
    public void extract(File projectRoot, SymbolTable symbolTable, EvidenceAccumulator accumulator) {
        ModuleSymbol projectSym = symbolTable.getOrCreateModule(projectRoot.getName());
        accumulator.contribute(
            projectSym, 
            RootCapability.LIBRARY_EXPORT, 
            new Evidence("BuildFileScanner", "Detected Python project boundaries", 0.3), 
            "Python Project"
        );

        File pyproj = new File(projectRoot, "pyproject.toml");
        File reqs = new File(projectRoot, "requirements.txt");
        File setup = new File(projectRoot, "setup.py");

        try {
            String content = "";
            if (pyproj.exists()) content += Files.readString(pyproj.toPath());
            if (reqs.exists()) content += Files.readString(reqs.toPath());
            if (setup.exists()) content += Files.readString(setup.toPath());

            if (content.contains("fastapi") || content.contains("uvicorn")) {
                accumulator.contribute(
                    projectSym,
                    RootCapability.API_PROVIDER,
                    new Evidence("BuildFileScanner", "Python configures FastAPI service bindings", 0.5),
                    "FastAPI Web Service"
                );
            }
            if (content.contains("django")) {
                accumulator.contribute(
                    projectSym,
                    RootCapability.API_PROVIDER,
                    new Evidence("BuildFileScanner", "Python configures Django application dependencies", 0.5),
                    "Django Web App"
                );
            }
            if (content.contains("console_scripts") || content.contains("scripts")) {
                accumulator.contribute(
                    projectSym,
                    RootCapability.EXECUTABLE,
                    new Evidence("BuildFileScanner", "Python configures console entrypoints", 0.5),
                    "CLI App"
                );
            }
        } catch (Exception e) {
            // Ignore
        }
    }
}
