package com.example.anuviya.model.namespace;

import com.example.anuviya.model.entity.EntityInfo;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

    // Pre-computed architecture metrics
    private int fanIn;
    private int fanOut;
    private double instabilityScore;
    private double abstractness;
    private double distance;
    private double riskScore;
    private final List<String> findings = new ArrayList<>();

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

    public int getFanIn()                         { return fanIn;         }
    public int getFanOut()                        { return fanOut;        }
    public double getInstabilityScore()           { return instabilityScore; }
    public double getAbstractness()               { return abstractness;  }
    public double getDistance()                   { return distance;      }
    public double getRiskScore()                  { return riskScore;     }
    public List<String> getFindings()             { return findings;      }

    // ── Mutators ──────────────────────────────────────────────────────────────

    public void setPartOfCycle(boolean value) { this.partOfCycle = value; }
    
    public void setFanIn(int fanIn) { this.fanIn = fanIn; }
    public void setFanOut(int fanOut) { this.fanOut = fanOut; }
    public void setInstabilityScore(double score) { this.instabilityScore = score; }
    public void setAbstractness(double score) { this.abstractness = score; }
    public void setDistance(double score) { this.distance = score; }
    public void setRiskScore(double score) { this.riskScore = score; }

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
