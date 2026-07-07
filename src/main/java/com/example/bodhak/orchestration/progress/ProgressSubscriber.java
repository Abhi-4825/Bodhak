package com.example.bodhak.orchestration.progress;

public interface ProgressSubscriber {
    void onProgress(AnalysisProgressEvent event);
}
