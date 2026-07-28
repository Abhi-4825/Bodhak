package com.example.anuviya.frontend.python;

import com.example.anuviya.frontend.SyntaxHighlighter;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonSyntaxHighlighter implements SyntaxHighlighter {

    private static final String[] KEYWORDS = new String[] {
            "False", "None", "True", "and", "as", "assert", "async",
            "await", "break", "class", "continue", "def", "del",
            "elif", "else", "except", "finally", "for", "from",
            "global", "if", "import", "in", "is", "lambda", "nonlocal",
            "not", "or", "pass", "raise", "return", "try", "while",
            "with", "yield", "match", "case"
    };

    private static final String KEYWORD_PATTERN = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String PAREN_PATTERN = "\\(|\\)";
    private static final String BRACE_PATTERN = "\\{|\\}";
    private static final String BRACKET_PATTERN = "\\[|\\]";
    private static final String STRING_PATTERN = "(\"\"\"(.|\\R)*?\"\"\")|('''(.|\\R)*?''')|(\"([^\"\\\\]|\\\\.)*\")|('([^'\\\\]|\\\\.)*')";
    private static final String COMMENT_PATTERN = "#[^\n]*";
    private static final String NUMBER_PATTERN = "\\b\\d+(\\.\\d+)?\\b";
    private static final String DECORATOR_PATTERN = "@[a-zA-Z_][a-zA-Z0-9_\\.]*";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMENT>" + COMMENT_PATTERN + ")"
                    + "|(?<STRING>" + STRING_PATTERN + ")"
                    + "|(?<KEYWORD>" + KEYWORD_PATTERN + ")"
                    + "|(?<DECORATOR>" + DECORATOR_PATTERN + ")"
                    + "|(?<NUMBER>" + NUMBER_PATTERN + ")"
                    + "|(?<PAREN>" + PAREN_PATTERN + ")"
                    + "|(?<BRACE>" + BRACE_PATTERN + ")"
                    + "|(?<BRACKET>" + BRACKET_PATTERN + ")"
    );

    @Override
    public StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        int lastKwEnd = 0;
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();

        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            if (end > text.length()) end = text.length();

            if (start < lastKwEnd) continue;

            String styleClass =
                    matcher.group("KEYWORD") != null ? "keyword" :
                            matcher.group("COMMENT") != null ? "comment" :
                                    matcher.group("STRING") != null ? "string" :
                                            matcher.group("DECORATOR") != null ? "annotation" :
                                                    matcher.group("NUMBER") != null ? "number" :
                                                            matcher.group("PAREN") != null ? "paren" :
                                                                    matcher.group("BRACE") != null ? "brace" :
                                                                            matcher.group("BRACKET") != null ? "bracket" :
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
