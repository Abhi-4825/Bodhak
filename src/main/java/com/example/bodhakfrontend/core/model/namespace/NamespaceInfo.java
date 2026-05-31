package com.example.bodhakfrontend.core.model.namespace;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;

import java.util.HashSet;
import java.util.Set;

/**
 * Language-neutral namespace container.
 *
 * In Java  → represents a package  (e.g. "com.example.service")
 * In Python → represents a module  (e.g. "myapp.utils")
 */
public class NamespaceInfo {

    private final String namespaceName;
    private final Set<NamespaceWarning> warnings = new HashSet<>();
    private final Set<EntityInfo> entities       = new HashSet<>();
    private final Set<String> dependsOn          = new HashSet<>();
    private final Set<String> usedBy             = new HashSet<>();
    private final Set<Set<String>> circularGroups = new HashSet<>();
    private boolean partOfCycle;

    public NamespaceInfo(String namespaceName) {
        this.namespaceName = namespaceName;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getNamespaceName()              { return namespaceName; }
    public Set<EntityInfo> getEntities()          { return entities;      }
    public Set<String> getDependsOn()             { return dependsOn;     }
    public Set<String> getUsedBy()                { return usedBy;        }
    public Set<Set<String>> getCircularGroups()   { return circularGroups;}
    public Set<NamespaceWarning> getWarnings()    { return warnings;      }
    public boolean isPartOfCycle()                { return partOfCycle;   }

    // ── Mutators ──────────────────────────────────────────────────────────────

    public void setPartOfCycle(boolean value) { this.partOfCycle = value; }

    public void setWarnings(Set<NamespaceWarning> newWarnings) {
        warnings.clear();
        warnings.addAll(newWarnings);
    }

    public void setCircularGroups(Set<Set<String>> cycles) {
        circularGroups.clear();
        circularGroups.addAll(cycles);
        partOfCycle = !cycles.isEmpty();
    }

    @Override
    public String toString() { return namespaceName; }
}
