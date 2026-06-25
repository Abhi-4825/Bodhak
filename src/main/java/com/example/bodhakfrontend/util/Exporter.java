package com.example.bodhakfrontend.util;

import com.example.bodhakfrontend.core.model.project.EntryPointInfo;

import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.core.model.project.UnusedEntityInfo;

import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;

import com.example.bodhakfrontend.core.analysis.AnalysisContext;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;

public class Exporter {
    public void exportAnalysis(AnalysisContext context) {
        ProjectInfo result = context.getProjectInfo();
        java.util.List<EntityInfo> entities = context.getEntities();

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
                    .entryPointInfo().getProjectFlavors().toString());
            out.println("Folders: " + result.knownFolders().size());
            out.println("Files: " + result.knownFiles().size());
            out.println();

            // Entry Points
            out.println("=== ENTRY POINTS ===");
            EntryPointInfo ep = result.entryPointInfo();
            out.println("Primary: " + ep.getPrimaryEntry().entityName());
            for (EntryPointInfo.Entry s : ep.getSecondaryEntries()) {
                out.println("Secondary: " + s.entityName());
            }
            out.println();



        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
