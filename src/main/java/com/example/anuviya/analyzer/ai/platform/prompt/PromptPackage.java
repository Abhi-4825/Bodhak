package com.example.anuviya.analyzer.ai.platform.prompt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record PromptPackage(
    String systemPrompt,
    String userPrompt,
    Map<String, String> metadata,
    List<String> attachments
) {
    public PromptPackage {
        if (metadata == null) metadata = new HashMap<>();
        if (attachments == null) attachments = new ArrayList<>();
    }
}
