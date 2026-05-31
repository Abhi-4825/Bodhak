package com.example.bodhakfrontend.core.plugin;

import java.util.Set;

/**
 * THE master contract every language plugin must implement.
 *
 * To add a new language (e.g. Kotlin):
 *   1. Create  languages/kotlin/KotlinLanguagePlugin.java
 *   2. Implement all methods below
 *   3. Register: registry.register(new KotlinLanguagePlugin())
 *   Zero other files need changing.
 *
 * Design rules:
 *  - Each method returns a STATELESS service object (or effectively stateless).
 *  - Plugins must never import from other plugins.
 *  - Plugins must never import from the ui/ package.
 */
public interface LanguagePlugin {
    String getId();
    Set<String> getSupportedExtensions();
    EntityNameExtractor getNameExtractor();
    EntityInfoBuilder getEntityBuilder();
    DependencyResolver getDependencyResolver();
    EntryPointDetector getEntryPointDetector();
    WarningRuleProvider getWarningProvider();
    SyntaxHighlighter getSyntaxHighlighter();
    /** Returns a language-specific AST builder. Never returns null. */
    AstProvider getAstProvider();
}
