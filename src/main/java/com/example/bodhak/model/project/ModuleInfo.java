package com.example.bodhak.model.project;

import java.nio.file.Path;

public record ModuleInfo(
    String name,
    Path path
) {}
