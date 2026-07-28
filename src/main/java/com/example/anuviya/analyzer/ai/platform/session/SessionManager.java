package com.example.anuviya.analyzer.ai.platform.session;

import com.example.anuviya.analyzer.ai.platform.AIException;
import com.example.anuviya.analyzer.ai.platform.model.AIProfileDef;
import com.example.anuviya.analyzer.ai.platform.model.ModelInfo;
import com.example.anuviya.analyzer.ai.platform.prompt.PromptPackage;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;

import java.util.*;

public class SessionManager {
    private static final SessionManager INSTANCE = new SessionManager();
    private final Map<String, AISession> activeSessions = new HashMap<>();

    private SessionManager() {}

    public static SessionManager getInstance() {
        return INSTANCE;
    }

    public synchronized AISession open(AIProfileDef profile, AIProvider provider, ModelInfo model) {
        String sessionId = UUID.randomUUID().toString();
        AISession session = new AISession(sessionId, provider, model, profile);
        session.setState(AISessionState.READY);
        activeSessions.put(sessionId, session);
        return session;
    }

    public void warmup(AISession session) throws AIException {
        if (session.getState() == AISessionState.CLOSED) {
            throw new AIException("Cannot warmup closed session.");
        }
        
        session.setState(AISessionState.RUNNING);
        try {
            PromptPackage warmupPrompt = new PromptPackage("You are a warm-up service.", "ping", null, null);
            session.getProvider().generate(session, warmupPrompt);
            session.setState(AISessionState.READY);
        } catch (Exception e) {
            session.setState(AISessionState.READY); // Reset status
            throw new AIException("Warm-up failed: " + e.getMessage(), e);
        }
    }

    public synchronized void close(AISession session) {
        session.setState(AISessionState.CLOSED);
        activeSessions.remove(session.getSessionId());
    }
}
