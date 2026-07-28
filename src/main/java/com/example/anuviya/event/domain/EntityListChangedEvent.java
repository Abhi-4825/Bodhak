package com.example.anuviya.event.domain;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.event.UiUpdateEvent;

import java.util.List;

/**
 * Signals that the entity (class / function / module) list changed.
 * Carries the minimal diff needed to update the UI list without full rebuild.
 *
 * @param added   newly discovered entities (empty list if none)
 * @param removed entities that no longer exist (empty list if none)
 * @param updated entities whose content changed (empty list if none)
 */
public record EntityListChangedEvent(
        List<EntityInfo> added,
        List<EntityInfo> removed,
        List<EntityInfo> updated
) implements UiUpdateEvent {}
