package com.example.bodhakfrontend.core.plugin;

import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;

import java.util.List;

/**
 * Detects entry points within a language's entities.
 * Each language plugin provides its own implementation.
 *
 * Java   → finds main(), Spring @SpringBootApplication, @Test, extends Application
 * Python → finds __main__ guard, manage.py, app.run(), pytest functions
 */
public interface EntryPointDetector {
    EntryPointInfo detect(List<EntityInfo> entities);
}
