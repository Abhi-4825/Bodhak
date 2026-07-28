package com.example.anuviya.ui.main.uiHelper;



import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PythonSyntaxHighlighter {

    private static final String[] KEYWORDS = {
            "False", "None", "True", "and", "as", "assert", "async", "await",
            "break", "class", "continue", "def", "del", "elif", "else",
            "except", "finally", "for", "from", "global", "if", "import",
            "in", "is", "lambda", "nonlocal", "not", "or", "pass",
            "raise", "return", "try", "while", "with", "yield"
    };

    private static final String KEYWORD_PATTERN    = "\\b(" + String.join("|", KEYWORDS) + ")\\b";
    private static final String DECORATOR_PATTERN  = "@[a-zA-Z_][a-zA-Z0-9_.]*";
    private static final String STRING_PATTERN     = "\"\"\"[\\s\\S]*?\"\"\"|'''[\\s\\S]*?'''|\"[^\"\\\\]*\"|'[^'\\\\]*'";
    private static final String COMMENT_PATTERN    = "#[^\n]*";
    private static final String NUMBER_PATTERN     = "\\b\\d+(\\.\\d+)?\\b";
    private static final String TYPES_PATTERN      = "\\b([A-Z][a-zA-Z0-9_]*)\\b";
    private static final String PAREN_PATTERN      = "[()\\[\\]{}]";

    private static final Pattern PATTERN = Pattern.compile(
            "(?<COMMENT>"   + COMMENT_PATTERN   + ")"
                    + "|(?<STRING>"     + STRING_PATTERN    + ")"
                    + "|(?<KEYWORD>"    + KEYWORD_PATTERN   + ")"
                    + "|(?<DECORATOR>"  + DECORATOR_PATTERN + ")"
                    + "|(?<NUMBER>"     + NUMBER_PATTERN    + ")"
                    + "|(?<TYPES>"      + TYPES_PATTERN     + ")"
                    + "|(?<PAREN>"      + PAREN_PATTERN     + ")"
    );

    public static StyleSpans<Collection<String>> computeHighlighting(String text) {
        Matcher matcher = PATTERN.matcher(text);
        StyleSpansBuilder<Collection<String>> builder = new StyleSpansBuilder<>();
        int lastEnd = 0;

        while (matcher.find()) {
            int start = matcher.start();
            if (start < lastEnd) continue;

            String styleClass =
                    matcher.group("KEYWORD")   != null ? "keyword"    :
                            matcher.group("COMMENT")   != null ? "comment"    :
                                    matcher.group("STRING")    != null ? "string"     :
                                            matcher.group("DECORATOR") != null ? "annotation" :  // reuse .annotation CSS
                                                    matcher.group("NUMBER")    != null ? "number"     :
                                                            matcher.group("PAREN")     != null ? "paren"      :
                                                                    matcher.group("TYPES")     != null ? "types"      : null;

            if (styleClass == null) continue;
            if (start > lastEnd) builder.add(Collections.emptyList(), start - lastEnd);
            builder.add(Collections.singleton(styleClass), matcher.end() - start);
            lastEnd = matcher.end();
        }

        if (lastEnd < text.length()) builder.add(Collections.emptyList(), text.length() - lastEnd);
        return builder.create();
    }
}
