package com.example.bodhak.quality.flag;

import com.example.bodhak.model.entity.EntityInfo;

import java.util.Set;

public record EntityCharacteristics(EntityInfo entityInfo, Set<EntityFlag> flags) {
}
