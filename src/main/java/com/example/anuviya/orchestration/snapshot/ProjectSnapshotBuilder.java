package com.example.anuviya.orchestration.snapshot;

import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.namespace.NamespaceInfo;
import com.example.anuviya.model.project.ProjectInfo;
import com.example.anuviya.model.project.ProjectSnapshot;

import java.util.List;
import java.util.Map;

/**
 * Assembles a {@link ProjectSnapshot} from pre-computed parts.
 *
 * The snapshot is a pure structural view of the project:
 * metadata + entities + namespace groupings.
 * No analysis results, no metrics.
 */
public class ProjectSnapshotBuilder {

    /**
     * Build an immutable {@link ProjectSnapshot}.
     *
     * @param projectInfo the project metadata (name, root, language map, entry point, …)
     * @param entities    all parsed entities in this project
     * @param namespaces  namespace key → NamespaceInfo map
     * @return immutable snapshot
     */
    public ProjectSnapshot build(
            ProjectInfo projectInfo,
            List<EntityInfo> entities,
            Map<String, NamespaceInfo> namespaces
    ) {
        return new ProjectSnapshot(
                projectInfo,
                List.copyOf(entities),
                Map.copyOf(namespaces)
        );
    }
}
