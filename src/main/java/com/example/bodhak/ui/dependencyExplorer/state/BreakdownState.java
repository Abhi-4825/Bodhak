package com.example.bodhak.ui.dependencyExplorer.state;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.model.entity.EntityInfo;
import com.example.bodhak.model.reference.SemanticReference;
import javafx.beans.property.*;

public class BreakdownState {

    private final IntegerProperty methodCalls = new SimpleIntegerProperty(0);
    private final IntegerProperty fieldAccesses = new SimpleIntegerProperty(0);
    private final IntegerProperty typeReferences = new SimpleIntegerProperty(0);
    private final IntegerProperty annotations = new SimpleIntegerProperty(0);
    private final IntegerProperty framework = new SimpleIntegerProperty(0);
    private final IntegerProperty imports = new SimpleIntegerProperty(0);
    private final IntegerProperty other = new SimpleIntegerProperty(0);

    public void clear() {
        methodCalls.set(0);
        fieldAccesses.set(0);
        typeReferences.set(0);
        annotations.set(0);
        framework.set(0);
        imports.set(0);
        other.set(0);
    }

    public void update(AnalysisContext context, EntityInfo entity) {
        clear();
        if (context == null || entity == null || context.getReferenceDatabase() == null) return;

        String name = entity.getEntityName();

        for (SemanticReference ref : context.getReferenceDatabase().getAllReferences()) {
            boolean isSource = ref.sourceSymbol().name().startsWith(name);
            boolean isTarget = ref.targetSymbol().name().startsWith(name);
            
            if (isSource || isTarget) {
                switch (ref.kind()) {
                    case CALL -> methodCalls.set(methodCalls.get() + 1);
                    case MEMBER -> fieldAccesses.set(fieldAccesses.get() + 1);
                    case TYPE -> typeReferences.set(typeReferences.get() + 1);
                    case ANNOTATION -> annotations.set(annotations.get() + 1);
                    case FRAMEWORK -> framework.set(framework.get() + 1);
                    case IMPORT -> imports.set(imports.get() + 1);
                    default -> other.set(other.get() + 1);
                }
            }
        }
    }

    public ReadOnlyIntegerProperty methodCallsProperty() { return methodCalls; }
    public ReadOnlyIntegerProperty fieldAccessesProperty() { return fieldAccesses; }
    public ReadOnlyIntegerProperty typeReferencesProperty() { return typeReferences; }
    public ReadOnlyIntegerProperty annotationsProperty() { return annotations; }
    public ReadOnlyIntegerProperty frameworkProperty() { return framework; }
    public ReadOnlyIntegerProperty importsProperty() { return imports; }
    public ReadOnlyIntegerProperty otherProperty() { return other; }
}
