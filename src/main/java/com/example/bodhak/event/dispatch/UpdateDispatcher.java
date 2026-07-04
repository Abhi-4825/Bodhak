package com.example.bodhak.event.dispatch;

import com.example.bodhak.event.dispatch.DispatchPolicy;
import com.example.bodhak.event.dispatch.DispatchProfile;
import com.example.bodhak.event.UiUpdateEvent;
import com.example.bodhak.event.domain.LogAppendEvent;
import com.example.bodhak.event.handler.UiUpdateHandler;
import javafx.application.Platform;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * The central bridge between the backend event bus and the JavaFX UI thread.
 *
 * <h3>Responsibilities</h3>
 * <ol>
 *   <li>Drains {@link UIEventBus} at the rate defined by the active {@link DispatchPolicy}.</li>
 *   <li>Collapses redundant events (last-write-wins) to minimize re-renders.</li>
 *   <li>Dispatches the collapsed batch to all registered {@link UiUpdateHandler}s
 *       exclusively via {@link Platform#runLater(Runnable)} — the only place in the
 *       entire codebase where {@code runLater} is called for update events.</li>
 * </ol>
 *
 * <h3>Lifecycle</h3>
 * Call {@link #start()} once after project load, {@link #stop()} on project close / app exit.
 * The policy can be hot-swapped at runtime via {@link #setPolicy(DispatchPolicy)}.
 *
 * <h3>Threading</h3>
 * <ul>
 *   <li>The drain loop runs on a single daemon {@link ScheduledExecutorService} thread.</li>
 *   <li>All handler invocations happen on the JavaFX Application Thread.</li>
 *   <li>No JavaFX observable is ever touched from the drain thread.</li>
 * </ul>
 */
public final class UpdateDispatcher {

    private final UIEventBus bus;
    private final List<UiUpdateHandler> handlers;

    private volatile DispatchPolicy policy;
    private final AtomicBoolean running = new AtomicBoolean(false);

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "Bodhak-UIDispatcher");
        t.setDaemon(true);
        return t;
    });

    private ScheduledFuture<?> currentTask;

    public UpdateDispatcher(UIEventBus bus, DispatchPolicy policy, List<UiUpdateHandler> handlers) {
        this.bus = bus;
        this.policy = policy;
        this.handlers = Collections.unmodifiableList(new ArrayList<>(handlers));
    }

    // ── Lifecycle ─────────────────────────────────────────────────────────────

    /** Starts the dispatch loop. Safe to call multiple times (idempotent). */
    public synchronized void start() {
        if (running.getAndSet(true)) return;
        scheduleWithCurrentPolicy();
    }

    /** Stops the dispatch loop and performs a final drain. */
    public synchronized void stop() {
        if (!running.getAndSet(false)) return;
        if (currentTask != null) currentTask.cancel(false);
        // Final drain on the caller's thread (usually app shutdown)
        drainAndDispatch();
    }

    /**
     * Hot-swaps the dispatch policy at runtime.
     * The new interval takes effect on the next scheduled tick.
     */
    public synchronized void setPolicy(DispatchPolicy newPolicy) {
        this.policy = newPolicy;
        if (running.get()) {
            if (currentTask != null) currentTask.cancel(false);
            scheduleWithCurrentPolicy();
        }
    }

    /** Convenience overload using a named profile. */
    public synchronized void setProfile(DispatchProfile profile) {
        setPolicy(new FixedIntervalPolicy(profile));
    }

    // ── Internal drain loop ───────────────────────────────────────────────────

    private void scheduleWithCurrentPolicy() {
        long interval = policy.intervalMs();
        currentTask = scheduler.scheduleAtFixedRate(
                this::drainAndDispatch, 0, interval, TimeUnit.MILLISECONDS
        );
    }

    private void drainAndDispatch() {
        List<UiUpdateEvent> raw = new ArrayList<>();
        bus.drainTo(raw, policy.maxEventsPerTick());

        if (raw.isEmpty()) return;

        List<UiUpdateEvent> collapsed = policy.collapse(raw);

        // THE only Platform.runLater in the entire update pipeline
        Platform.runLater(() -> {
            for (UiUpdateEvent event : collapsed) {
                for (UiUpdateHandler handler : handlers) {
                    try {
                        if (handler.canHandle(event)) {
                            handler.apply(event);
                        }
                    } catch (Exception ex) {
                        System.err.println("[UpdateDispatcher] Handler error in "
                                + handler.getClass().getSimpleName() + ": " + ex.getMessage());
                    }
                }
            }
        });
    }
}
