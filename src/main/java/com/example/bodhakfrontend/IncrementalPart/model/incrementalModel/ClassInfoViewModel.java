package com.example.bodhakfrontend.IncrementalPart.model.incrementalModel;

import com.example.bodhakfrontend.IncrementalPart.model.Class.*;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.io.File;
import java.util.Map;
import java.util.Set;

public class ClassInfoViewModel {
    private final StringProperty name=new SimpleStringProperty();
    private final StringProperty simpleName=new SimpleStringProperty();
    private final StringProperty packageName=new SimpleStringProperty();
    private final ObjectProperty<File> sourceFile=new SimpleObjectProperty<>();
    private final ObjectProperty<ClassInfo.Kind> kind=new SimpleObjectProperty<>();
    private final ObservableSet<String> fields= FXCollections.observableSet();
    private final ObservableList<MethodInfo> methods=FXCollections.observableArrayList();
    private final ObservableList<ConstructorInfo> constructors=FXCollections.observableArrayList();
    private final ObservableSet<String> annotations=FXCollections.observableSet();
    private final ObservableSet<String> dependsOn=FXCollections.observableSet();
    private final ObservableSet<String> usedBy=FXCollections.observableSet();
    private final ObservableSet<Set<String>> circularDependencyGroups=FXCollections.observableSet();
    private final BooleanProperty isAbstract=new SimpleBooleanProperty();
    private final BooleanProperty isFinal=new SimpleBooleanProperty();
    private final BooleanProperty isPublic=new SimpleBooleanProperty();
    private final IntegerProperty linesOfCode=new SimpleIntegerProperty();
    private final IntegerProperty beginLine=new SimpleIntegerProperty();
    private final IntegerProperty beginColumn=new SimpleIntegerProperty();
    private final ObjectProperty<ClassContribution> classContribution=new SimpleObjectProperty<>();
    private final ObservableSet<IssueType> issueType =FXCollections.observableSet();
    private final ObservableList<String> warnings =FXCollections.observableArrayList();



    // extract Simple name
    private String extractSimpleName(String name){
        return name.substring(name.lastIndexOf(".")+1);
    }

    public ClassInfoViewModel(ClassInfo classInfo){
        name.set(classInfo.getClassName());
        simpleName.set(extractSimpleName(classInfo.getClassName()));
        packageName.set(classInfo.getPackageName());
        sourceFile.set(classInfo.getSourceFile());
        kind.set(classInfo.getKind());
        isAbstract.set(classInfo.isAbstract());
        isFinal.set(classInfo.isFinal());
        isPublic.set(classInfo.isPublic());
        linesOfCode.set(classInfo.getLinesOfCode());
        beginLine.set(classInfo.getBeginLine());
        beginColumn.set(classInfo.getBeginColumn());
        classContribution.set(classInfo.getClassContribution());
        // observables
        fields.addAll(classInfo.getFields());
        methods.addAll(classInfo.getMethods());
        constructors.addAll(classInfo.getConstructors());
        annotations.addAll(classInfo.getAnnotations());
        dependsOn.addAll(classInfo.getDependsOn());
        usedBy.addAll(classInfo.getUsedBy());
        circularDependencyGroups.addAll(classInfo.getCircularDependencyGroups());
        issueType.addAll(classInfo.getIssueType());
        warnings.addAll(classInfo.getWarnings());



    }
    public ObservableList<String> getWarnings() {
        return warnings;
    }

    public ObservableSet<IssueType> getIssueType() {
        return issueType;
    }

    public ClassContribution getClassContribution() {
        return classContribution.get();
    }

    public ObjectProperty<ClassContribution> classContributionProperty() {
        return classContribution;
    }

    public int getBeginColumn() {
        return beginColumn.get();
    }

    public IntegerProperty beginColumnProperty() {
        return beginColumn;
    }

    public int getBeginLine() {
        return beginLine.get();
    }

    public IntegerProperty beginLineProperty() {
        return beginLine;
    }

    public int getLinesOfCode() {
        return linesOfCode.get();
    }

    public IntegerProperty linesOfCodeProperty() {
        return linesOfCode;
    }

    public boolean isIsPublic() {
        return isPublic.get();
    }

    public BooleanProperty isPublicProperty() {
        return isPublic;
    }

    public boolean isIsFinal() {
        return isFinal.get();
    }

    public BooleanProperty isFinalProperty() {
        return isFinal;
    }

    public boolean isIsAbstract() {
        return isAbstract.get();
    }

    public BooleanProperty isAbstractProperty() {
        return isAbstract;
    }

    public ObservableSet<Set<String>> getCircularDependencyGroups() {
        return circularDependencyGroups;
    }

    public ObservableSet<String> getUsedBy() {
        return usedBy;
    }

    public ObservableSet<String> getDependsOn() {
        return dependsOn;
    }

    public ObservableSet<String> getAnnotations() {
        return annotations;
    }

    public ObservableList<ConstructorInfo> getConstructors() {
        return constructors;
    }

    public ObservableList<MethodInfo> getMethods() {
        return methods;
    }

    public ObservableSet<String> getFields() {
        return fields;
    }

    public ClassInfo.Kind getKind() {
        return kind.get();
    }

    public ObjectProperty<ClassInfo.Kind> kindProperty() {
        return kind;
    }

    public File getSourceFile() {
        return sourceFile.get();
    }

    public ObjectProperty<File> sourceFileProperty() {
        return sourceFile;
    }

    public String getPackageName() {
        return packageName.get();
    }

    public StringProperty packageNameProperty() {
        return packageName;
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }
    public StringProperty simpleNameProperty() {
        return simpleName;
    }



    public void updateFrom(ClassInfo ci) {

        // ---------- simple properties ----------
        name.set(ci.getClassName());
        simpleName.set(extractSimpleName(ci.getClassName()));
        packageName.set(ci.getPackageName());
        sourceFile.set(ci.getSourceFile());
        kind.set(ci.getKind());
        isAbstract.set(ci.isAbstract());
        isFinal.set(ci.isFinal());
        isPublic.set(ci.isPublic());
        linesOfCode.set(ci.getLinesOfCode());
        beginLine.set(ci.getBeginLine());
        beginColumn.set(ci.getBeginColumn());
        classContribution.set(ci.getClassContribution());

        // ---------- collections (CLEAR + ADD) ----------
        fields.clear();
        fields.addAll(ci.getFields());
        methods.setAll(ci.getMethods());
        constructors.setAll(ci.getConstructors());
        annotations.clear();
        annotations.addAll(ci.getAnnotations());
        dependsOn.clear();
        dependsOn.addAll(
                ci.getDependsOn()
        );

        usedBy.clear();
        usedBy.addAll(
              ci.getUsedBy()
        );

        circularDependencyGroups.clear();
        circularDependencyGroups.addAll(ci.getCircularDependencyGroups());
        issueType.clear();
        issueType.addAll(ci.getIssueType());
        warnings.setAll(ci.getWarnings());
    }



}


