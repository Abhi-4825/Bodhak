package com.example.bodhakfrontend.util;

import java.io.File;
import java.util.Set;

public final class IgnoreRules {

    private static final Set<String> IGNORED_DIR_NAMES = Set.of(
            // build outputs
            "target", "build", "out", "dist", "bin", "obj",

            // dependencies
            "node_modules", "vendor", "venv", ".env",
            "site-packages", ".gradle", ".mvn",

            // VCS
            ".git", ".hg", ".svn",

            // IDEs
            ".idea", ".vscode", ".eclipse", ".settings",

            // OS / cache
            "__pycache__", ".cache"
    );

    private IgnoreRules() {}

    public static boolean shouldIgnore(File file) {
        File current = file;
        while (current != null) {
            if (IGNORED_DIR_NAMES.contains(current.getName())) {
                return true;
            }
            current = current.getParentFile();
        }
        return false;
    }
}

