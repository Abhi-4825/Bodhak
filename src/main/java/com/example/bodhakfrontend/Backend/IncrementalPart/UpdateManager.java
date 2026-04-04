package com.example.bodhakfrontend.Backend.IncrementalPart;


import com.example.bodhakfrontend.Backend.Analysis.Engine.AnalysisEngine;
import com.example.bodhakfrontend.Backend.ClassGraphBuilder;
import com.example.bodhakfrontend.Backend.languages.JavaLanguage.Builder.*;
import com.example.bodhakfrontend.Backend.models.Class.ClassInfo;

import java.nio.file.Path;
import java.util.List;
public class UpdateManager {
    private final AnalysisEngine  analysisEngine;

    public UpdateManager(AnalysisEngine analysisEngine) {
        this.analysisEngine = analysisEngine;
    }



    // OnFile Create
    public void onFileCreate(Path path) {
        analysisEngine.onFileCreate(path);
    }
    public void onFileDelete(Path path) {
       analysisEngine.onFileDelete(path);
    }
    public void onFileUpdate(Path path) {
      analysisEngine.onFileModify(path);
    }

    public void onFolderCreate(Path path) {
        analysisEngine.onFolderCreate(path);
    }
    public void onFolderDelete(Path path) {
        analysisEngine.onFolderDelete(path);
    }


   }





