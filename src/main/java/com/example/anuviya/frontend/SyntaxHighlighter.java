package com.example.anuviya.frontend;

import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

public interface SyntaxHighlighter {
    StyleSpans<Collection<String>> computeHighlighting(String text);
}
