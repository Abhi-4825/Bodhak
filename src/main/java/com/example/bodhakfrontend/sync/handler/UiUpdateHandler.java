package com.example.bodhakfrontend.sync.handler;

import com.example.bodhakfrontend.sync.api.UiUpdateEvent;

/**
 * Handles a specific subset of {@link UiUpdateEvent} types.
 *
 * <p>Implementations are always invoked on the JavaFX Application Thread by the
 * {@link com.example.bodhakfrontend.sync.bus.UpdateDispatcher}.
 * They may safely mutate JavaFX observables and call UI APIs.
 *
 * <h3>Registration</h3>
 * Handlers are registered with the {@link com.example.bodhakfrontend.sync.bus.UpdateDispatcher}
 * at project-load time. The dispatcher calls {@link #canHandle(UiUpdateEvent)} first;
 * only matching handlers receive {@link #apply(UiUpdateEvent)}.
 */
public interface UiUpdateHandler {

    /**
     * Returns {@code true} if this handler should process the given event.
     * Called on the JavaFX Application Thread — must not block.
     */
    boolean canHandle(UiUpdateEvent event);

    /**
     * Applies the event to the UI or UIStore.
     * Called on the JavaFX Application Thread — must not block or perform I/O.
     *
     * @param event guaranteed to have passed {@link #canHandle(UiUpdateEvent)}
     */
    void apply(UiUpdateEvent event);
}
