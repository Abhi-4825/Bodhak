package com.example.anuviya.platform.environment;

import com.example.anuviya.platform.environment.model.NetworkStatus;
import javafx.beans.property.ReadOnlyObjectProperty;

public interface NetworkService {
    NetworkStatus status();
    boolean isConnected();
    boolean canReach(String endpoint);
    void refresh();
    void startMonitoring();
    void stopMonitoring();
    ReadOnlyObjectProperty<NetworkStatus> statusProperty();
}
