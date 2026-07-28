package com.example.anuviya.frontend.python;

import com.example.anuviya.frontend.LanguageFrontend;
import com.example.anuviya.ir.IRNode;
import com.example.anuviya.frontend.SyntaxHighlighter;
import org.treesitter.TSTree;
import java.nio.file.Path;
import java.util.Set;

import java.util.List;
import java.util.Collections;

/**
 * Compiler frontend for the Python language.
 */
public class PythonLanguageFrontend implements LanguageFrontend {

    private final PythonTreeSitterParser parserService;
    private final PythonIRBuilder irBuilder;
    private final PythonSyntaxHighlighter syntaxHighlighter;

    public PythonLanguageFrontend() {
        this(Collections.emptyList());
    }

    public PythonLanguageFrontend(List<Path> sourceRoots) {
        this.parserService = new PythonTreeSitterParser();
        this.irBuilder = new PythonIRBuilder(sourceRoots);
        this.syntaxHighlighter = new PythonSyntaxHighlighter();
    }

    @Override
    public String getId() {
        return "python";
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".py");
    }

    @Override
    public IRNode parseToIR(Path filePath) throws Exception {
        TSTree tree = parserService.parse(filePath);
        return irBuilder.build(filePath, tree);
    }

    @Override
    public SyntaxHighlighter getSyntaxHighlighter() {
        return syntaxHighlighter;
    }
}
