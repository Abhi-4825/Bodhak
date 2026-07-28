package com.example.anuviya.event.domain;

import com.example.anuviya.event.UiUpdateEvent;
import java.nio.file.Path;

/**
 * Signals that {@code filePath} was deleted.
 * The editor tab and any overview tabs for this file should be closed.
 */
public record EditorCloseEvent(Path filePath) implements UiUpdateEvent {}
