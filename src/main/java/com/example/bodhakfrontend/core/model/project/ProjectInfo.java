package com.example.bodhakfrontend.core.model.project;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.namespace.NamespaceInfo;

import java.nio.file.Path;
import java.util.*;

/**
 * Aggregated result of a full project analysis.
 * Completely language-neutral — every field is driven by EntityInfo.
 */
public class ProjectInfo {

    private final Map<String, Set<Path>> languageCountMap;
    private final List<LargestFileInfo> largestFiles;
    private final Set<Path> knownFolders;
    private final Set<Path> knownFiles;
    private final Map<String, NamespaceInfo> namespaceInfos;
    private final List<EntityInfo> entities;

    // ── Computed metrics ──────────────────────────────────────────────────────
    private final int totalEntities;
    private final int healthyEntities;
    private final int entitiesWithWarnings;
    private final int godEntities;
    private final int circularEntities;
    private final int highlyCoupledEntities;

    private final List<Hotspot> hotspots;
    private final EntryPointInfo entryPointInfo;
    private final Set<UnusedEntityInfo> unusedEntities;

    public ProjectInfo(Map<String, Set<Path>> languageCountMap,
                       List<LargestFileInfo> largestFiles,
                       Set<Path> knownFolders,
                       Set<Path> knownFiles,
                       List<EntityInfo> entities,
                       Map<String, NamespaceInfo> namespaceInfos,
                       List<Hotspot> hotspots,
                       EntryPointInfo entryPointInfo,
                       Set<UnusedEntityInfo> unusedEntities,
                       int totalEntities, int healthyEntities,
                       int entitiesWithWarnings, int godEntities,
                       int circularEntities, int highlyCoupledEntities) {

        this.languageCountMap       = languageCountMap;
        this.largestFiles           = largestFiles;
        this.knownFolders           = knownFolders;
        this.knownFiles             = knownFiles;
        this.entities               = entities;
        this.namespaceInfos         = namespaceInfos;
        this.hotspots               = hotspots;
        this.entryPointInfo         = entryPointInfo;
        this.unusedEntities         = unusedEntities;
        this.totalEntities          = totalEntities;
        this.healthyEntities        = healthyEntities;
        this.entitiesWithWarnings   = entitiesWithWarnings;
        this.godEntities            = godEntities;
        this.circularEntities       = circularEntities;
        this.highlyCoupledEntities  = highlyCoupledEntities;
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public Map<String, Set<Path>> getLanguageCountMap()     { return languageCountMap;      }
    public List<LargestFileInfo> getLargestFiles()          { return largestFiles;          }
    public Set<Path> getKnownFolders()                      { return knownFolders;          }
    public Set<Path> getKnownFiles()                        { return knownFiles;            }
    public Map<String, NamespaceInfo> getNamespaceInfos()   { return namespaceInfos;        }
    public List<EntityInfo> getEntities()                   { return entities;              }
    public int getTotalEntities()                           { return totalEntities;         }
    public int getHealthyEntities()                         { return healthyEntities;       }
    public int getEntitiesWithWarnings()                    { return entitiesWithWarnings;  }
    public int getGodEntities()                             { return godEntities;           }
    public int getCircularEntities()                        { return circularEntities;      }
    public int getHighlyCoupledEntities()                   { return highlyCoupledEntities; }
    public List<Hotspot> getHotspots()                      { return hotspots;              }
    public EntryPointInfo getEntryPointInfo()               { return entryPointInfo;        }
    public Set<UnusedEntityInfo> getUnusedEntities()        { return unusedEntities;        }

    // ── Derived helpers ───────────────────────────────────────────────────────

    public List<EntityInfo> getEntitiesInCycles() {
        return entities.stream()
                .filter(e -> !e.getCircularGroups().isEmpty())
                .toList();
    }

    public Map<String, EntityInfo> getEntityMap() {
        Map<String, EntityInfo> map = new HashMap<>();
        for (EntityInfo e : entities) map.put(e.getEntityName(), e);
        return map;
    }
}
