package com.example.anuviya.frontend;

import com.example.anuviya.ir.IRNode;

import java.nio.file.Path;
import java.util.Set;

/**
 * Interface representing the language-specific compiler frontend.
 * Responsible only for converting source code to normalized IR.
 */
public interface LanguageFrontend {

    /** Returns the unique identifier for the language (e.g. "java"). */
    String getId();

    /** Returns the set of file extensions supported by this frontend. */
    Set<String> getSupportedExtensions();

    /**
     * Parses the given source file and translates its syntax tree into a language-neutral IRNode.
     *
     * @param filePath the path to the source file to parse.
     * @return the normalized IRNode root of the file.
     * @throws Exception if parsing or translation fails.
     */
    IRNode parseToIR(Path filePath) throws Exception;

    /** Returns the optional syntax highlighter for the UI editor. */
    SyntaxHighlighter getSyntaxHighlighter();
}
