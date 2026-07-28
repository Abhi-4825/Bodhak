package com.example.anuviya.quality.hotspot;

import com.example.anuviya.quality.flag.EntityCharacteristics;
import com.example.anuviya.quality.flag.EntityFlag;

import java.util.Map;

public class HotspotScorer {
    private static final Map<EntityFlag, Integer> WEIGHTS =
            Map.of(

                    EntityFlag.OVERSIZED_ENTITY, 30,

                    EntityFlag.HIGH_COUPLING, 25,

                    EntityFlag.HIGH_FAN_IN, 20,

                    EntityFlag.HIGH_FAN_OUT, 15,

                    EntityFlag.CIRCULAR_DEPENDENCY, 10,

                    EntityFlag.ANEMIC_DOMAIN, 5
            );
    public int score(
            EntityCharacteristics characteristics
    ) {

        return characteristics.flags()
                .stream()
                .mapToInt(flag ->
                        WEIGHTS.getOrDefault(flag, 0)
                )
                .sum();
    }
}
