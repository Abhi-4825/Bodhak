package com.example.bodhakfrontend.sync.events;

import com.example.bodhakfrontend.core.model.project.ProjectInfo;
import com.example.bodhakfrontend.sync.api.UiUpdateEvent;

/**
 * Signals that the full project summary has been recomputed.
 * The Analyze panel and metrics cards should refresh.
 *
 * <p>This is a "last-write-wins" event — the dispatcher will collapse
 * multiple consecutive occurrences and apply only the most recent.
 *
 * @param projectInfo the freshly-built, immutable {@link ProjectInfo} snapshot.
 */
public record ProjectSummaryChangedEvent(ProjectInfo projectInfo) implements UiUpdateEvent {}
