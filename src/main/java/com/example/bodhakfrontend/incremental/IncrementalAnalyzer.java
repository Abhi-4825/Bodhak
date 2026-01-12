package com.example.bodhakfrontend.incremental;

import com.example.bodhakfrontend.Builder.ClassDependencyInfoBuilder;
import com.example.bodhakfrontend.Builder.ProjectAnalysisResultBuilder;
import com.example.bodhakfrontend.Models.ClassDependencyInfo;
import com.example.bodhakfrontend.Models.Incremental.FileChangeEvent;
import com.example.bodhakfrontend.Models.Incremental.UiRefreshEvent;
import com.example.bodhakfrontend.projectAnalysis.FileAnalyzer;
import com.example.bodhakfrontend.util.ParseCache;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class IncrementalAnalyzer {
    private final ProjectAnalysisResultBuilder projectAnalysisResultBuilder;
    private final ClassDependencyInfo dependencyInfo;
    private final ClassDependencyInfoBuilder  classDependencyInfoBuilder;
    private final ParseCache parseCache;
    private final Set<String> sourceClasses;
    private final EventBus bus;
    private final FileAnalyzer fileAnalyzer;


    public IncrementalAnalyzer(
            EventBus bus,
            ClassDependencyInfo dependencyInfo, ClassDependencyInfoBuilder classDependencyInfoBuilder,
            ParseCache parseCache,

            Set<String> sourceClasses, ProjectAnalysisResultBuilder projectAnalysisResultBuilder,

            FileAnalyzer fileAnalyzer
    ) {
        this.dependencyInfo = dependencyInfo;
        this.classDependencyInfoBuilder = classDependencyInfoBuilder;
        this.parseCache = parseCache;
        this.sourceClasses = sourceClasses;
        this.bus = bus;
        this.projectAnalysisResultBuilder = projectAnalysisResultBuilder;
        this.fileAnalyzer = fileAnalyzer;
        bus.subscribe(FileChangeEvent.class, this::handle);
    }
    private void handle(FileChangeEvent event) {



        switch (event.type()) {

            case  FILE_CREATED-> {
                handleFileCreate(event);
            }

            case FILE_MODIFIED -> {
                      handleFileModify(event);
            }

            case FILE_DELETED -> {

                handleFileDelete(event);
            }
            case DIR_CREATED -> {handleDirCreate(event);}
            case DIR_DELETED -> {handleDirDelete(event);}
        }

    }
    private void handleFileModify(FileChangeEvent event) {
        Path path = event.path();

        bus.publish(
                new UiRefreshEvent(
                        UiRefreshEvent.UiRefreshType.REFRESH_EDITOR,
                        path
                )
        );

        scheduleSemanticUpdate(path);
    }

    private void handleDirCreate(FileChangeEvent event) {
        Path path=event.path();
        projectAnalysisResultBuilder.onDirCreate(path);
        publishFileTreeRefresh(path.getParent());
        publishAnalysisRefresh(path);
    }
    private void handleDirDelete(FileChangeEvent event) {
        Path path=event.path();
        projectAnalysisResultBuilder.onDirDelete(path);
        publishFileTreeRefresh(path.getParent());
        publishAnalysisRefresh(path);
    }




    private void handleFileCreate(FileChangeEvent event) {
        Path path=event.path();
        classDependencyInfoBuilder.onFileCreated(path,sourceClasses);
        projectAnalysisResultBuilder.onFileCreate(path,fileAnalyzer);
        publishFileTreeRefresh(path.getParent());
        publishAnalysisRefresh(path);

    }

    private void handleFileDelete(FileChangeEvent event) {
        Path path=event.path();
        classDependencyInfoBuilder.onFileDeleted(path);
        projectAnalysisResultBuilder.onFileDelete(path);
        bus.publish(new UiRefreshEvent(UiRefreshEvent.UiRefreshType.CLOSE_EDITOR, path));
        publishFileTreeRefresh(path.getParent());
        publishAnalysisRefresh(path);


    }
    private void publishFileTreeRefresh(Path path){
        bus.publish(new UiRefreshEvent(UiRefreshEvent.UiRefreshType.FILE_TREE,path));

    }
    private void publishAnalysisRefresh(Path path){
        bus.publish(new UiRefreshEvent(UiRefreshEvent.UiRefreshType.REFRESH_ANALYSIS,path));
    }



    private final Map<Path, Long> pending = new ConcurrentHashMap<>();
    private static final long STABLE_DELAY_MS = 300;

    private void scheduleSemanticUpdate(Path path) {
        long now = System.currentTimeMillis();
        pending.put(path, now);

        new Thread(() -> {
            try {
                Thread.sleep(STABLE_DELAY_MS);
            } catch (InterruptedException ignored) {}

            Long last = pending.get(path);
            if (last != null && last == now) {
                pending.remove(path);

                // 🔥 NOW the file is stable
                parseCache.invalidate(path);
                parseCache.get(path);
                classDependencyInfoBuilder.onFileModified(path,sourceClasses);


                projectAnalysisResultBuilder.onFileChange(path, fileAnalyzer);
                publishAnalysisRefresh(path);
            }
        }, "EntryPoint-Debounce").start();
    }


}


