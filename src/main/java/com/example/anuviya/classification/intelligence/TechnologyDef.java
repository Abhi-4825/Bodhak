package com.example.anuviya.classification.intelligence;

import java.util.ArrayList;
import java.util.List;

public class TechnologyDef {
    private String id;
    private int schemaVersion = 1;
    private String displayName;
    private String family;
    private String category;
    private VersionDetection versionDetection;
    private List<EvidenceRule> evidence = new ArrayList<>();
    private List<String> capabilities = new ArrayList<>();
    private Integer godClassLocThreshold;
    private Integer highCouplingFanOutThreshold;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public int getSchemaVersion() { return schemaVersion; }
    public void setSchemaVersion(int schemaVersion) { this.schemaVersion = schemaVersion; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getFamily() { return family; }
    public void setFamily(String family) { this.family = family; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public VersionDetection getVersionDetection() { return versionDetection; }
    public void setVersionDetection(VersionDetection versionDetection) { this.versionDetection = versionDetection; }

    public List<EvidenceRule> getEvidence() { return evidence; }
    public void setEvidence(List<EvidenceRule> evidence) { this.evidence = evidence; }

    public List<String> getCapabilities() { return capabilities; }
    public void setCapabilities(List<String> capabilities) { this.capabilities = capabilities; }

    public Integer getGodClassLocThreshold() { return godClassLocThreshold; }
    public void setGodClassLocThreshold(Integer threshold) { this.godClassLocThreshold = threshold; }

    public Integer getHighCouplingFanOutThreshold() { return highCouplingFanOutThreshold; }
    public void setHighCouplingFanOutThreshold(Integer threshold) { this.highCouplingFanOutThreshold = threshold; }

    public static class VersionDetection {
        private String buildDependency;

        public String getBuildDependency() { return buildDependency; }
        public void setBuildDependency(String buildDependency) { this.buildDependency = buildDependency; }
    }

    public static class EvidenceRule {
        private String type; // BUILD_DEPENDENCY, BUILD_PLUGIN, ANNOTATION_REFERENCE, METHOD_REFERENCE, etc.
        private String pattern;
        private List<String> aliases = new ArrayList<>();

        public String getType() { return type; }
        public void setType(String type) { this.type = type; }

        public String getPattern() { return pattern; }
        public void setPattern(String pattern) { this.pattern = pattern; }

        public List<String> getAliases() { return aliases; }
        public void setAliases(List<String> aliases) { this.aliases = aliases; }
    }
}
