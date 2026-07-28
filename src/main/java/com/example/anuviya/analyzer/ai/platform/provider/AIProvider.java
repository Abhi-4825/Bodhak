package com.example.anuviya.analyzer.ai.platform.provider;

import com.example.anuviya.analyzer.ai.platform.AIException;
import com.example.anuviya.analyzer.ai.platform.model.ModelInfo;
import com.example.anuviya.analyzer.ai.platform.model.RuntimeStatus;
import com.example.anuviya.analyzer.ai.platform.prompt.PromptPackage;
import com.example.anuviya.analyzer.ai.platform.session.AISession;

import java.util.List;
import java.util.function.Consumer;

public interface AIProvider {
    ProviderInfo info();
    RuntimeStatus runtimeStatus();
    ProviderHealth health();
    boolean start();
    boolean stop();
    List<ModelInfo> installedModels();
    String generate(AISession session, PromptPackage prompt) throws AIException;
    void stream(AISession session, PromptPackage prompt, Consumer<String> chunkConsumer) throws AIException;
}
