package com.example.bodhakfrontend.sync.events;

import com.example.bodhakfrontend.sync.api.UiUpdateEvent;

/**
 * Carries a background-task progress update for the bottom progress bar.
 *
 * @param fraction 0.0–1.0 (negative means indeterminate)
 * @param label    short human-readable status label, e.g. "Indexing…"
 */
public record ProgressEvent(double fraction, String label) implements UiUpdateEvent {

    /** Convenience factory for indeterminate progress. */
    public static ProgressEvent indeterminate(String label) {
        return new ProgressEvent(-1.0, label);
    }

    /** Convenience factory for completion. */
    public static ProgressEvent done() {
        return new ProgressEvent(1.0, "");
    }
}
