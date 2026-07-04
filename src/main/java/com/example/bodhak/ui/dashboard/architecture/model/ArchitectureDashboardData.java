package com.example.bodhak.ui.dashboard.architecture.model;

public record ArchitectureDashboardData(

        double healthScore,

        int cycles,

        int dependencies,

        int namespaces,

        int classes

) {
}
