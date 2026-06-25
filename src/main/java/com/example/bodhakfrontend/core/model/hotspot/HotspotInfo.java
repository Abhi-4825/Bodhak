package com.example.bodhakfrontend.core.model.hotspot;

import com.example.bodhakfrontend.core.analysis.entityflag.EntityFlag;
import com.example.bodhakfrontend.core.model.entity.EntityInfo;


import java.util.Set;

/**
 * A risk hotspot: an entity with a high combined score based on
 * LOC, coupling, member count, and circular dependencies.
 */
public record HotspotInfo
        ( EntityInfo entity,
              Set<EntityFlag> flags,
              double score)
 {
}
