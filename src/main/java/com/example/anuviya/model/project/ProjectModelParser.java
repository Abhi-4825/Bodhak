package com.example.anuviya.model.project;

import java.nio.file.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectModelParser {

    public static ProjectModel parse(Path projectRoot, Set<Path> knownFiles) {
        String primaryTool = "unknown";
        Set<Path> buildFiles = new HashSet<>();
        Map<String, String> dependencies = new HashMap<>();
        Set<String> plugins = new HashSet<>();
        Map<String, String> properties = new HashMap<>();

        Path pomPath = projectRoot.resolve("pom.xml");
        if (Files.exists(pomPath)) {
            primaryTool = "Maven";
            buildFiles.add(pomPath);
            parsePom(pomPath, dependencies, plugins, properties);
        }

        Path gradlePath = projectRoot.resolve("build.gradle");
        Path gradleKtsPath = projectRoot.resolve("build.gradle.kts");
        if (Files.exists(gradlePath)) {
            primaryTool = "Gradle";
            buildFiles.add(gradlePath);
            parseGradle(gradlePath, dependencies, plugins);
        } else if (Files.exists(gradleKtsPath)) {
            primaryTool = "Gradle";
            buildFiles.add(gradleKtsPath);
            parseGradle(gradleKtsPath, dependencies, plugins);
        }

        Path packageJsonPath = projectRoot.resolve("package.json");
        if (Files.exists(packageJsonPath)) {
            if (primaryTool.equals("unknown")) {
                primaryTool = "npm";
            }
            buildFiles.add(packageJsonPath);
            parsePackageJson(packageJsonPath, dependencies);
        }

        Path reqPath = projectRoot.resolve("requirements.txt");
        if (Files.exists(reqPath)) {
            if (primaryTool.equals("unknown")) {
                primaryTool = "pip";
            }
            buildFiles.add(reqPath);
            parseRequirementsTxt(reqPath, dependencies);
        }
        Path pyprojectPath = projectRoot.resolve("pyproject.toml");
        if (Files.exists(pyprojectPath)) {
            if (primaryTool.equals("unknown") || primaryTool.equals("pip")) {
                primaryTool = "poetry";
            }
            buildFiles.add(pyprojectPath);
            parsePyprojectToml(pyprojectPath, dependencies);
        }

        Path cargoPath = projectRoot.resolve("Cargo.toml");
        if (Files.exists(cargoPath)) {
            primaryTool = "Cargo";
            buildFiles.add(cargoPath);
            parseCargoToml(cargoPath, dependencies);
        }

        Path goModPath = projectRoot.resolve("go.mod");
        if (Files.exists(goModPath)) {
            primaryTool = "Go";
            buildFiles.add(goModPath);
            parseGoMod(goModPath, dependencies);
        }

        if (knownFiles != null) {
            for (Path file : knownFiles) {
                String filename = file.getFileName().toString();
                if (filename.equalsIgnoreCase("pom.xml") && !file.equals(pomPath)) {
                    buildFiles.add(file);
                    parsePom(file, dependencies, plugins, properties);
                } else if ((filename.equalsIgnoreCase("build.gradle") || filename.equalsIgnoreCase("build.gradle.kts")) 
                           && !file.equals(gradlePath) && !file.equals(gradleKtsPath)) {
                    buildFiles.add(file);
                    parseGradle(file, dependencies, plugins);
                } else if (filename.equalsIgnoreCase("package.json") && !file.equals(packageJsonPath)) {
                    buildFiles.add(file);
                    parsePackageJson(file, dependencies);
                }
            }
        }

        BuildModel buildModel = new BuildModel(primaryTool, buildFiles, dependencies, plugins, properties);

        boolean hasDockerfile = false;
        Set<Path> dockerfiles = new HashSet<>();
        boolean hasK8s = false;
        Set<Path> k8sManifests = new HashSet<>();

        if (knownFiles != null) {
            for (Path file : knownFiles) {
                String filename = file.getFileName().toString();
                if (filename.equalsIgnoreCase("Dockerfile") || filename.toLowerCase().endsWith(".dockerfile")) {
                    hasDockerfile = true;
                    dockerfiles.add(file);
                } else if (filename.endsWith(".yaml") || filename.endsWith(".yml")) {
                    try {
                        String content = Files.readString(file);
                        if (content.contains("apiVersion:") && content.contains("kind:")) {
                            hasK8s = true;
                            k8sManifests.add(file);
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        DeploymentModel deploymentModel = new DeploymentModel(hasDockerfile, dockerfiles, hasK8s, k8sManifests);
        
        String packaging = "none";
        if (primaryTool.equals("Maven")) {
            packaging = properties.getOrDefault("packaging", "jar");
        } else if (primaryTool.equals("npm")) {
            packaging = "tarball";
        } else if (hasDockerfile) {
            packaging = "docker";
        }

        PackagingModel packagingModel = new PackagingModel(packaging);

        return new ProjectModel(buildModel, deploymentModel, packagingModel, List.of(), List.of());
    }

    private static void parsePom(Path pom, Map<String, String> dependencies, Set<String> plugins, Map<String, String> properties) {
        try {
            String content = Files.readString(pom);
            
            Matcher packMatcher = Pattern.compile("<packaging>(.*?)</packaging>").matcher(content);
            if (packMatcher.find()) {
                properties.put("packaging", packMatcher.group(1));
            }

            Matcher depMatcher = Pattern.compile("<dependency>\\s*<groupId>(.*?)</groupId>\\s*<artifactId>(.*?)</artifactId>\\s*(?:<version>(.*?)</version>)?", Pattern.DOTALL).matcher(content);
            while (depMatcher.find()) {
                String artifactId = depMatcher.group(2).trim();
                String version = depMatcher.group(3) != null ? depMatcher.group(3).trim() : "managed";
                dependencies.put(artifactId, version);
            }

            Matcher pluginMatcher = Pattern.compile("<plugin>\\s*<groupId>.*?</groupId>\\s*<artifactId>(.*?)</artifactId>", Pattern.DOTALL).matcher(content);
            while (pluginMatcher.find()) {
                plugins.add(pluginMatcher.group(1).trim());
            }

            Matcher propBlockMatcher = Pattern.compile("<properties>(.*?)</properties>", Pattern.DOTALL).matcher(content);
            if (propBlockMatcher.find()) {
                String propBlock = propBlockMatcher.group(1);
                Matcher propMatcher = Pattern.compile("<(.*?)>(.*?)</\\1>").matcher(propBlock);
                while (propMatcher.find()) {
                    properties.put(propMatcher.group(1).trim(), propMatcher.group(2).trim());
                }
            }
        } catch (Exception ignored) {}
    }

    private static void parseGradle(Path gradle, Map<String, String> dependencies, Set<String> plugins) {
        try {
            String content = Files.readString(gradle);
            Matcher depMatcher = Pattern.compile("(?:implementation|api|compileOnly|runtimeOnly|testImplementation)\\s*\\(?\\s*['\"]([^'\"]+?)['\"]\\)?").matcher(content);
            while (depMatcher.find()) {
                String dep = depMatcher.group(1);
                String[] parts = dep.split(":");
                if (parts.length >= 2) {
                    String artifact = parts[1];
                    String version = parts.length >= 3 ? parts[2] : "latest";
                    dependencies.put(artifact, version);
                } else {
                    dependencies.put(dep, "unknown");
                }
            }

            Matcher pluginMatcher = Pattern.compile("id\\s*\\(?\\s*['\"]([^'\"]+?)['\"]\\)?").matcher(content);
            while (pluginMatcher.find()) {
                plugins.add(pluginMatcher.group(1).trim());
            }
        } catch (Exception ignored) {}
    }

    private static void parsePackageJson(Path packageJson, Map<String, String> dependencies) {
        try {
            String content = Files.readString(packageJson);
            Matcher depMatcher = Pattern.compile("\"([^\"]+?)\"\\s*:\\s*\"([^\"]+?)\"").matcher(content);
            while (depMatcher.find()) {
                String name = depMatcher.group(1);
                String version = depMatcher.group(2);
                if (!name.equals("name") && !name.equals("version") && !name.equals("description") && !name.equals("main")) {
                    dependencies.put(name, version);
                }
            }
        } catch (Exception ignored) {}
    }

    private static void parseRequirementsTxt(Path req, Map<String, String> dependencies) {
        try {
            List<String> lines = Files.readAllLines(req);
            for (String line : lines) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                String[] parts = line.split("==");
                if (parts.length >= 2) {
                    dependencies.put(parts[0].trim(), parts[1].trim());
                } else {
                    dependencies.put(line, "any");
                }
            }
        } catch (Exception ignored) {}
    }

    private static void parsePyprojectToml(Path pyproject, Map<String, String> dependencies) {
        try {
            String content = Files.readString(pyproject);
            Matcher depMatcher = Pattern.compile("([a-zA-Z0-9_-]+)\\s*=\\s*\"([^\"]+?)\"").matcher(content);
            while (depMatcher.find()) {
                dependencies.put(depMatcher.group(1), depMatcher.group(2));
            }
        } catch (Exception ignored) {}
    }

    private static void parseCargoToml(Path cargo, Map<String, String> dependencies) {
        try {
            String content = Files.readString(cargo);
            Matcher depMatcher = Pattern.compile("([a-zA-Z0-9_-]+)\\s*=\\s*\"([^\"]+?)\"").matcher(content);
            while (depMatcher.find()) {
                dependencies.put(depMatcher.group(1), depMatcher.group(2));
            }
        } catch (Exception ignored) {}
    }

    private static void parseGoMod(Path goMod, Map<String, String> dependencies) {
        try {
            List<String> lines = Files.readAllLines(goMod);
            for (String line : lines) {
                line = line.trim();
                if (line.startsWith("require (")) continue;
                if (line.startsWith("require")) {
                    String[] parts = line.split("\\s+");
                    if (parts.length >= 3) {
                        dependencies.put(parts[1], parts[2]);
                    }
                }
            }
        } catch (Exception ignored) {}
    }
}
