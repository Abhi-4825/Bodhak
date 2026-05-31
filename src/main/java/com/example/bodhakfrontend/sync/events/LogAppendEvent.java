package com.example.bodhakfrontend.sync.events;

import com.example.bodhakfrontend.sync.api.UiUpdateEvent;

/**
 * Carries a single log line to be appended to the Log panel.
 *
 * <p>This is an <em>additive</em> event — multiple occurrences are NOT
 * collapsed by the dispatcher; every line is preserved in order.
 *
 * @param level   severity: "INFO", "WARN", "ERROR"
 * @param message human-readable log message
 */
public record LogAppendEvent(String level, String message) implements UiUpdateEvent {}
