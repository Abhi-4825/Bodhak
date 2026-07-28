package com.example.anuviya.quality.hotspot;

import com.example.anuviya.quality.flag.EntityFlag;
import com.example.anuviya.model.entity.EntityInfo;


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
