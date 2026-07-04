package com.example.bodhak.ui.analysisReport.compiler;

import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UniversalSyntaxHighlighter {

    private static final String[] JAVA_KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else", "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import", "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public", "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while",
            "var", "true", "false", "null"
    };

    private static final String[] PYTHON_KEYWORDS = new String[] {
            "False", "None", "True", "and", "as", "assert", "async", "await", "break", "class", "continue",
            "def", "del", "elif", "else", "except", "finally", "for", "from", "global", "if", "import", "in",
            "is", "lambda", "nonlocal", "not", "or", "pass", "raise", "return", "try", "while", "with", "yield"
    };

    private static final String[] C_KEYWORDS = new String[] {
            "auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else", "enum",
            "extern", "float", "for", "goto", "if", "inline", "int", "long", "register", "restrict", "return",
            "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void",
            "volatile", "while", "_Alignas", "_Alignof", "_Atomic", "_Bool", "_Complex", "_Generic", "_Imaginary",
            "noreturn", "_Static_assert", "_Thread_local"
    };

    private static final Pattern JAVA_PATTERN = buildPattern(JAVA_KEYWORDS, "//[^\n]*|/\\*(.|\\R)*?\\*/");
    private static final Pattern PYTHON_PATTERN = buildPattern(PYTHON_KEYWORDS, "#[^\n]*");
    private static final Pattern C_PATTERN = buildPattern(C_KEYWORDS, "//[^\n]*|/\\*(.|\\R)*?\\*/");

    private static Pattern buildPattern(String[] keywords, String commentPattern) {
        String keywordPattern = "\\b(" + String.join("|", keywords) + ")\\b";
        String orangeTypesPattern = "\\b([A-Z][a-zA-Z0-9_]*)\\b";
        String stringPattern = "\"([^\"\\\\]|\\\\.)*\"|'([^'\\\\]|\\\\.)*'";
        String numberPattern = "\\b\\d+\\b";

        return Pattern.compile(
                "(?<COMMENT>" + commentPattern + ")"
                        + "|(?<STRING>" + stringPattern + ")"
                        + "|(?<KEYWORD>" + keywordPattern + ")"
                        + "|(?<NUMBER>" + numberPattern + ")"
                        + "|(?<TYPES>" + orangeTypesPattern + ")"
        );
    }

    public static StyleSpans<Collection<String>> computeHighlighting(String text, String fileName) {
        Pattern pattern;
        if (fileName != null && (fileName.endsWith(".py") || fileName.endsWith(".pyw"))) {
            pattern = PYTHON_PATTERN;
        } else if (fileName != null && (fileName.endsWith(".c") || fileName.endsWith(".h") || fileName.endsWith(".cpp") || fileName.endsWith(".hpp"))) {
            pattern = C_PATTERN;
        } else {
            pattern = JAVA_PATTERN;
        }

        Matcher matcher = pattern.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (end > text.length()) {
                end = text.length();
            }

            if (start < lastKwEnd) {
                continue;
            }

            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("COMMENT") != null ? "comment" :
                                    matcher.group("STRING") != null ? "string" :
                                            matcher.group("NUMBER") != null ? "number" :
                                                    matcher.group("TYPES") != null ? "types" :
                                                            null;

            if (styleClass == null) continue;

            if (start > lastKwEnd) {
                spansBuilder.add(Collections.emptyList(), start - lastKwEnd);
            }
            spansBuilder.add(Collections.singleton(styleClass), end - start);
            lastKwEnd = end;
        }

        if (lastKwEnd < text.length()) {
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        }

        return spansBuilder.create();
    }
}
