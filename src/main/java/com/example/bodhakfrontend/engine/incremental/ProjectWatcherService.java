package com.example.bodhakfrontend.engine.incremental;

import com.example.bodhakfrontend.engine.AnalysisEngine;
import com.example.bodhakfrontend.sync.bus.UIEventBus;
import com.example.bodhakfrontend.sync.events.*;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
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
 *   <li>After each engine update, publishes typed {@link com.example.bodhakfrontend.sync.api.UiUpdateEvent}s
 *       to the per-project {@link UIEventBus}. The bus is thread-safe; the events
 *       are delivered to the FX thread by the {@link com.example.bodhakfrontend.sync.bus.UpdateDispatcher}.</li>
 *   <li>Language plugins are completely isolated from this class.</li>
 *   <li>Adding a language never requires touching this class.</li>
 * </ul>
 */
public class ProjectWatcherService {

    private final AnalysisEngine engine;
    private final UIEventBus bus;
    private final WatchService watchService;
    private final Map<WatchKey, Path> keyDirMap = new HashMap<>();

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
        if (kind == ENTRY_CREATE) {
            engine.onFileCreate(fullPath);
            bus.publish(new FileTreeChangedEvent(containingDir));
            bus.publish(new ProjectSummaryChangedEvent(engine.getProjectInfo()));
            bus.publish(new EntityListChangedEvent(
                    engine.getEntitiesForFile(fullPath), java.util.List.of(), java.util.List.of()
            ));

        } else if (kind == ENTRY_MODIFY) {
            var before = engine.getEntitiesForFile(fullPath);
            engine.onFileModify(fullPath);
            var after  = engine.getEntitiesForFile(fullPath);
            bus.publish(new EditorReloadEvent(fullPath));
            bus.publish(new EntityListChangedEvent(
                    java.util.List.of(), java.util.List.of(), after
            ));
            bus.publish(new ProjectSummaryChangedEvent(engine.getProjectInfo()));
            bus.publish(new DependencyGraphChangedEvent(engine.getGraphSnapshot()));

        } else if (kind == ENTRY_DELETE) {
            var removed = engine.getEntitiesForFile(fullPath);
            engine.onFileDelete(fullPath);
            bus.publish(new FileTreeChangedEvent(containingDir));
            bus.publish(new EditorCloseEvent(fullPath));
            bus.publish(new EntityListChangedEvent(
                    java.util.List.of(), removed, java.util.List.of()
            ));
            bus.publish(new ProjectSummaryChangedEvent(engine.getProjectInfo()));
        }
    }
}
