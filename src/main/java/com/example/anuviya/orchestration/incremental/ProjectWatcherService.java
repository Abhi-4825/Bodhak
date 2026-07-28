package com.example.anuviya.orchestration.incremental;

import com.example.anuviya.orchestration.AnalysisEngine;
import com.example.anuviya.event.dispatch.UIEventBus;
import com.example.anuviya.event.domain.*;
import com.example.anuviya.orchestration.incremental.engine.ChangeDebouncer;
import com.example.anuviya.orchestration.incremental.engine.ChangeClassifier;
import com.example.anuviya.orchestration.incremental.engine.ChangeEvent;
import com.example.anuviya.orchestration.incremental.engine.ChangeType;
import com.example.anuviya.model.entity.EntityInfo;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

/**
 * Watches a project root directory for file-system changes and triggers
 * incremental re-analysis in the {@link AnalysisEngine}.
 *
 * <h3>Design rules</h3>
 * <ul>
 *   <li>Runs on a single dedicated daemon thread — never blocks the FX thread.</li>
 *   <li>After each engine update, publishes typed {@link com.example.anuviya.event.UiUpdateEvent}s
 *       to the per-project {@link UIEventBus}. The bus is thread-safe; the events
 *       are delivered to the FX thread by the {@link com.example.anuviya.event.dispatch.UpdateDispatcher}.</li>
 *   <li>Language plugins are completely isolated from this class.</li>
 *   <li>Adding a language never requires touching this class.</li>
 * </ul>
 */
public class ProjectWatcherService {

    private final AnalysisEngine engine;
    private final UIEventBus bus;
    private final WatchService watchService;
    private final Map<WatchKey, Path> keyDirMap = new HashMap<>();
    private ChangeDebouncer debouncer;

    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "FileWatcher-Thread");
        t.setDaemon(true);
        return t;
    });

    private volatile boolean running = false;

    public ProjectWatcherService(AnalysisEngine engine, UIEventBus bus) throws IOException {
        this.engine = engine;
        this.bus    = bus;
        this.watchService = FileSystems.getDefault().newWatchService();
    }

    /**
     * Starts watching the given project root (including all subdirectories).
     * Stops any previously running watch session first.
     */
    public void watch(Path projectRoot) {
        stop();
        keyDirMap.clear();

        ChangeClassifier classifier = engine.getOrchestrator().getClassifier();
        this.debouncer = new ChangeDebouncer(50, classifier, changes -> {
            // Collect removed entities BEFORE cache is updated
            Map<Path, List<EntityInfo>> removedEntitiesMap = new HashMap<>();
            for (ChangeEvent change : changes) {
                if (change.changeType() == ChangeType.DELETED_FILE) {
                    List<EntityInfo> entities = engine.getOrchestrator().getCache().getEntities(change.filePath());
                    if (entities != null) {
                        removedEntitiesMap.put(change.filePath(), new ArrayList<>(entities));
                    }
                }
            }

            // Process changes on incremental compiler
            engine.getOrchestrator().processChanges(changes);

            // Publish UI events
            for (ChangeEvent change : changes) {
                Path file = change.filePath();
                Path containingDir = file.getParent();

                switch (change.changeType()) {
                    case NEW_FILE -> {
                        bus.publish(new FileTreeChangedEvent(containingDir));
                        List<EntityInfo> added = engine.getOrchestrator().getCache().getEntities(file);
                        if (added == null) added = List.of();
                        bus.publish(new EntityListChangedEvent(added, List.of(), List.of()));
                    }
                    case DELETED_FILE -> {
                        bus.publish(new FileTreeChangedEvent(containingDir));
                        bus.publish(new EditorCloseEvent(file));
                        List<EntityInfo> removed = removedEntitiesMap.get(file);
                        if (removed == null) removed = List.of();
                        bus.publish(new EntityListChangedEvent(List.of(), removed, List.of()));
                    }
                    case CONTENT_CHANGE -> {
                        bus.publish(new EditorReloadEvent(file));
                        List<EntityInfo> updated = engine.getOrchestrator().getCache().getEntities(file);
                        if (updated == null) updated = List.of();
                        bus.publish(new EntityListChangedEvent(List.of(), List.of(), updated));
                    }
                    case BUILD_CONFIG_CHANGE, RESOURCE_CHANGE -> {
                        bus.publish(new FileTreeChangedEvent(projectRoot));
                    }
                    default -> {}
                }
            }
        });

        try {
            registerAll(projectRoot);
        } catch (IOException e) {
            System.err.println("[Watcher] Failed to register: " + e.getMessage());
            return;
        }
        running = true;
        executor.submit(this::pollLoop);
        System.out.println("[Watcher] Started watching: " + projectRoot);
    }

    /** Stops the watcher gracefully. */
    public void stop() {
        running = false;
        if (debouncer != null) {
            debouncer.shutdown();
            debouncer = null;
        }
    }

    // ── Internal ──────────────────────────────────────────────────────────────

    private void registerAll(Path start) throws IOException {
        Files.walkFileTree(start, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs)
                     throws IOException {
                String name = dir.getFileName().toString();
                if (name.startsWith(".") || name.equals("target") ||
                        name.equals("build") || name.equals("node_modules")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }
                register(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void register(Path dir) throws IOException {
        WatchKey key = dir.register(watchService, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
        keyDirMap.put(key, dir);
    }

    private void pollLoop() {
        while (running) {
            WatchKey key;
            try {
                key = watchService.take();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }

            Path dir = keyDirMap.get(key);
            if (dir == null) { key.reset(); continue; }

            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == OVERFLOW) continue;

                @SuppressWarnings("unchecked")
                Path name = ((WatchEvent<Path>) event).context();
                Path fullPath = dir.resolve(name).toAbsolutePath().normalize();

                if (Files.isDirectory(fullPath)) {
                    handleDirEvent(kind, fullPath, dir);
                } else {
                    handleFileEvent(kind, fullPath, dir);
                }
            }

            key.reset();
        }
    }

    private void handleDirEvent(WatchEvent.Kind<?> kind, Path fullPath, Path containingDir) {
        if (kind == ENTRY_CREATE) {
            try { registerAll(fullPath); } catch (IOException ignored) {}
            engine.onFolderCreate(fullPath);
        } else if (kind == ENTRY_DELETE) {
            engine.onFolderDelete(fullPath);
        }
        bus.publish(new FileTreeChangedEvent(containingDir));
    }

    private void handleFileEvent(WatchEvent.Kind<?> kind, Path fullPath, Path containingDir) {
        if (debouncer == null) return;
        
        ChangeClassifier.WatchEventKind eventKind = ChangeClassifier.WatchEventKind.MODIFY;
        if (kind == ENTRY_CREATE) {
            eventKind = ChangeClassifier.WatchEventKind.CREATE;
        } else if (kind == ENTRY_DELETE) {
            eventKind = ChangeClassifier.WatchEventKind.DELETE;
        }
        
        debouncer.onFileEvent(fullPath, eventKind);
    }
}
