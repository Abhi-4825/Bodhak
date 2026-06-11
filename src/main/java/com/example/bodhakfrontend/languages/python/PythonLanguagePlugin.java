package com.example.bodhakfrontend.languages.python;

import com.example.bodhakfrontend.core.plugin.*;
import com.example.bodhakfrontend.languages.python.analyzer.PythonEntryPointDetector;
import com.example.bodhakfrontend.languages.python.analyzer.PythonWarningProvider;
import com.example.bodhakfrontend.languages.python.ast.PythonAstProvider;
import com.example.bodhakfrontend.languages.python.extractor.PythonDependencyResolver;
import com.example.bodhakfrontend.languages.python.extractor.PythonEntityInfoBuilder;
import com.example.bodhakfrontend.languages.python.extractor.PythonEntityNameExtractor;
import com.example.bodhakfrontend.languages.python.highlight.PythonSyntaxHighlighter;
import com.example.bodhakfrontend.languages.python.parser.PythonParseCache;
import org.treesitter.TSTree;

import java.util.Set;

public class PythonLanguagePlugin implements LanguagePlugin {

    private final Parser<TSTree> parser;
    private final PythonEntityNameExtractor nameExtractor;
    private final PythonEntityInfoBuilder entityBuilder;
    private final PythonDependencyResolver dependencyResolver;
    private final PythonEntryPointDetector entryPointDetector;
    private final PythonWarningProvider warningProvider;
    private final PythonSyntaxHighlighter syntaxHighlighter;
    private final PythonAstProvider astProvider;

    public PythonLanguagePlugin() {
        this.parser = new PythonParseCache();
        this.nameExtractor = new PythonEntityNameExtractor(parser);
        this.entityBuilder = new PythonEntityInfoBuilder(parser);
        this.dependencyResolver = new PythonDependencyResolver(parser);
        this.entryPointDetector = new PythonEntryPointDetector();
        this.warningProvider = new PythonWarningProvider();
        this.syntaxHighlighter = new PythonSyntaxHighlighter();
        this.astProvider = new PythonAstProvider(this.parser);
    }

    @Override
    public String getId() {
        return "python";
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".py", ".pyw");
    }

    @Override
    public EntityNameExtractor getNameExtractor() {
        return nameExtractor;
    }

    @Override
    public EntityInfoBuilder getEntityBuilder() {
        return entityBuilder;
    }

    @Override
    public DependencyResolver getDependencyResolver() {
        return dependencyResolver;
    }

    @Override
    public EntryPointDetector getEntryPointDetector() {
        return entryPointDetector;
    }

    @Override
    public WarningRuleProvider getWarningProvider() {
        return warningProvider;
    }

    @Override
    public SyntaxHighlighter getSyntaxHighlighter() {
        return syntaxHighlighter;
    }

    @Override
    public AstProvider getAstProvider() {
        return astProvider;
    }

    @Override
    public Parser<TSTree> getParser() {
        return parser;
    }
}
