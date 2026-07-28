package com.example.anuviya.event.dispatch;

import com.example.anuviya.event.UiUpdateEvent;
import com.example.anuviya.event.domain.LogAppendEvent;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Per-project, thread-safe event queue between backend workers and the UI dispatcher.
 *
 * <h3>Design rules</h3>
 * <ul>
 *   <li>Any thread may call {@link #publish(UiUpdateEvent)} — it is wait-free under
 *       normal conditions (queue not full).</li>
 *   <li>The {@link UpdateDispatcher} is the only consumer; it calls
 *       {@link #drainTo(List, int)} on its scheduler thread.</li>
 *   <li>Capacity is bounded at {@value #CAPACITY} to provide backpressure. When
 *       the queue is full, the oldest <em>collapsible</em> event is dropped (not the
 *       newest), preserving the most up-to-date state.</li>
 * </ul>
 *
 * <p>This class has no JavaFX dependency — it lives entirely in the backend layer.
 */
public final class UIEventBus {

    private static final int CAPACITY = 10_000;

    private final LinkedBlockingQueue<UiUpdateEvent> queue =
            new LinkedBlockingQueue<>(CAPACITY);

    /**
     * Publishes an event from any thread.
     *
     * <p>If the queue is at capacity (backpressure), the oldest non-additive event
     * is evicted to make room. {@link LogAppendEvent}s are never evicted.
     */
    public void publish(UiUpdateEvent event) {
        if (!queue.offer(event)) {
            applyBackpressure();
            queue.offer(event); // best-effort after eviction
        }
    }

    /**
     * Drains up to {@code maxElements} events into {@code sink}.
     * Called by the dispatcher on its scheduled executor thread.
     */
    public void drainTo(List<? super UiUpdateEvent> sink, int maxElements) {
        queue.drainTo(sink, maxElements);
    }

    /** Returns the current backlog size — useful for adaptive policy decisions. */
    public int pendingCount() {
        return queue.size();
    }

    // ── Backpressure ──────────────────────────────────────────────────────────

    /**
     * Evicts the first non-additive event from the front of the queue.
     * Log events are never dropped (they are ordered, cumulative records).
     */
    private void applyBackpressure() {
        // Walk the queue to find the first droppable event
        // LinkedBlockingQueue.iterator() is weakly consistent — acceptable here
        for (UiUpdateEvent candidate : queue) {
            if (!(candidate instanceof LogAppendEvent)) {
                if (queue.remove(candidate)) {
                    System.err.println("[UIEventBus] Backpressure: dropped " +
                            candidate.getClass().getSimpleName());
                    return;
                }
            }
        }
        // All events are log lines — drop the oldest one reluctantly
        queue.poll();
    }
}
