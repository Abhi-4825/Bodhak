package com.example.bodhakfrontend.core.plugin;

import org.fxmisc.richtext.model.StyleSpans;

import java.util.Collection;

public interface SyntaxHighlighter {
    StyleSpans<Collection<String>> computeHighlighting(String text);
}
