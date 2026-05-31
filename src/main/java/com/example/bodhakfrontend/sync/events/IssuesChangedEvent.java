package com.example.bodhakfrontend.sync.events;

import com.example.bodhakfrontend.core.model.project.UnusedEntityInfo;
import com.example.bodhakfrontend.core.model.warning.WarningRule;
import com.example.bodhakfrontend.sync.api.UiUpdateEvent;

import java.util.List;
import java.util.Set;

/**
 * Signals that the issue / warning set changed.
 * The Issues panel should refresh its list.
 *
 * <p>Last-write-wins — dispatcher collapses consecutive occurrences.
 */
public record IssuesChangedEvent(
        List<WarningRule> warnings,
        Set<UnusedEntityInfo> unusedEntities
) implements UiUpdateEvent {}
