package com.example.anuviya.model.project;

public record PackagingModel(
    String packagingType // jar, war, pom, docker, none
) {
    public static PackagingModel empty() {
        return new PackagingModel("none");
    }
}
