package com.example.bodhakfrontend.languages.java;

import com.example.bodhakfrontend.core.plugin.*;
import com.example.bodhakfrontend.languages.java.analyzer.JavaEntryPointDetector;
import com.example.bodhakfrontend.languages.java.analyzer.JavaWarningProvider;
import com.example.bodhakfrontend.languages.java.ast.JavaAstProvider;
import com.example.bodhakfrontend.languages.java.extractor.JavaDependencyResolver;
import com.example.bodhakfrontend.languages.java.extractor.JavaEntityInfoBuilder;
import com.example.bodhakfrontend.languages.java.extractor.JavaEntityNameExtractor;
import com.example.bodhakfrontend.languages.java.highlight.JavaSyntaxHighlighter;
import com.example.bodhakfrontend.languages.java.parser.JavaParseCache;
import com.github.javaparser.ast.CompilationUnit;

import java.nio.file.Path;
import java.util.List;
import java.util.Set;

public class JavaLanguagePlugin implements LanguagePlugin {

    private final Parser<CompilationUnit> parser;
    private final JavaEntityNameExtractor nameExtractor;
    private final JavaEntityInfoBuilder entityBuilder;
    private final JavaDependencyResolver dependencyResolver;
    private final JavaEntryPointDetector entryPointDetector;
    private final JavaWarningProvider warningProvider;
    private final JavaSyntaxHighlighter syntaxHighlighter;
    private final JavaAstProvider astProvider;

    /**
     * Initializes the Java plugin with a set of source roots.
     * The source roots are needed by the JavaSymbolSolver.
     */
    public JavaLanguagePlugin(List<Path> sourceRoots) {
        this.parser = new JavaParseCache(sourceRoots);
        this.nameExtractor = new JavaEntityNameExtractor(parser);
        this.entityBuilder = new JavaEntityInfoBuilder(parser);
        this.dependencyResolver = new JavaDependencyResolver(parser);
        this.entryPointDetector = new JavaEntryPointDetector();
        this.warningProvider = new JavaWarningProvider();
        this.syntaxHighlighter = new JavaSyntaxHighlighter();
        this.astProvider = new JavaAstProvider(this.parser);
    }

    @Override
    public String getId() {
        return "java";
    }

    @Override
    public Set<String> getSupportedExtensions() {
        return Set.of(".java");
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
    public Parser<CompilationUnit> getParser() {
        return parser;
    }
}
