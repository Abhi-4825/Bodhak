package com.example.anuviya.analyzer.ai.platform.session;

import com.example.anuviya.analyzer.ai.platform.model.AIProfileDef;
import com.example.anuviya.analyzer.ai.platform.model.ModelInfo;
import com.example.anuviya.analyzer.ai.platform.provider.AIProvider;

public class AISession {
    private final String sessionId;
    private final AIProvider provider;
    private final ModelInfo model;
    private final AIProfileDef profile;
    private AISessionState state = AISessionState.CREATED;

    public AISession(String sessionId, AIProvider provider, ModelInfo model, AIProfileDef profile) {
        this.sessionId = sessionId;
        this.provider = provider;
        this.model = model;
        this.profile = profile;
    }

    public String getSessionId() {
        return sessionId;
    }

    public AIProvider getProvider() {
        return provider;
    }

    public ModelInfo getModel() {
        return model;
    }

    public AIProfileDef getProfile() {
        return profile;
    }

    public synchronized AISessionState getState() {
        return state;
    }

    public synchronized void setState(AISessionState state) {
        this.state = state;
    }
}
