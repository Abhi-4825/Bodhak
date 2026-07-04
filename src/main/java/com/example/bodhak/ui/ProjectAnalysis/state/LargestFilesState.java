package com.example.bodhak.ui.ProjectAnalysis.state;


import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.project.LargestFileInfo;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LargestFilesState {

    private final ObservableList<LargestFileItem> files =
            FXCollections.observableArrayList();

    public void update(AnalysisContext context) {

        files.clear();

        context.getProjectInfo()
                .largestFiles()
                .forEach(file ->

                        files.add(

                                new LargestFileItem(

                                        file.getName(),

                                        file.getSourceFile(),

                                        file.getLoc()

                                )
                        )
                );
    }

    public ObservableList<LargestFileItem> getFiles() {
        return files;
    }
}
