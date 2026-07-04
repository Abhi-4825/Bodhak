package com.example.bodhak.orchestration.incremental.engine;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ChangeDebouncer {
    private final long debounceDelayMs;
    private final ChangeClassifier classifier;
    private final Consumer<Set<ChangeEvent>> batchProcessor;
    private final Map<Path, ChangeClassifier.WatchEventKind> pendingEvents = new HashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "ChangeDebouncer-Timer");
        t.setDaemon(true);
        return t;
    });
    private ScheduledFuture<?> currentTask = null;

    public ChangeDebouncer(long debounceDelayMs, ChangeClassifier classifier, Consumer<Set<ChangeEvent>> batchProcessor) {
        this.debounceDelayMs = debounceDelayMs;
        this.classifier = classifier;
        this.batchProcessor = batchProcessor;
    }

    public synchronized void onFileEvent(Path path, ChangeClassifier.WatchEventKind kind) {
        pendingEvents.put(path, kind);
        scheduleTask();
    }

    private void scheduleTask() {
        if (currentTask != null && !currentTask.isDone()) {
            currentTask.cancel(false);
        }
        currentTask = scheduler.schedule(this::processBatch, debounceDelayMs, TimeUnit.MILLISECONDS);
    }

    private synchronized void processBatch() {
        if (pendingEvents.isEmpty()) {
            return;
        }
        Map<Path, ChangeClassifier.WatchEventKind> batch = new HashMap<>(pendingEvents);
        pendingEvents.clear();
        
        Set<ChangeEvent> events = new java.util.HashSet<>();
        for (Map.Entry<Path, ChangeClassifier.WatchEventKind> entry : batch.entrySet()) {
            ChangeEvent event = classifier.classify(entry.getKey(), entry.getValue());
            if (event.changeType() != ChangeType.GENERATED_FILE_CHANGE) {
                events.add(event);
            }
        }
        
        if (!events.isEmpty()) {
            try {
                batchProcessor.accept(events);
            } catch (Exception e) {
                System.err.println("Error processing file changes batch: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }
}
