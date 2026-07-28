package com.example.anuviya.frontend.java;

import com.example.anuviya.frontend.LanguageFrontend;
import com.example.anuviya.ir.IRNode;
import com.example.anuviya.frontend.SyntaxHighlighter;
import com.github.javaparser.ast.CompilationUnit;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;

/**
 * Compiler frontend for the Java language.
 */
public class JavaLanguageFrontend implements LanguageFrontend {

    private final JavaParserService parserService;
    private final JavaIRBuilder irBuilder;
    private final JavaSyntaxHighlighter syntaxHighlighter;

    public JavaLanguageFrontend(List<Path> sourceRoots) {
        this.parserService = new JavaParserService(sourceRoots);
        this.irBuilder = new JavaIRBuilder();
        this.syntaxHighlighter = new JavaSyntaxHighlighter();
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
    public IRNode parseToIR(Path filePath) throws Exception {
        CompilationUnit cu = parserService.parse(filePath);
        return irBuilder.build(filePath, cu);
    }

    @Override
    public SyntaxHighlighter getSyntaxHighlighter() {
        return syntaxHighlighter;
    }
}
