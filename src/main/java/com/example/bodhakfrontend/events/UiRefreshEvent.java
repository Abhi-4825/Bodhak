package com.example.bodhakfrontend.events;

import java.nio.file.Path;

public record UiRefreshEvent(Path path, UiRefreshType type) {
}
