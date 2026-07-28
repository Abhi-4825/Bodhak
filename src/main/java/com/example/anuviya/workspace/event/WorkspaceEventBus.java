package com.example.anuviya.workspace.event;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class WorkspaceEventBus {
    private static final WorkspaceEventBus INSTANCE = new WorkspaceEventBus();
    private final List<Consumer<WorkspaceEvent>> listeners = new ArrayList<>();

    private WorkspaceEventBus() {}

    public static WorkspaceEventBus getInstance() {
        return INSTANCE;
    }

    public synchronized void register(Consumer<WorkspaceEvent> listener) {
        listeners.add(listener);
    }

    public synchronized void unregister(Consumer<WorkspaceEvent> listener) {
        listeners.remove(listener);
    }

    public synchronized void publish(WorkspaceEvent event) {
        // Copy the list to prevent ConcurrentModificationException if listeners register/unregister inside notification
        List<Consumer<WorkspaceEvent>> targets;
        synchronized (this) {
            targets = new ArrayList<>(listeners);
        }
        for (Consumer<WorkspaceEvent> listener : targets) {
            listener.accept(event);
        }
    }
}
