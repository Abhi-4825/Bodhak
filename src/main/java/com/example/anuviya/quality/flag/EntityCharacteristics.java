package com.example.anuviya.quality.flag;

import com.example.anuviya.model.entity.EntityInfo;

import java.util.Set;

public record EntityCharacteristics(EntityInfo entityInfo, Set<EntityFlag> flags) {
}
