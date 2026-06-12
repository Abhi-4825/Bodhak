package com.example.bodhakfrontend.core.model.entity;

import com.example.bodhakfrontend.core.model.warning.WarningRule;

import java.io.File;
import java.util.*;

/**
 * Language-neutral representation of any code entity in a project.
 *
 * Replaces the old {@code ClassInfo} which was tightly coupled to Java.
 *
 * Covers:
 *   Java  → CLASS, INTERFACE, ENUM, RECORD
 *   Python → CLASS, FUNCTION, MODULE, SCRIPT
 *   Future → OBJECT, TRAIT, STRUCT (no code changes needed)
 *
 * The {@code language} field lets the UI and analysers know which plugin
 * produced this entity without instanceof checks.
 */
public class EntityInfo {

    // ── Core identity ─────────────────────────────────────────────────────────
    /** Fully-qualified name: "com.example.Foo" in Java, "mymodule.MyClass" in Python. */
    private final String entityName;
    /** Namespace name: package for Java, module path for Python. */
    private final String namespaceName;
    private final File sourceFile;
    private final EntityKind kind;
    /** Language id that produced this entity: "java", "python", etc. */
    private final String language;

    // ── Structure ─────────────────────────────────────────────────────────────
    private final Set<String> fields;
    /** Unified: methods + constructors + functions — all under MemberInfo. */
    private final List<MemberInfo> members;
    /** Decorators / annotations as raw strings. */
    private final Set<String> decorators;

    // ── Dependencies ──────────────────────────────────────────────────────────
    private final Set<String> dependsOn;
    private final Set<String> usedBy;
    private final Set<Set<String>> circularGroups;

    // ── Modifiers ─────────────────────────────────────────────────────────────
    private final boolean isAbstract;
    private final boolean isFinal;
    private final boolean isPublic;

    // ── Metrics ───────────────────────────────────────────────────────────────
    private final int linesOfCode;
    private final int beginLine;
    private final int beginColumn;

    // ── Plugin-provided contribution ──────────────────────────────────────────
    private final EntityContribution contribution;

    // ── Mutable post-build state ──────────────────────────────────────────────
    private final Set<IssueType> issueCache   = new HashSet<>();
    private final List<WarningRule> warnings  = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────

    public EntityInfo(String entityName, String namespaceName, File sourceFile,
                      EntityKind kind, String language,
                      Set<String> fields, List<MemberInfo> members, Set<String> decorators,
                      Set<String> dependsOn, Set<String> usedBy, Set<Set<String>> circularGroups,
                      boolean isAbstract, boolean isFinal, boolean isPublic,
                      int linesOfCode, int beginLine, int beginColumn,
                      EntityContribution contribution) {
        this.entityName     = entityName;
        this.namespaceName  = namespaceName;
        this.sourceFile     = sourceFile;
        this.kind           = kind;
        this.language       = language;
        this.fields         = fields;
        this.members        = members;
        this.decorators     = decorators;
        this.dependsOn      = dependsOn;
        this.usedBy         = usedBy;
        this.circularGroups = circularGroups;
        this.isAbstract     = isAbstract;
        this.isFinal        = isFinal;
        this.isPublic       = isPublic;
        this.linesOfCode    = linesOfCode;
        this.beginLine      = beginLine;
        this.beginColumn    = beginColumn;
        this.contribution   = contribution;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public String getEntityName()               { return entityName;     }
    public String getNamespaceName()            { return namespaceName;  }
    public File getSourceFile()                 { return sourceFile;     }
    public EntityKind getKind()                 { return kind;           }
    public String getLanguage()                 { return language;       }
    public Set<String> getFields()              { return fields;         }
    public List<MemberInfo> getMembers()        { return members;        }
    public Set<String> getDecorators()          { return decorators;     }
    public Set<String> getDependsOn()           { return dependsOn;      }
    public Set<String> getUsedBy()              { return usedBy;         }
    public Set<Set<String>> getCircularGroups() { return circularGroups; }
    public boolean isAbstract()                 { return isAbstract;     }
    public boolean isFinal()                    { return isFinal;        }
    public boolean isPublic()                   { return isPublic;       }
    public int getLinesOfCode()                 { return linesOfCode;    }
    public int getBeginLine()                   { return beginLine;      }
    public int getBeginColumn()                 { return beginColumn;    }
    public EntityContribution getContribution() { return contribution;   }

    // ── Convenience helpers ───────────────────────────────────────────────────

    /** Count only METHOD-kind members (useful for hotspot scoring). */
    public long getMethodCount() {
        return members.stream().filter(m -> m.getKind() == MemberKind.METHOD || m.getKind() == MemberKind.FUNCTION).count();
    }

    /** Count only CONSTRUCTOR-kind members. */
    public long getConstructorCount() {
        return members.stream().filter(m -> m.getKind() == MemberKind.CONSTRUCTOR).count();
    }

    // ── Issue detection ───────────────────────────────────────────────────────

    private void detectIssues() {
        issueCache.clear();
        if (isGodEntity())                               issueCache.add(IssueType.GOD_CLASS);
        if (dependsOn.size() > 10)      issueCache.add(IssueType.HIGH_COUPLING);
        if (!circularGroups.isEmpty())                   issueCache.add(IssueType.CIRCULAR_DEPENDENCY);
        if (fields.size() >= 3 && members.size() <= 2)  issueCache.add(IssueType.ANEMIC_DOMAIN);
    }

    private boolean isGodEntity() {
        return linesOfCode > 500 || members.size() > 20 || fields.size() > 15;
    }

    public Set<IssueType> getIssueType() {
        detectIssues();
        return Collections.unmodifiableSet(issueCache);
    }

    // ── Warning management ────────────────────────────────────────────────────

    public void setWarnings(List<WarningRule> rules) {
        warnings.clear();
        warnings.addAll(rules);
    }

    public List<WarningRule> getWarnings() { return warnings; }

    // ── Display ───────────────────────────────────────────────────────────────

    /** Simple (unqualified) name for display. */
    public String getSimpleName() {
        int dot = entityName.lastIndexOf('.');
        return dot >= 0 ? entityName.substring(dot + 1) : entityName;
    }

    @Override
    public String toString() { return getSimpleName(); }
}
