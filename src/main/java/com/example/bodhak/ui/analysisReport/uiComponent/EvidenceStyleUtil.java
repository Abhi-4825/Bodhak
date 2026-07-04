package com.example.bodhak.ui.analysisReport.uiComponent;

/**
 * Shared color-coding for evidence categories, technology families, and
 * generic taxonomy chips, so the Project Classification and Framework
 * Detection overlays speak the same visual language.
 */
public final class EvidenceStyleUtil {

    private static final String[] CHIP_PALETTE = {
            "cyan", "blue", "green", "teal", "amber", "orange", "violet"
    };

    private EvidenceStyleUtil() {}

    /** Dot style for an EvidenceCategory or EvidenceType name (case-insensitive). */
    public static String dotStyle(String category) {
        return "ar-cat-dot-" + colorFor(category);
    }

    /** Text style for an EvidenceCategory or EvidenceType name. */
    public static String textStyle(String category) {
        return "ar-cat-text-" + colorFor(category);
    }

    /** Pill chip style for arbitrary taxonomy strings (family, category, etc). */
    public static String chipStyle(String value) {
        return "ar-chip-" + colorFor(value);
    }

    /**
     * Known evidence taxonomies get fixed, meaningful colors. Anything else
     * (e.g. a "family" string like "database" or "messaging") gets a stable
     * hash-based color so the same value always renders the same way.
     */
    private static String colorFor(String raw) {
        if (raw == null || raw.isBlank()) return "secondary";
        String key = raw.toUpperCase();
        return switch (key) {
            // EvidenceCategory (classification / framework evidence)
            case "DEPENDENCY", "BUILD_DEPENDENCY" -> "cyan";
            case "BUILD_FILE", "BUILD_PLUGIN" -> "blue";
            case "AST_ANNOTATION", "ANNOTATION_REFERENCE" -> "green";
            case "AST_PATTERN", "METHOD_REFERENCE", "METHOD_DECLARATION" -> "teal";
            case "CONFIGURATION" -> "amber";
            case "FILE_STRUCTURE", "PROJECT_ROOT" -> "orange";
            case "ENTITY_TAG", "IMPORT_REFERENCE" -> "violet";
            default -> CHIP_PALETTE[Math.floorMod(key.hashCode(), CHIP_PALETTE.length)];
        };
    }

    public static String humanize(String enumLike) {
        if (enumLike == null || enumLike.isBlank()) return "";
        return enumLike.replace("_", " ").toLowerCase();
    }
}