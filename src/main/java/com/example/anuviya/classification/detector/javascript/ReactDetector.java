package com.example.anuviya.classification.detector.javascript;

import com.example.anuviya.classification.detection.*;

import static com.example.anuviya.classification.Capability.*;

/**
 * Detects React framework usage.
 *
 * Evidence sources:
 *   - Dependencies: "react" in package.json
 *   - File structure: src/App.jsx, components/ directory
 *   - Config: react-scripts, vite with @vitejs/plugin-react
 */
public final class ReactDetector extends AbstractFrameworkDetector {

    @Override
    public String frameworkName() { return "react"; }

    @Override
    public String languageId() { return "javascript"; }

    @Override
    protected FrameworkDetectionResult doDetect(DetectionContext ctx) {
        var builder = FrameworkDetectionResult.builder(frameworkName());

        // ── 1. Dependency evidence ───────────────────────────────
        if (packageJsonContains(ctx, "\"react\"")) {
            builder.addEvidence(dependencyEvidence(
                    "package.json contains react dependency", 0.40));
            builder.addCapability(WEB_UI);
        }
        if (packageJsonContains(ctx, "\"react-dom\"")) {
            builder.addEvidence(dependencyEvidence(
                    "package.json contains react-dom", 0.15));
            builder.addCapability(WEB_UI);
        }
        if (packageJsonContains(ctx, "\"react-router\"")
                || packageJsonContains(ctx, "\"react-router-dom\"")) {
            builder.addEvidence(dependencyEvidence(
                    "package.json contains react-router", 0.10));
        }

        // ── 2. Build file evidence ──────────────────────────────
        if (packageJsonContains(ctx, "\"react-scripts\"")) {
            builder.addEvidence(buildFileEvidence(
                    "Uses create-react-app (react-scripts)", 0.15));
        }
        if (packageJsonContains(ctx, "@vitejs/plugin-react")) {
            builder.addEvidence(buildFileEvidence(
                    "Uses Vite with React plugin", 0.15));
        }

        // ── 3. File structure evidence ──────────────────────────
        if (ctx.hasFileMatching("*.jsx") || ctx.hasFileMatching("*.tsx")) {
            builder.addEvidence(fileStructureEvidence(
                    "JSX/TSX files found in project", 0.10));
            builder.addCapability(WEB_UI);
        }
        if (ctx.hasDirectory("components")) {
            builder.addEvidence(fileStructureEvidence(
                    "components/ directory found", 0.05));
        }

        // ── 4. State management ─────────────────────────────────
        if (packageJsonContains(ctx, "\"redux\"")
                || packageJsonContains(ctx, "\"@reduxjs/toolkit\"")
                || packageJsonContains(ctx, "\"zustand\"")) {
            builder.addEvidence(dependencyEvidence(
                    "State management library detected", 0.05));
        }

        return builder.build();
    }
}
