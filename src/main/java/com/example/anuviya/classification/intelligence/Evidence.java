package com.example.anuviya.classification.intelligence;

public record Evidence(
    EvidenceType type,
    String pattern, // the rule pattern that matched, e.g. *.jsx or spring-boot-starter-web
    String value, // the actual value found, e.g. App.jsx
    String sourceFile, // where it was found, e.g. pom.xml or MyClass.java
    String description
) {}
