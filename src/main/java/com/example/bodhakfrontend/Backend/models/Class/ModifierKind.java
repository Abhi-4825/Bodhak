package com.example.bodhakfrontend.Backend.models.Class;

import com.github.javaparser.ast.Modifier;

public enum ModifierKind {
PUBLIC, PRIVATE, PROTECTED,
STATIC, ABSTRACT, FINAL, SYNCHRONIZED;

    public static ModifierKind from(Modifier.Keyword keyword) {
        return switch (keyword) {
            case PUBLIC -> PUBLIC;
            case PRIVATE -> PRIVATE;
            case PROTECTED -> PROTECTED;
            case STATIC -> STATIC;
            case ABSTRACT -> ABSTRACT;
            case FINAL -> FINAL;
            case SYNCHRONIZED -> SYNCHRONIZED;
            default -> null;
        };


}}