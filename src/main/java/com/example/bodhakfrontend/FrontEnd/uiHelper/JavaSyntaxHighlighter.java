package com.example.bodhakfrontend.FrontEnd.uiHelper;



import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaSyntaxHighlighter {

    private static final String[] KEYWORDS = new String[] {
            "abstract", "assert", "boolean", "break", "byte",
            "case", "catch", "char", "class", "const",
            "continue", "default", "do", "double", "else",
            "enum", "extends", "final", "finally", "float",
            "for", "goto", "if", "implements", "import",
            "instanceof", "int", "interface", "long", "native",
            "new", "package", "private", "protected", "public",
            "return", "short", "static", "strictfp", "super",
            "switch", "synchronized", "this", "throw", "throws",
            "transient", "try", "void", "volatile", "while", "var", "true", "false", "null"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String ORANGE_TYPES_PATTERN = "\\b([A-Z][a-zA-Z0-9_]*)\\b"; // User definitions or Classes
    private static final String ANNOTATION_PATTERN = "@[a-zA-Z0-9]+";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String SEMICOLON_PATTERN = "\\;";
    private static final String STRING_PATTERN = "\"([^\"\\\\]|\\\\.)*\"";
    private static final String COMMENT_PATTERN = "//[^\n]*" + "|" + "/\\*(.|\\R)*?\\*/";
    private static final String NUMBER_PATTERN = "\\b\\d+\\b";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<ANNOTATION>" + ANNOTATION_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
                    + "|(?<SEMICOLON>" + SEMICOLON_PATTERN + ")"
                    + "|(?<TYPES>" + ORANGE_TYPES_PATTERN + ")"
    );

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {

        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;

        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {

            int start = matcher.start();
            int end = matcher.end();
            if (end > text.length()) {
                end = text.length();
            }

            //  CRITICAL FIX: prevent overlap / negative spans
            if (start < lastKwEnd) {
                continue;
            }

            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("COMMENT") != null ? "comment" :
                                    matcher.group("STRING") != null ? "string" :
                                            matcher.group("ANNOTATION") != null ? "annotation" :
                                                    matcher.group("NUMBER") != null ? "number" :
                                                            matcher.group("PAREN") != null ? "paren" :
                                                                    matcher.group("BRACE") != null ? "brace" :
                                                                            matcher.group("BRACKET") != null ? "bracket" :
                                                                                    matcher.group("SEMICOLON") != null ? "semicolon" :
                                                                                            matcher.group("TYPES") != null ? "types" :
                                                                                                    null;

            if (styleClass == null) continue;

            // gap
            if (start > lastKwEnd) {
                spansBuilder.add(Collections.emptyList(), start - lastKwEnd);
            }
            // match
            spansBuilder.add(Collections.singleton(styleClass), end - start);

            lastKwEnd = end;

        }

        //  FINAL GUARANTEE

        if (lastKwEnd < text.length()) {
            spansBuilder.add(Collections.emptyList(), text.length() - lastKwEnd);
        }

//  CREATE SPANS
        StyleSpans<Collection<String>> spans = spansBuilder.create();

//  VALIDATION (VERY IMPORTANT)
        int total = spans.stream().mapToInt(s -> s.getLength()).sum();

        if (total != text.length()) {
            System.out.println("⚠ Span mismatch, skipping highlight");

            return new StyleSpansBuilder<Collection<String>>()
                    .add(Collections.emptyList(), text.length())
                    .create();
        }

        return spans;
    }
}

