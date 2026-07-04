package com.example.bodhak.frontend.python;

import com.example.bodhak.frontend.LanguageFrontend;
import com.example.bodhak.ir.IRNode;
import com.example.bodhak.frontend.python.PythonIRBuilder;
import com.example.bodhak.frontend.python.PythonTreeSitterParser;
import com.example.bodhak.frontend.SyntaxHighlighter;
import com.example.bodhak.frontend.python.PythonSyntaxHighlighter;
import org.treesitter.TSTree;
import java.nio.file.Path;
import java.util.Set;

/**
 * Compiler frontend for the Python language.
 */
public class PythonLanguageFrontend implements LanguageFrontend {

    private final PythonTreeSitterParser parserService;
    private final PythonIRBuilder irBuilder;
    private final PythonSyntaxHighlighter syntaxHighlighter;

    public PythonLanguageFrontend() {
        this.parserService = new PythonTreeSitterParser();
        this.irBuilder = new PythonIRBuilder();
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
