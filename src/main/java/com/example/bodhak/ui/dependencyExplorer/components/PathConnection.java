package com.example.bodhak.ui.dependencyExplorer.components;

public record PathConnection(
        String from,
        String to,
        String referenceType,
        int count,
        String details
) {}
