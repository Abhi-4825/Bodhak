package com.example.bodhak.ui.ProjectAnalysis.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.project.ProjectInfo;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.Set;

public class ProjectSummaryState {

    private final IntegerProperty totalFiles =
            new SimpleIntegerProperty();

    private final IntegerProperty totalFolders =
            new SimpleIntegerProperty();

    private final ObservableList<LanguageInfo> languages =
            FXCollections.observableArrayList();

    public void update(AnalysisContext context) {

        ProjectInfo info = context.getProjectInfo();



        totalFiles.set(
                info.knownFiles().size()
        );

        totalFolders.set(
                info.knownFolders().size()
        );

        languages.clear();

        int total = info.languageCountMap()
                .values()
                .stream()
                .mapToInt(Set::size)
                .sum();

        info.languageCountMap().forEach((language, files) -> {

            double percent =
                    total == 0
                            ? 0
                            : (double) files.size() / total;

            languages.add(
                    new LanguageInfo(
                            language,
                            files.size(),
                            percent
                    )
            );
        });
    }



    public IntegerProperty totalFilesProperty() {
        return totalFiles;
    }

    public IntegerProperty totalFoldersProperty() {
        return totalFolders;
    }

    public ObservableList<LanguageInfo> getLanguages() {
        return languages;
    }
}
