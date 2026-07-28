package com.example.anuviya.event.domain;

import com.example.anuviya.event.UiUpdateEvent;
import java.nio.file.Path;

/**
 * Signals that the on-disk content of {@code filePath} changed.
 * The code editor tab for this file should reload its content.
 */
public record EditorReloadEvent(Path filePath) implements UiUpdateEvent {}
