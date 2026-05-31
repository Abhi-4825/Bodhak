package com.example.bodhakfrontend.core.model.project;

import java.io.File;

/**
 * Information about a single large source file.
 * Extracted from the old ProjectInfo inner class for clarity.
 */
public class LargestFileInfo {
    private final String name;
    private final File sourceFile;
    private final int loc;

    public LargestFileInfo(String name, File sourceFile, int loc) {
        this.name       = name;
        this.sourceFile = sourceFile;
        this.loc        = loc;
    }

    public String getName()       { return name;       }
    public File getSourceFile()   { return sourceFile; }
    public int getLoc()           { return loc;        }
}
