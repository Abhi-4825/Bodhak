package com.example.bodhakfrontend.core.projectType.detectors.python;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.projectType.detection.*;

import static com.example.bodhakfrontend.core.projectType.Capability.*;

import java.util.List;

/**
 * Detects FastAPI framework usage.
 *
 * Evidence sources:
 *   - Dependencies: "fastapi" in requirements.txt / pyproject.toml
 *   - AST: @app.get, @app.post decorators
 *   - Tags: FastAPI-related EntityContribution tags
 *   - Dependencies: "uvicorn" (ASGI server)
 */
public final class FastAPIDetector extends AbstractFrameworkDetector {

    @Override
    public String frameworkName() { return "fastapi"; }

    @Override
    public String languageId() { return "python"; }

    @Override
    protected FrameworkDetectionResult doDetect(DetectionContext ctx) {
        var builder = FrameworkDetectionResult.builder(frameworkName());

        // ── 1. Dependency evidence ───────────────────────────────
        if (requirementsContains(ctx, "fastapi")) {
            builder.addEvidence(dependencyEvidence(
                    "fastapi found in Python dependencies", 0.40));
            builder.addCapability(HTTP_ENDPOINT);
            builder.addCapability(DEPENDENCY_INJECTION);
        }
        if (requirementsContains(ctx, "uvicorn")) {
            builder.addEvidence(dependencyEvidence(
                    "uvicorn ASGI server in dependencies", 0.10));
        }
        if (requirementsContains(ctx, "sqlalchemy")
                || requirementsContains(ctx, "tortoise-orm")) {
            builder.addEvidence(dependencyEvidence(
                    "ORM library in Python dependencies", 0.10));
            builder.addCapability(DATABASE_ACCESS);
            builder.addCapability(ORM);
        }

        // ── 2. AST evidence ─────────────────────────────────────
        List<EntityInfo> routeEntities = entities(ctx).stream()
                .filter(e -> e.getDecorators().stream()
                        .anyMatch(d -> d.contains("app.get") || d.contains("app.post")
                                || d.contains("app.put") || d.contains("app.delete")
                                || d.contains("router.get") || d.contains("router.post")))
                .toList();

        if (!routeEntities.isEmpty()) {
            builder.addEvidence(astAnnotationEvidence(
                    "FastAPI route decorators on " + routeEntities.size() + " entities",
                    0.25,
                    routeEntities.getFirst().getEntityName()));
            builder.addCapability(HTTP_ENDPOINT);
        }

        // ── 3. Configuration evidence ───────────────────────────
        if (ctx.hasFile("alembic.ini")) {
            builder.addEvidence(configEvidence(
                    "Alembic migration config found", 0.05));
            builder.addCapability(DATABASE_ACCESS);
        }

        // ── 4. File structure evidence ──────────────────────────
        if (ctx.hasDirectory("routers") || ctx.hasDirectory("routes")) {
            builder.addEvidence(fileStructureEvidence(
                    "routers/ or routes/ directory found", 0.05));
        }

        // ── 5. Websocket support ────────────────────────────────
        boolean hasWebsocket = entities(ctx).stream()
                .anyMatch(e -> e.getDecorators().stream()
                        .anyMatch(d -> d.contains("websocket")));
        if (hasWebsocket) {
            builder.addEvidence(astAnnotationEvidence(
                    "WebSocket decorators found", 0.05, null));
            builder.addCapability(WEBSOCKET);
        }

        return builder.build();
    }
}
