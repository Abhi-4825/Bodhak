package com.example.bodhakfrontend.engine.analyzer;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;
import com.example.bodhakfrontend.core.model.project.EntryPointInfo;
import com.example.bodhakfrontend.core.model.project.UnusedEntityInfo;
import com.example.bodhakfrontend.core.model.warning.UsageStatus;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UnusedEntityAnalyzer {

    public Set<UnusedEntityInfo> analyze(List<EntityInfo> entities, EntryPointInfo entryPointInfo) {
        Set<UnusedEntityInfo> unusedItems = new HashSet<>();

        // If it's used by nothing, and isn't an entry point / framework root / test, it's unused.
        Set<String> frameworkRootNames = entryPointInfo.getAllRoots().stream()
                .map(EntryPointInfo.Entry::entityName).collect(Collectors.toSet());

        for (EntityInfo entity : entities) {
            String name = entity.getEntityName();
            var contrib = entity.getContribution();

            boolean isRoot = false;
            if (entryPointInfo.getPrimaryEntry() != null && name.equals(entryPointInfo.getPrimaryEntry().entityName())) {
                isRoot = true;
            }
            if (frameworkRootNames.contains(name)) {
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
