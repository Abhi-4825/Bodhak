package com.example.anuviya.platform.environment;

import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

public class SystemEnvironmentManager {
    private static final SystemEnvironmentManager INSTANCE = new SystemEnvironmentManager();

    private final NetworkService networkService;
    private final EnvironmentScanner scanner;
    private final SimpleObjectProperty<EnvironmentSnapshot> snapshotProperty = new SimpleObjectProperty<>();

    private SystemEnvironmentManager() {
        this.networkService = new DefaultNetworkService();
        this.scanner = new EnvironmentScanner(this.networkService);
        
        // Refresh environment scanner snapshot when network service changes
        this.networkService.statusProperty().addListener((obs, oldVal, newVal) -> {
            refresh();
        });
    }

    public static SystemEnvironmentManager getInstance() {
        return INSTANCE;
    }

    public void startMonitoring() {
        networkService.startMonitoring();
        refresh();
    }

    public void stopMonitoring() {
        networkService.stopMonitoring();
    }

    public synchronized void refresh() {
        EnvironmentSnapshot newSnapshot = scanner.scan();
        if (Platform.isFxApplicationThread()) {
            snapshotProperty.set(newSnapshot);
        } else {
            Platform.runLater(() -> snapshotProperty.set(newSnapshot));
        }
    }

    public NetworkService getNetworkService() {
        return networkService;
    }

    public ReadOnlyObjectProperty<EnvironmentSnapshot> snapshotProperty() {
        return snapshotProperty;
    }

    public EnvironmentSnapshot getSnapshot() {
        return snapshotProperty.get();
    }
}
