package com.example.bodhak.quality.flag;

import com.example.bodhak.model.entity.EntityInfo;
import java.util.List;

public class ProjectBaselineCalculator {

    public ProjectBaselines calculate(
            List<EntityInfo> entities
    ) {

        return new ProjectBaselines(

                percentile(
                        entities.stream()
                                .map(EntityInfo::getLinesOfCode)
                                .toList()
                ),

                percentile(
                        entities.stream()
                                .map(e -> (int) e.getMethodCount())
                                .toList()
                ),

                percentile(
                        entities.stream()
                                .map(e -> e.getFields().size())
                                .toList()
                ),

                percentile(
                        entities.stream()
                                .map(e -> e.getUsedBy().size())
                                .toList()
                ),

                percentile(
                        entities.stream()
                                .map(e -> e.getDependsOn().size())
                                .toList()
                )
        );
    }

    private int percentile(List<Integer> values) {

        if (values.isEmpty()) {
            return 0;
        }

        List<Integer> sorted =
                values.stream()
                        .sorted()
                        .toList();

        int index =
                (int) Math.ceil(sorted.size() * 0.90) - 1;

        index =
                Math.max(0, Math.min(index, sorted.size() - 1));

        return sorted.get(index);
    }
}
