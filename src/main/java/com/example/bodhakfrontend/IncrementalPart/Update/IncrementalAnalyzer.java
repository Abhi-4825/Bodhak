package com.example.bodhakfrontend.IncrementalPart.Update;
import com.example.bodhakfrontend.IncrementalPart.UpdateManager;
import java.nio.file.Path;
public class IncrementalAnalyzer {
    private final EventBus bus;
    private final UpdateManager  updateManager;
    public IncrementalAnalyzer(
            EventBus bus, UpdateManager updateManager
    ) {
        this.bus = bus;
        this.updateManager = updateManager;
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
        updateManager.onFileUpdate(path);
        publishProjectInfoChange(path);
    }

    private void handleDirCreate(FileChangeEvent event) {
        Path path=event.path();

        publishFileTreeRefresh(path.getParent());
        updateManager.onFolderCreate(path);
        publishProjectInfoChange(path);

    }
    private void handleDirDelete(FileChangeEvent event) {
        Path path=event.path();

        publishFileTreeRefresh(path.getParent());
        updateManager.onFolderDelete(path);
        publishProjectInfoChange(path);

    }
    private void handleFileCreate(FileChangeEvent event) {
        publishFileTreeRefresh(event.path().getParent());
        updateManager.onFileCreate(event.path());
        publishProjectInfoChange(event.path());


    }
    private void handleFileDelete(FileChangeEvent event) {
        Path path=event.path();
        bus.publish(new UiRefreshEvent(UiRefreshEvent.UiRefreshType.CLOSE_EDITOR, path));
        publishFileTreeRefresh(path.getParent());
        updateManager.onFileDelete(path);
        publishProjectInfoChange(path);
        bus.publish(new UiRefreshEvent(UiRefreshEvent.UiRefreshType.OVERVIEWTAB_CLOSED, event.path()));
    }

    private void publishFileTreeRefresh(Path path){
        bus.publish(new UiRefreshEvent(UiRefreshEvent.UiRefreshType.FILE_TREE,path));

    }

    private void publishProjectInfoChange(Path path){
        bus.publish(new UiRefreshEvent(UiRefreshEvent.UiRefreshType.PROJECTINFO_CHANGED,path));
    }

}