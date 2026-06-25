package com.example.bodhakfrontend.core.model.incremental;

import com.example.bodhakfrontend.core.model.entity.EntityContribution;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.entity.EntityKind;
import com.example.bodhakfrontend.core.model.entity.MemberInfo;
import com.example.bodhakfrontend.core.model.warning.WarningRule;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableSet;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * JavaFX-observable wrapper around EntityInfo for live UI binding.
 */
public class EntityViewModel {

    private final StringProperty entityName = new SimpleStringProperty();
    private final StringProperty namespaceName = new SimpleStringProperty();
    private final IntegerProperty linesOfCode = new SimpleIntegerProperty();
    private final SimpleObjectProperty<File> sourceFile = new SimpleObjectProperty<>();
    private final IntegerProperty beginLine = new SimpleIntegerProperty();
    private final IntegerProperty beginColumn = new SimpleIntegerProperty();
    private final SimpleObjectProperty<EntityKind> kind = new SimpleObjectProperty<>();
    private final StringProperty language = new SimpleStringProperty();
    
    private final ObservableList<MemberInfo> members = FXCollections.observableArrayList();
    private final ObservableSet<String> fields = FXCollections.observableSet();
    private final ObservableSet<String> decorators = FXCollections.observableSet();
    private final ObservableSet<String> dependsOn = FXCollections.observableSet();
    private final ObservableSet<String> usedBy = FXCollections.observableSet();
    private final ObservableList<Set<String>> circularGroups = FXCollections.observableArrayList();
    private final ObservableList<WarningRule> warnings = FXCollections.observableArrayList();
    
    private EntityContribution contribution;

    public EntityViewModel(EntityInfo entity) {
        update(entity);
    }

    public void update(EntityInfo entity) {
        entityName.set(entity.getEntityName());
        namespaceName.set(entity.getNamespaceName());
        linesOfCode.set(entity.getLinesOfCode());
        sourceFile.set(entity.getSourceFile());
        beginLine.set(entity.getBeginLine());
        beginColumn.set(entity.getBeginColumn());
        kind.set(entity.getKind());
        language.set(entity.getLanguage());
        this.contribution = entity.getContribution();

        members.setAll(entity.getMembers());
        fields.clear();
        fields.addAll(entity.getFields());
        decorators.clear();
        decorators.addAll(entity.getDecorators());
        
        dependsOn.clear();
        dependsOn.addAll(entity.getDependsOn());

        usedBy.clear();
        usedBy.addAll(entity.getUsedBy());

        circularGroups.setAll(entity.getCircularGroups());

    }

    // ── Properties ───────────────────────────────────────────────────────────
    public StringProperty entityNameProperty()    { return entityName;    }
    public StringProperty namespaceNameProperty() { return namespaceName; }
    public IntegerProperty linesOfCodeProperty()  { return linesOfCode;  }
    public ObjectProperty<File> sourceFileProperty() { return sourceFile; }
    public IntegerProperty beginLineProperty()    { return beginLine;     }
    public IntegerProperty beginColumnProperty()  { return beginColumn;   }
    public ObjectProperty<EntityKind> kindProperty() { return kind;       }
    public StringProperty languageProperty()      { return language;     }

    // ── Observable Collections ──────────────────────────────────────────────
    public ObservableList<MemberInfo> getMembers() { return members; }
    public ObservableSet<String> getFields()       { return fields; }
    public ObservableSet<String> getDependsOn()    { return dependsOn; }
    public ObservableSet<String> getUsedBy()       { return usedBy;    }
    public ObservableList<Set<String>> getCircularDependencyGroups() { return circularGroups; }
    public ObservableList<WarningRule> getWarnings() { return warnings; }

    // ── Getters ──────────────────────────────────────────────────────────────
    public String getEntityName()    { return entityName.get();    }
    public String getNamespaceName() { return namespaceName.get(); }
    public int getLinesOfCode()      { return linesOfCode.get();   }
    public File getSourceFile()     { return sourceFile.get();    }
    public int getBeginLine()       { return beginLine.get();     }
    public int getBeginColumn()     { return beginColumn.get();   }
    public EntityKind getKind()     { return kind.get();          }
    public String getLanguage()     { return language.get();      }

    // ── Reconstruction ───────────────────────────────────────────────────────
    public EntityInfo toEntityInfo() {
        EntityInfo info = new EntityInfo(
            getEntityName(), getNamespaceName(), getSourceFile(),
            getKind(), getLanguage(),
            new HashSet<>(fields), new ArrayList<>(members), new HashSet<>(decorators),
            new HashSet<>(dependsOn), new HashSet<>(usedBy), new HashSet<>(circularGroups),
            false, false, true, // Modifiers (can refine if needed)
            getLinesOfCode(), getBeginLine(), getBeginColumn(),
            contribution
        );

        return info;
    }
    
    // ── UI Compatibility ────────────────────────────────────────────────────
    public StringProperty simpleNameProperty() { return entityName; }
    public String getName() { return getEntityName(); }

    /** Convenience alias for {@link #toEntityInfo()}. */
    public EntityInfo getEntity() { return toEntityInfo(); }
}
