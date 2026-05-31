package com.example.bodhakfrontend.util;

import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import com.example.bodhakfrontend.core.model.project.Hotspot;
import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.model.project.UnusedEntityInfo;

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
            out.println("Primary: " + ep.getPrimaryEntry().entityName());
            for (EntryPointInfo.Entry s : ep.getSecondaryEntries()) {
                out.println("Secondary: " + s.entityName());
            }
            out.println();

            // Health Summary
            out.println("=== PROJECT HEALTH ===");

            out.println("Total Classes: " + result.getTotalEntities());
            out.println("Healthy: " + result.getHealthyEntities());
            out.println("Warnings: " + result.getEntitiesWithWarnings());
            out.println("God Classes: " + result.getGodEntities());
            out.println("Circular: " + result.getCircularEntities());
            out.println();

            // Hotspots
            out.println("=== HOTSPOTS ===");
            for (Hotspot h : result.getHotspots()) {
                out.println(
                        h.getEntity().getEntityName()
                                + " | Score=" + h.getScore()
                                + " | LOC=" + h.getEntity().getLinesOfCode()
                );
            }
            out.println();

            // Unused Classes
            out.println("=== UNUSED CLASSES ===");
            for (UnusedEntityInfo uc : result.getUnusedEntities()) {
                out.println(uc.getEntity().getEntityName() + " | LOC=" + uc.getEntity().getLinesOfCode());
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
