package com.example.bodhakfrontend.sync.events;

import com.example.bodhakfrontend.sync.api.UiUpdateEvent;
import java.nio.file.Path;

/**
 * Signals that the on-disk content of {@code filePath} changed.
 * The code editor tab for this file should reload its content.
 */
public record EditorReloadEvent(Path filePath) implements UiUpdateEvent {}
