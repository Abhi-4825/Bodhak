package com.example.anuviya.event;

/**
 * Marker interface for all UI update events in Bodhak.
 *
 * <p>All concrete event types are records in the {@code sync.events} package.
 * The {@link com.example.anuviya.event.dispatch.UpdateDispatcher} uses
 * {@code instanceof} pattern matching to route events to handlers — providing
 * the same exhaustive-checking benefit as a sealed class hierarchy.
 *
 * <h3>Extending the event set</h3>
 * <ol>
 *   <li>Create a new record in {@code sync.events} that implements {@code UiUpdateEvent}.</li>
 *   <li>Create a matching {@link com.example.anuviya.event.handler.UiUpdateHandler}.</li>
 * </ol>
 * No other infrastructure changes are required.
 */
public interface UiUpdateEvent {
}
