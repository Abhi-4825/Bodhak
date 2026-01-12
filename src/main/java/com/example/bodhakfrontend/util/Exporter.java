package com.example.bodhakfrontend.util;

import com.example.bodhakfrontend.Models.*;
import com.example.bodhakfrontend.Models.PackageAnalysis.EntryPointInfo;
import com.example.bodhakfrontend.Models.PackageAnalysis.ProjectAnalysisResult;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;

public class Exporter {
    public void exportAnalysis(ProjectAnalysisResult result) {

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
            out.println("Type: " + result.getPackageAnalysisInfo()
                    .getEntryPointInfo().getProjectType());
            out.println("Folders: " + result.getPackageAnalysisInfo().getFolderCount());
            out.println("Files: " + result.getPackageAnalysisInfo().getFileCount());
            out.println();

            // Entry Points
            out.println("=== ENTRY POINTS ===");
            EntryPointInfo ep = result.getPackageAnalysisInfo().getEntryPointInfo();
            out.println("Primary: " + ep.getPrimaryEntry());
            for (String s : ep.getSecondaryEntries()) {
                out.println("Secondary: " + s);
            }
            out.println();

            // Health Summary
            out.println("=== PROJECT HEALTH ===");
            ProjectHealthSummary hs = result.getProjectHealthSummary();
            out.println("Total Classes: " + hs.getTotalClasses());
            out.println("Healthy: " + hs.getHealthyClasses());
            out.println("Warnings: " + hs.getClassesWithWarnings());
            out.println("God Classes: " + hs.getGodClasses());
            out.println("Circular: " + hs.getCircularClasses());
            out.println();

            // Hotspots
            out.println("=== HOTSPOTS ===");
            for (HotspotInfo h : result.getHotspotInfos()) {
                out.println(
                        h.getClassName()
                                + " | Score=" + h.getScore()
                                + " | LOC=" + h.getLoc()
                );
            }
            out.println();

            // Unused Classes
            out.println("=== UNUSED CLASSES ===");
            for (UnusedClassInfo uc : result.getUnusedClassInfos()) {
                out.println(uc.getClassName() + " | LOC=" + uc.getLoc());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
