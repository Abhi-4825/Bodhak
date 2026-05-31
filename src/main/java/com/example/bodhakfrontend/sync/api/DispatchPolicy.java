package com.example.bodhakfrontend.sync.api;

import java.util.List;

/**
 * Strategy interface governing how the {@link com.example.bodhakfrontend.sync.bus.UpdateDispatcher}
 * batches, collapses, and schedules UI update events.
 *
 * <h3>Current implementations</h3>
 * <ul>
 *   <li>{@link com.example.bodhakfrontend.sync.bus.FixedIntervalPolicy} — fires
 *       at a fixed tick rate derived from a {@link DispatchProfile}.</li>
 * </ul>
 *
 * <h3>Planned implementations (not in scope for this PR)</h3>
 * <ul>
 *   <li>{@code AdaptiveLoadPolicy} — widens the interval under CPU load,
 *       narrows it during idle periods.</li>
 *   <li>{@code IdleOptimizedPolicy} — batches aggressively during bursts,
 *       flushes immediately when the queue drains.</li>
 * </ul>
 */
public interface DispatchPolicy {

    /**
     * Returns how many milliseconds the dispatcher should wait between drain ticks.
     * Called once during {@link com.example.bodhakfrontend.sync.bus.UpdateDispatcher#start()}
     * to schedule the initial timer; may be called again if the policy is hot-swapped.
     */
    long intervalMs();

    /**
     * Returns the maximum number of events to drain from the queue per tick.
     * Implementations may return {@link Integer#MAX_VALUE} to drain all available events.
     */
    int maxEventsPerTick();

    /**
     * Collapses a raw batch of events (already drained from the queue) into the
     * minimal set that should be applied to the UI in this tick.
     *
     * <p>Contract:
     * <ul>
     *   <li>The returned list must be a subset (or reordering) of {@code raw}.</li>
     *   <li>Additive events (e.g., log lines) must be preserved in order.</li>
     *   <li>Last-write-wins events should be deduplicated, keeping only the last.</li>
     * </ul>
     *
     * @param raw the unfiltered batch drained this tick
     * @return the collapsed list to be dispatched to handlers
     */
    List<UiUpdateEvent> collapse(List<UiUpdateEvent> raw);
}
