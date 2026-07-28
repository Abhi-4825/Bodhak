package com.example.anuviya.ui.analysisReport.state;

import com.example.anuviya.context.AnalysisContext;
import com.example.anuviya.ui.analysisReport.model.LanguageSummary;
import com.example.anuviya.ui.analysisReport.model.ProjectSize;
import com.example.anuviya.ui.analysisReport.util.ProjectSizeClassifier;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExecutiveSummaryState implements  AnalysisReportSection{

    private final StringProperty projectName =
            new SimpleStringProperty();

    private final IntegerProperty compilationUnits =
            new SimpleIntegerProperty();

    private final IntegerProperty namespaces =
            new SimpleIntegerProperty();

    private final IntegerProperty entities =
            new SimpleIntegerProperty();

    private final IntegerProperty members =
            new SimpleIntegerProperty();

    private final LongProperty linesOfCode =
            new SimpleLongProperty();

    private final StringProperty primaryLanguage =
            new SimpleStringProperty();

    private final ObservableList<LanguageSummary> languages =
            FXCollections.observableArrayList();

    private final ObjectProperty<ProjectSize> projectSize =
            new SimpleObjectProperty<>(ProjectSize.UNKNOWN);

    private final DoubleProperty health =
            new SimpleDoubleProperty(-1);

    private final StringProperty analysisTime =
            new SimpleStringProperty();

    private final ObjectProperty<LocalDateTime> lastAnalysis =
            new SimpleObjectProperty<>();




    @Override
    public void update(AnalysisContext context) {
 if(context==null ){return;}
 // projectname
        projectName.set(context.getProjectInfo().projectName());
 //count compilationUNit
        compilationUnits.set(context.getCompilationUnits().size());
        // count namespace
        namespaces.set(context.getNamespaces().size());
        //count entities
        entities.set(context.getEntities().size());
        // count members
        members.set(

                context.getEntities()

                        .stream()

                        .mapToInt(e -> e.getStructure().memberNames().size())

                        .sum()

        );
        // lines of code
        linesOfCode.set(context.getEntities().stream().mapToLong(e->e.getMetrics().linesOfCode()).sum());
        //languages
 buildLanguageSummary(context);

        // ProjectSize
        projectSize.set(ProjectSizeClassifier.classify(entities.get(),linesOfCode.get()));

    }
    private void buildLanguageSummary(AnalysisContext context) {

        Map<String, LanguageAccumulator> languageMap = new LinkedHashMap<>();

        // Count compilation units
        context.getCompilationUnits().forEach(unit -> {

            String language = "Unknown";

            if (unit.getLanguageMetadata() != null) {
                language = unit.getLanguageMetadata().languageId();
            }
            languageMap
                    .computeIfAbsent(language, l -> new LanguageAccumulator())
                    .compilationUnits++;
        });

        // Count entities and LOC
        context.getEntities().forEach(entity -> {

            String language = entity
                    .getLanguage();

            LanguageAccumulator accumulator =
                    languageMap.computeIfAbsent(
                            language,
                            l -> new LanguageAccumulator()
                    );

            accumulator.entities++;

            accumulator.linesOfCode +=
                    entity.getMetrics().linesOfCode();
        });

        int totalEntities =
                Math.max(entities.get(), 1);

        List<LanguageSummary> summaries =
                languageMap.entrySet()
                        .stream()
                        .map(entry -> {

                            LanguageAccumulator acc =
                                    entry.getValue();

                            double percentage =
                                    (acc.entities * 100.0)
                                            / totalEntities;

                            return new LanguageSummary(

                                    entry.getKey(),

                                    acc.compilationUnits,

                                    acc.entities,

                                    acc.linesOfCode,

                                    percentage

                            );

                        })
                        .sorted(
                                Comparator.comparingInt(
                                        LanguageSummary::entities
                                ).reversed()
                        )
                        .toList();

        languages.setAll(summaries);

        if (!summaries.isEmpty()) {
            primaryLanguage.set(
                    summaries.getFirst().language()
            );
        } else {
            primaryLanguage.set("Unknown");
        }
    }
    private static final class LanguageAccumulator {

        int compilationUnits;
        int entities;
        long linesOfCode;

    }


    private void clear() {

        projectName.set("");

        compilationUnits.set(0);

        namespaces.set(0);

        entities.set(0);

        members.set(0);

        linesOfCode.set(0);

        primaryLanguage.set("Unknown");

        languages.clear();

        projectSize.set(ProjectSize.UNKNOWN);

        health.set(-1);

        analysisTime.set("");

        lastAnalysis.set(null);
    }

    public LocalDateTime getLastAnalysis() {
        return lastAnalysis.get();
    }

    public ObjectProperty<LocalDateTime> lastAnalysisProperty() {
        return lastAnalysis;
    }

    public String getAnalysisTime() {
        return analysisTime.get();
    }

    public StringProperty analysisTimeProperty() {
        return analysisTime;
    }

    public double getHealth() {
        return health.get();
    }

    public DoubleProperty healthProperty() {
        return health;
    }

    public ProjectSize getProjectSize() {
        return projectSize.get();
    }

    public ObjectProperty<ProjectSize> projectSizeProperty() {
        return projectSize;
    }

    public ObservableList<LanguageSummary> getLanguages() {
        return languages;
    }

    public String getPrimaryLanguage() {
        return primaryLanguage.get();
    }

    public StringProperty primaryLanguageProperty() {
        return primaryLanguage;
    }

    public long getLinesOfCode() {
        return linesOfCode.get();
    }

    public LongProperty linesOfCodeProperty() {
        return linesOfCode;
    }

    public int getMembers() {
        return members.get();
    }

    public IntegerProperty membersProperty() {
        return members;
    }

    public int getEntities() {
        return entities.get();
    }

    public IntegerProperty entitiesProperty() {
        return entities;
    }

    public int getNamespaces() {
        return namespaces.get();
    }

    public IntegerProperty namespacesProperty() {
        return namespaces;
    }

    public int getCompilationUnits() {
        return compilationUnits.get();
    }

    public IntegerProperty compilationUnitsProperty() {
        return compilationUnits;
    }

    public String getProjectName() {
        return projectName.get();
    }

    public StringProperty projectNameProperty() {
        return projectName;
    }
}
