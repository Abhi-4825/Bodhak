package com.example.anuviya.ui.analysisReport.model;


public record LanguageSummary(

        String language,

        int compilationUnits,

        int entities,

        long linesOfCode,

        double percentage

) {}
