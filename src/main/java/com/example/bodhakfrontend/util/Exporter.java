package com.example.bodhakfrontend.util;

import com.example.bodhakfrontend.Backend.models.Project.EntryPointInfo;
import com.example.bodhakfrontend.Backend.models.Project.Hotspots;
import com.example.bodhakfrontend.Backend.models.Project.ProjectInfo;
import com.example.bodhakfrontend.Backend.models.Project.UnusedClassInfo;

import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;

public class Exporter {
    public void exportAnalysis(ProjectInfo result) {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Analysis");
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text File", "*.txt")
        );

        File file = chooser.showSaveDialog(null);
        if (file == null) return;

        try (PrintWriter out = new PrintWriter(file)) {

            // Project Summary
            out.println("=== PROJECT SUMMARY ===");
            out.println("Type: " + result
                    .getEntryPointInfo().getProjectFlavors().toString());
            out.println("Folders: " + result.getKnownFolders().size());
            out.println("Files: " + result.getKnownFiles().size());
            out.println();

            // Entry Points
            out.println("=== ENTRY POINTS ===");
            EntryPointInfo ep = result.getEntryPointInfo();
            out.println("Primary: " + ep.getPrimaryEntry().className());
            for (EntryPointInfo.Entry s : ep.getSecondaryEntries()) {
                out.println("Secondary: " + s.className());
            }
            out.println();

            // Health Summary
            out.println("=== PROJECT HEALTH ===");

            out.println("Total Classes: " + result.getTotalClasses());
            out.println("Healthy: " + result.getHealthyClasses());
            out.println("Warnings: " + result.getClassesWithWarnings());
            out.println("God Classes: " + result.getGodClasses());
            out.println("Circular: " + result.getCircularClasses());
            out.println();

            // Hotspots
            out.println("=== HOTSPOTS ===");
            for (Hotspots h : result.getHotspotClasses()) {
                out.println(
                        h.getClassInfo().getClassName()
                                + " | Score=" + h.getScore()
                                + " | LOC=" + h.getClassInfo().getLinesOfCode()
                );
            }
            out.println();

            // Unused Classes
            out.println("=== UNUSED CLASSES ===");
            for (UnusedClassInfo uc : result.getUnusedClassInfos()) {
                out.println(uc.getClassInfo().getClassName() + " | LOC=" + uc.getClassInfo().getLinesOfCode());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
