package com.example.anuviya.event.domain;

import java.nio.file.Path;

public record UiRefreshEvent(Path path, UiRefreshType type) {
}
