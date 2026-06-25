package com.example.bodhakfrontend.core.analysis.entityflag;

import com.example.bodhakfrontend.core.model.entity.EntityInfo;

import java.util.Set;

public record EntityCharacteristics(EntityInfo entityInfo, Set<EntityFlag> flags) {
}
