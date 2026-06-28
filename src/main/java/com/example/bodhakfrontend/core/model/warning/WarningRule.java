package com.example.bodhakfrontend.core.model.warning;

import com.example.bodhakfrontend.core.analysis.entityflag.EntityFlag;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * A warning rule that a language plugin provides or is loaded from config.
 * Supports pattern matching against identified IssueTypes.
 */
public class WarningRule {
    private String ruleId;
    private String title;
    private String description;
    private Severity severity;
    private String language;
    
    @JsonProperty("requiredIssues")
    private List<EntityFlag> requiredIssues = new ArrayList<>();
    @JsonProperty("forbiddenIssues")
    private List<EntityFlag> forbiddenIssues = new ArrayList<>();

    public WarningRule() {} // For Jackson

    public WarningRule(String ruleId, String title, String description, Severity severity, String language) {
        this.ruleId      = ruleId;
        this.title       = title;
        this.description = description;
        this.severity    = severity;
        this.language    = language;
    }

    // ── Getters/Setters for basic fields ──────────────────────────────────────
    public String getRuleId()      { return ruleId;      }
    public void setRuleId(String id) { this.ruleId = id; }
    
    public String getTitle()       { return title;       }
    public void setTitle(String t)  { this.title = t;    }
    
    public String getDescription() { return description; }
    public void setDescription(String d) { this.description = d; }
    
    public Severity getSeverity()  { return severity;    }
    public void setSeverity(Severity s) { this.severity = s; }
    
    public String getLanguage()    { return language;    }
    public void setLanguage(String l) { this.language = l;   }

    // ── Getters/Setters for matching logic ────────────────────────────────────
    public List<EntityFlag> getRequiredIssues() { 
        return requiredIssues == null ? List.of() : requiredIssues.stream().filter(java.util.Objects::nonNull).toList(); 
    }
    public void setRequiredIssues(List<EntityFlag> ri) { this.requiredIssues = ri; }
    
    public List<EntityFlag> getForbiddenIssues() { 
        return forbiddenIssues == null ? List.of() : forbiddenIssues.stream().filter(java.util.Objects::nonNull).toList(); 
    }
    public void setForbiddenIssues(List<EntityFlag> fi) { this.forbiddenIssues = fi; }

    // ── JSON compatibility helpers ──────────────────────────────────────────
    public void setMessage(String m) { this.description = m; this.title = "Analysis Warning"; }
    
    public boolean appliesTo(String languageId) {
        return language == null || language.isBlank() || language.equalsIgnoreCase(languageId);
    }
}
