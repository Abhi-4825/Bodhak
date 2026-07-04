package com.example.bodhak.classification;

import com.example.bodhak.classification.detection.DetectionContext;
import com.example.bodhak.classification.detection.FrameworkDetectionResult;
import com.example.bodhak.classification.detector.javascript.ReactDetector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static com.example.bodhak.classification.Capability.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for ReactDetector.
 */
class ReactDetectorTest {

    private final ReactDetector detector = new ReactDetector();
    private TestContextBuilder builder;

    @AfterEach
    void cleanup() {
        if (builder != null) builder.cleanup();
    }

    @Test
    void frameworkName_isReact() {
        assertEquals("react", detector.frameworkName());
    }

    @Test
    void languageId_isJavascript() {
        assertEquals("javascript", detector.languageId());
    }

    // ── Dependency evidence ───────────────────────────────────────────────────

    @Test
    void packageJsonWithReact_detected_withWebUi() {
        builder = new TestContextBuilder()
                .withPackageJson("{\"dependencies\":{\"react\":\"^18.0.0\",\"react-dom\":\"^18.0.0\"}}");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        // react=0.40 + react-dom=0.15 = 0.55 → detected
        assertTrue(result.isDetected());
        assertTrue(result.capabilities().contains(WEB_UI));
    }

    @Test
    void packageJsonWithOnlyReact_stillDetected() {
        builder = new TestContextBuilder()
                .withPackageJson("{\"dependencies\":{\"react\":\"^18.0.0\"}}");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        // react=0.40 ≥ threshold 0.30
        assertTrue(result.isDetected());
    }

    @Test
    void packageJsonWithReactScripts_cra_buildFileEvidence() {
        builder = new TestContextBuilder()
                .withPackageJson("{\"dependencies\":{\"react\":\"^18.0.0\"}," +
                                  "\"devDependencies\":{\"react-scripts\":\"5.0.0\"}}");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        // react=0.40 + react-scripts=0.15 = 0.55
        assertTrue(result.score() >= 0.54);
    }

    @Test
    void packageJsonWithViteReact_buildFileEvidence() {
        builder = new TestContextBuilder()
                .withPackageJson("{\"dependencies\":{\"react\":\"^18.0.0\"}," +
                                  "\"devDependencies\":{\"@vitejs/plugin-react\":\"^4.0.0\"}}");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.isDetected());
    }

    // ── File structure evidence ───────────────────────────────────────────────

    @Test
    void jsxFilePresent_addsWebUiAndEvidence() {
        builder = new TestContextBuilder()
                .withPackageJson("{\"dependencies\":{\"react\":\"^18.0.0\"}}")
                .withFile("App.jsx", "export default function App() { return <div>Hello</div>; }");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.capabilities().contains(WEB_UI));
        // File structure evidence should also be added
        assertTrue(result.score() > 0.40,
                "JSX file evidence should push score above react-only baseline");
    }

    @Test
    void componentsDirectory_addsEvidence() {
        builder = new TestContextBuilder()
                .withPackageJson("{\"dependencies\":{\"react\":\"^18.0.0\"}}")
                .withDirectory("components");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertTrue(result.isDetected());
        assertTrue(result.score() > 0.40);
    }

    // ── No React signals ──────────────────────────────────────────────────────

    @Test
    void nonReactPackageJson_notDetected() {
        builder = new TestContextBuilder()
                .withPackageJson("{\"dependencies\":{\"lodash\":\"^4.17.0\",\"axios\":\"^1.0.0\"}}");
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertFalse(result.isDetected());
    }

    @Test
    void emptyProject_notDetected() {
        builder = new TestContextBuilder();
        DetectionContext ctx = builder.buildDetectionContext();

        FrameworkDetectionResult result = detector.detect(ctx);

        assertFalse(result.isDetected());
        assertEquals(0.0, result.score());
    }
}
