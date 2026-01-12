package com.example.bodhakfrontend.util;

import com.example.bodhakfrontend.Models.Severity;
public class SeverityStyle {

    public static String color(Severity severity) {
        return switch (severity) {
            case HIGH -> "#d32f2f";   // red
            case MEDIUM -> "#f9a825"; // amber
            case LOW -> "#388e3c";    // green
        };
    }

    public static String icon(Severity severity) {
        return switch (severity) {
            case HIGH -> "🔴";
            case MEDIUM -> "🟡";
            case LOW -> "🟢";
        };
    }
}
