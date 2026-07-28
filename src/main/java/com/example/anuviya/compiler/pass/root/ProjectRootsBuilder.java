package com.example.anuviya.compiler.pass.root;

import com.example.anuviya.compiler.CompilerPass;
import com.example.anuviya.compiler.PipelineContext;
import com.example.anuviya.compiler.symbol.SymbolTable;
import com.example.anuviya.context.db.ReferenceDatabase;
import com.example.anuviya.compiler.root.*;
import com.example.anuviya.model.project.ProjectRootInfo;
import java.io.File;
import java.util.List;

/**
 * Compiler pass that aggregates project boundary evidence to build the semantic ProjectRootInfo.
 */
public class ProjectRootsBuilder implements CompilerPass {
    private final List<RootEvidenceScanner> scanners = List.of(
        new BuildFileScanner(),
        new FrameworkScanner(),
        new IRScanner()
    );
    private final RootResolver resolver = new RootResolver();

    @Override
    public String getName() {
        return "ProjectRootsBuilder";
    }

    @Override
    public void execute(PipelineContext context) {
        String pathStr = (String) context.getAttribute("project_path");
        File projectPath = pathStr != null ? new File(pathStr) : new File(".");
        ReferenceDatabase database = (ReferenceDatabase) context.getAttribute("reference_database");
        SymbolTable symbolTable = (SymbolTable) context.getAttribute("symbol_table");

        if (database == null) database = new ReferenceDatabase();
        if (symbolTable == null) symbolTable = new SymbolTable();

        EvidenceAccumulator accumulator = new EvidenceAccumulator();

        for (RootEvidenceScanner scanner : scanners) {
            scanner.scan(projectPath, database, symbolTable, accumulator);
        }

        ProjectRootInfo roots = resolver.resolve(accumulator);
        context.setAttribute("project_roots", roots);
    }
}
