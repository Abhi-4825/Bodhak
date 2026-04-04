package com.example.bodhakfrontend.Backend.IncrementalPart.Update;

import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;

public class ProjectFileWatcher {
    private FileAlterationMonitor monitor;
    public void start(File projectRoot, ProjectFileListener listener) throws Exception{
        FileAlterationObserver observer=new FileAlterationObserver(projectRoot);
        observer.addListener(listener);

        monitor=new FileAlterationMonitor(1500);
        monitor.addObserver(observer);
        monitor.start();
    }
    public void stop() throws Exception{
        if(monitor!=null){
            monitor.stop();
        }
    }


}
