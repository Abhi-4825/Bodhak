package com.example.anuviya.quality.unused;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.project.ProjectRootInfo;
import com.example.anuviya.model.project.UnusedEntityInfo;
import com.example.anuviya.model.diagnostic.UsageStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UnusedEntityAnalyzer {

    public Set<UnusedEntityInfo> analyze(List<EntityInfo> entities, ProjectRootInfo projectRootInfo) {
        Set<UnusedEntityInfo> unusedItems = new HashSet<>();

        // If it's used by nothing, and isn't a project surface, it's unused.
        Set<String> rootNames = projectRootInfo.surfaces().stream()
                .map(s -> s.symbol().name()).collect(Collectors.toSet());

        for (EntityInfo entity : entities) {
            String name = entity.getEntityName();
            var contrib = entity.getContribution();

            boolean isRoot = false;
            if (rootNames.contains(name)) {
                isRoot = true;
            }
            
            // Any framework-managed component is treated as a root (language-neutral).
            // Language plugins mark these via the standard "framework_root" contribution tag.
            if (contrib.isFrameworkRoot()) {
                isRoot = true;
            }
            
            // Tests are executed externally
            if (contrib.isTest()) {
                isRoot = true;
            }

            // Actually check usedBy
            if (entity.getUsedBy().isEmpty() && !isRoot) {
                // Determine confidence based on public vs private API
                UnusedEntityInfo.Confidence confidence = entity.isPublic() ?
                        UnusedEntityInfo.Confidence.MEDIUM : UnusedEntityInfo.Confidence.HIGH;
                
                unusedItems.add(new UnusedEntityInfo(
                        entity,
                        UsageStatus.UNUSED,
                        confidence,
                        "Entity has 0 incoming dependencies."
                ));
            }
        }
        return unusedItems;
    }
}
