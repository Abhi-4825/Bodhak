package com.example.anuviya.event.handler;

import com.example.anuviya.event.UiUpdateEvent;
import com.example.anuviya.event.domain.LogAppendEvent;
import com.example.anuviya.event.domain.ProgressEvent;
import com.example.anuviya.event.store.UIStore;

/**
 * Handles {@link LogAppendEvent} and {@link ProgressEvent}.
 *
 * <p>Runs on the JavaFX Application Thread.
 */
public final class LogAndProgressHandler implements UiUpdateHandler {

    private final UIStore store;

    public LogAndProgressHandler(UIStore store) {
        this.store = store;
    }

    @Override
    public boolean canHandle(UiUpdateEvent event) {
        return event instanceof LogAppendEvent || event instanceof ProgressEvent;
    }

    @Override
    public void apply(UiUpdateEvent event) {
        if (event instanceof LogAppendEvent e) {
            store.appendLog(e.level(), e.message());
        } else if (event instanceof ProgressEvent e) {
            store.setProgress(e.fraction(), e.label());
        }
    }
}
