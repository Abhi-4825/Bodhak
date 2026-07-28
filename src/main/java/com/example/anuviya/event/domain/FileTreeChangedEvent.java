package com.example.anuviya.event.domain;

import com.example.anuviya.event.UiUpdateEvent;
import java.nio.file.Path;

/**
 * Signals that a folder's contents changed (file created, deleted, or renamed).
 * The UI should refresh the file tree node rooted at {@code affectedDirectory}.
 */
public record FileTreeChangedEvent(Path affectedDirectory) implements UiUpdateEvent {}
