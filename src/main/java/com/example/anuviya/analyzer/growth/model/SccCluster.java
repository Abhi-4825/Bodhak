package com.example.anuviya.analyzer.growth.model;


import java.util.Set;

public record SccCluster(

        String clusterId,

        Set<String> nodes,

        int size

) {
}
