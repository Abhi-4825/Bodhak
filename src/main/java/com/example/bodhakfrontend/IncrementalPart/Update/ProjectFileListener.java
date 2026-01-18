package com.example.bodhakfrontend.IncrementalPart.Update;

import com.example.bodhakfrontend.util.IgnoreRules;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;


import java.io.File;

public class ProjectFileListener extends FileAlterationListenerAdaptor {
    EventBus bus;
    public ProjectFileListener(EventBus bus) {
        this.bus = bus;
    }
    public void onFileCreate(File file) {
        if(IgnoreRules.shouldIgnore(file)) {return;}
        bus.publish(new FileChangeEvent(FileChangeEvent.ChangeType.FILE_CREATED,file.toPath()));
        System.out.println("File created: " + file.getAbsolutePath());
    }
    public void onFileChange(File file) {
        if(IgnoreRules.shouldIgnore(file)) {return;}
        bus.publish(new FileChangeEvent(FileChangeEvent.ChangeType.FILE_MODIFIED,file.toPath()));

        System.out.println("File changed: " + file.getAbsolutePath());
    }
    public void onFileDelete(File file) {
        if(IgnoreRules.shouldIgnore(file)) {return;}
        bus.publish(new FileChangeEvent(FileChangeEvent.ChangeType.FILE_DELETED,file.toPath()));

        System.out.println("File deleted: " + file.getAbsolutePath());
    }
//    public void onDirectoryChange(File directory) {
//        if(IgnoreRules.shouldIgnore(directory)) {return;}
//        bus.publish(new FileChangeEvent(FileChangeEvent.ChangeType.DIR_MODIFIED,directory.toPath().toAbsolutePath()));
//
//        System.out.println("Directory changed: " + directory.getAbsolutePath());
//    }

    @Override
    public void onDirectoryCreate(File directory) {
        if(IgnoreRules.shouldIgnore(directory)) {return;}
        bus.publish(new FileChangeEvent(FileChangeEvent.ChangeType.DIR_CREATED,directory.toPath()));

        System.out.println("Directory created: " + directory.getAbsolutePath());
    }

    @Override
    public void onDirectoryDelete(File directory) {
        if(IgnoreRules.shouldIgnore(directory)) {return;}
        bus.publish(new FileChangeEvent(FileChangeEvent.ChangeType.DIR_DELETED,directory.toPath()));

        System.out.println("Directory deleted: " + directory.getAbsolutePath());
    }
}
