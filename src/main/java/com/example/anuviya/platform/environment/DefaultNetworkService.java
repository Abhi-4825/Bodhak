package com.example.anuviya.platform.environment;

import com.example.anuviya.platform.environment.model.NetworkStatus;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class DefaultNetworkService implements NetworkService {
    private final SimpleObjectProperty<NetworkStatus> statusProperty = new SimpleObjectProperty<>(NetworkStatus.UNKNOWN);
    private ScheduledExecutorService scheduler;

    @Override
    public NetworkStatus status() {
        return statusProperty.get();
    }

    @Override
    public boolean isConnected() {
        return status() == NetworkStatus.ONLINE || status() == NetworkStatus.LIMITED;
    }

    @Override
    public boolean canReach(String endpoint) {
        // endpoint can be a host or host:port
        try {
            String host = endpoint;
            int port = 80;
            if (endpoint.contains(":")) {
                String[] parts = endpoint.split(":");
                host = parts[0];
                port = Integer.parseInt(parts[1]);
            }
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress(host, port), 2000);
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void refresh() {
        boolean connected = false;
        // Try connecting to github.com first
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress("github.com", 80), 2000);
            connected = true;
        } catch (Exception e) {
            // Fallback to DNS root
            try (Socket socket = new Socket()) {
                socket.connect(new InetSocketAddress("8.8.8.8", 53), 2000);
                connected = true;
            } catch (Exception ex) {
                // both offline
            }
        }
        final NetworkStatus newStatus = connected ? NetworkStatus.ONLINE : NetworkStatus.OFFLINE;
        if (statusProperty.get() != newStatus) {
            Platform.runLater(() -> statusProperty.set(newStatus));
        }
    }

    @Override
    public void startMonitoring() {
        if (scheduler != null) {
            return;
        }
        scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "bodhak-network-monitor");
            thread.setDaemon(true);
            return thread;
        });
        scheduler.scheduleWithFixedDelay(this::refresh, 0, 30, TimeUnit.SECONDS);
    }

    @Override
    public void stopMonitoring() {
        if (scheduler != null) {
            scheduler.shutdownNow();
            scheduler = null;
        }
    }

    @Override
    public ReadOnlyObjectProperty<NetworkStatus> statusProperty() {
        return statusProperty;
    }
}
