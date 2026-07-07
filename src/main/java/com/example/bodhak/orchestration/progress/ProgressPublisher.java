package com.example.bodhak.orchestration.progress;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ProgressPublisher {
    private static final List<ProgressSubscriber> subscribers = new CopyOnWriteArrayList<>();

    private ProgressPublisher() {}

    public static void subscribe(ProgressSubscriber subscriber) {
        if (!subscribers.contains(subscriber)) {
            subscribers.add(subscriber);
        }
    }

    public static void unsubscribe(ProgressSubscriber subscriber) {
        subscribers.remove(subscriber);
    }

    public static void publish(AnalysisProgressEvent event) {
        for (ProgressSubscriber sub : subscribers) {
            try {
                sub.onProgress(event);
            } catch (Exception e) {
                System.err.println("[ProgressPublisher] Error dispatching event: " + e.getMessage());
            }
        }
    }
}
