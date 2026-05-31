package com.example.bodhakfrontend.core.plugin;

import java.nio.file.Path;

public interface Parser<T> {

    T parse(Path filePath);

    void invalidate(Path filePath);
}
