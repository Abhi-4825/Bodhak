package com.example.bodhakfrontend.sync.bus;

import com.example.bodhakfrontend.sync.api.DispatchPolicy;
import com.example.bodhakfrontend.sync.api.DispatchProfile;
import com.example.bodhakfrontend.sync.api.UiUpdateEvent;
import com.example.bodhakfrontend.sync.events.LogAppendEvent;

import java.util.*;

/**
 * A simple fixed-interval {@link DispatchPolicy} that drains and collapses events
 * at a constant tick rate derived from a {@link DispatchProfile}.
 *
 * <h3>Collapse rules</h3>
 * <ul>
 *   <li><strong>Last-write-wins:</strong> for all event types except
 *       {@link LogAppendEvent}, only the most recent occurrence of each class is
 *       dispatched. This prevents N consecutive saves triggering N full UI redraws.</li>
 *   <li><strong>Additive (LogAppendEvent):</strong> every instance is preserved
 *       in arrival order — no log lines are silently dropped.</li>
 *   <li><strong>Ordering:</strong> last-write-wins events are dispatched in the order
 *       their class was first seen in the batch; log events follow at the end.</li>
 * </ul>
 */
public final class FixedIntervalPolicy implements DispatchPolicy {

    private static final int MAX_EVENTS_PER_TICK = 500;

    private final long intervalMs;

    public FixedIntervalPolicy(DispatchProfile profile) {
        this.intervalMs = profile.intervalMs();
    }

    /** Custom interval in milliseconds (for fine-grained tuning without adding a new profile). */
    public FixedIntervalPolicy(long intervalMs) {
        this.intervalMs = intervalMs;
    }

    @Override
    public long intervalMs() {
        return intervalMs;
    }

    @Override
    public int maxEventsPerTick() {
        return MAX_EVENTS_PER_TICK;
    }

    /**
     * Collapses the batch:
     * <ol>
     *   <li>Walk the list in arrival order.</li>
     *   <li>For non-additive events, record the last seen instance per class.</li>
     *   <li>For {@link LogAppendEvent}, always preserve in order.</li>
     *   <li>Return: non-additive events (insertion-ordered by first-seen class) + log events.</li>
     * </ol>
     */
    @Override
    public List<UiUpdateEvent> collapse(List<UiUpdateEvent> raw) {
        // LinkedHashMap preserves first-seen class order while overwriting with last value
        Map<Class<? extends UiUpdateEvent>, UiUpdateEvent> lastWins = new LinkedHashMap<>();
        List<LogAppendEvent> logs = new ArrayList<>();

        for (UiUpdateEvent event : raw) {
            if (event instanceof LogAppendEvent log) {
                logs.add(log);
            } else {
                // Overwrite: keeps the last occurrence, but insertion order of the key
                // is determined by first occurrence (LinkedHashMap semantics)
                lastWins.put(event.getClass(), event);
            }
        }

        List<UiUpdateEvent> result = new ArrayList<>(lastWins.values());
        result.addAll(logs);
        return result;
    }
}
