package com.example.bodhakfrontend.sync.events;

import com.example.bodhakfrontend.engine.GraphSnapshot;
import com.example.bodhakfrontend.sync.api.UiUpdateEvent;

/**
 * Signals that the dependency graph was recomputed.
 * The Dependency Graph panel should re-render.
 *
 * <p>Carries the new immutable {@link GraphSnapshot} so the UI never touches
 * the mutable graph internals directly.
 *
 * <p>Last-write-wins — dispatcher collapses consecutive occurrences.
 */
public record DependencyGraphChangedEvent(GraphSnapshot snapshot) implements UiUpdateEvent {}
