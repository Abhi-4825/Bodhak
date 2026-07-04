package com.example.bodhak.analyzer.growth.model;


import java.util.Set;

public record SccCluster(

        String clusterId,

        Set<String> nodes,

        int size

) {
}
