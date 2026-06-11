package com.example.bodhakfrontend.ui.dashboard.architecture.model;

public record ArchitectureDashboardData(

        double healthScore,

        int cycles,

        int dependencies,

        int namespaces,

        int classes

) {
}
