package com.example.bodhakfrontend.engine.growth.model;


import java.util.Set;

public record SccCluster(

        String clusterId,

        Set<String> nodes,

        int size

) {
}
