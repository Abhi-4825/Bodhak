package com.example.bodhak.compiler.root;

import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.compiler.symbol.SymbolTable;
import com.example.bodhak.compiler.root.build.*;
import java.io.File;
import java.util.List;

/**
 * Scans build tools and files (Maven, Gradle, NPM, Poetry) to gather project root evidence.
 */
public class BuildFileScanner implements RootEvidenceScanner {
    private final List<BuildEvidenceProvider> providers = List.of(
        new MavenScanner(),
        new GradleScanner(),
        new PackageJsonScanner(),
        new PyProjectScanner()
    );

    @Override
    public void scan(File projectRoot, ReferenceDatabase database, SymbolTable symbolTable, EvidenceAccumulator accumulator) {
        for (BuildEvidenceProvider provider : providers) {
            if (provider.matches(projectRoot)) {
                provider.extract(projectRoot, symbolTable, accumulator);
            }
        }
    }
}
