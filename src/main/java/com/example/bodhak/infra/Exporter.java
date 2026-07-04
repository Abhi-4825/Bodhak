package com.example.bodhak.infra;

import com.example.bodhak.model.project.ProjectRootInfo;
import com.example.bodhak.model.project.ProjectSurface;

import com.example.bodhak.model.project.ProjectInfo;
import com.example.bodhak.model.project.UnusedEntityInfo;

import javafx.stage.FileChooser;

import java.io.File;
import java.io.PrintWriter;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.entity.EntityInfo;

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
                    .projectRootInfo().detectedArchetypes().toString());
            out.println("Folders: " + result.knownFolders().size());
            out.println("Files: " + result.knownFiles().size());
            out.println();

            // Project Surfaces
            out.println("=== PROJECT SURFACES ===");
            ProjectRootInfo pri = result.projectRootInfo();
            for (ProjectSurface ps : pri.surfaces()) {
                out.println("Surface: " + ps.symbol().name() + " -> capabilities: " + ps.capabilities() + ", confidence: " + ps.confidence());
            }
            out.println();



        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

}
