package com.example.anuviya.platform.installation;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class InstallContext {
    private final Map<String, Object> metadata = new HashMap<>();
    private final Consumer<Double> progressConsumer;
    private String errorMessage;

    public InstallContext(Consumer<Double> progressConsumer) {
        this.progressConsumer = progressConsumer;
    }

    public void put(String key, Object value) {
        metadata.put(key, value);
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T) metadata.get(key);
    }

    public void updateProgress(double progress) {
        if (progressConsumer != null) {
            progressConsumer.accept(progress);
        }
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
