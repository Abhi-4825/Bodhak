package com.example.anuviya.analyzer.runtime.engine;

import com.example.anuviya.model.runtime.ExecutionPlan;
import com.example.anuviya.model.runtime.PerformanceReport;
import com.example.anuviya.model.runtime.RuntimeValidationPlan;
import javafx.beans.property.ReadOnlyObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import java.util.Optional;

/**
 * Singleton orchestrator managing lifecycle, active execution session dispatching, and state management.
 */
public class ExecutionEngine {
    private static final ExecutionEngine instance = new ExecutionEngine();
    private final ExecutionProviderResolver resolver = new ExecutionProviderResolver();
    private final SimpleObjectProperty<ExecutionSession> activeSession = new SimpleObjectProperty<>(null);

    private ExecutionEngine() {
    }

    public static ExecutionEngine getInstance() {
        return instance;
    }

    public ExecutionProviderResolver getProviderResolver() {
        return resolver;
    }

    public ReadOnlyObjectProperty<ExecutionSession> activeSessionProperty() {
        return activeSession;
    }

    public ExecutionSession createSession(String providerId, ExecutionPlan plan, RuntimeValidationPlan validationPlan) {
        Optional<ExecutionProvider> providerOpt = resolver.resolve(providerId);
        if (providerOpt.isEmpty()) {
            throw new IllegalArgumentException("Unsupported execution provider: " + providerId);
        }

        ExecutionSession session = new ExecutionSession(plan, validationPlan, providerOpt.get());
        activeSession.set(session);
        return session;
    }

    public PerformanceReport runSession(ExecutionSession session) {
        try {
            return session.run();
        } finally {
            if (activeSession.get() == session) {
                activeSession.set(null);
            }
        }
    }
}
