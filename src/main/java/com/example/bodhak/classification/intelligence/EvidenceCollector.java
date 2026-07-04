package com.example.bodhak.classification.intelligence;

import com.example.bodhak.context.AnalysisContext;
import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.model.reference.ReferenceKind;
import com.example.bodhak.model.reference.SemanticReference;
import com.example.bodhak.model.project.ProjectModel;
import com.example.bodhak.model.project.ProjectInfo;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.FileSystems;
import java.nio.file.PathMatcher;
import java.util.*;

public class EvidenceCollector {

    public EvidenceGraph collect(AnalysisContext context, ProjectModel projectModel, List<TechnologyDef> registry) {
        EvidenceGraph graph = new EvidenceGraph();
        ProjectInfo info = context.getProjectInfo();
        ReferenceDatabase refDb = context.getReferenceDatabase();

        var buildModel = projectModel.buildModel();
        
        List<SemanticReference> annotations = refDb != null ? refDb.getByKind(ReferenceKind.ANNOTATION) : List.of();
        List<SemanticReference> calls = refDb != null ? refDb.getByKind(ReferenceKind.CALL) : List.of();
        List<SemanticReference> imports = refDb != null ? refDb.getByKind(ReferenceKind.IMPORT) : List.of();

        for (TechnologyDef tech : registry) {
            for (TechnologyDef.EvidenceRule rule : tech.getEvidence()) {
                EvidenceType type;
                try {
                    type = EvidenceType.valueOf(rule.getType());
                } catch (IllegalArgumentException e) {
                    System.err.println("Unknown evidence type: " + rule.getType() + " in " + tech.getId());
                    continue;
                }

                List<String> patterns = new ArrayList<>();
                if (rule.getPattern() != null && !rule.getPattern().isBlank()) {
                    patterns.add(rule.getPattern());
                }
                if (rule.getAliases() != null) {
                    patterns.addAll(rule.getAliases());
                }

                for (String pattern : patterns) {
                    switch (type) {
                        case BUILD_DEPENDENCY -> {
                            for (var entry : buildModel.dependencies().entrySet()) {
                                if (entry.getKey().equalsIgnoreCase(pattern) || entry.getKey().contains(pattern)) {
                                    graph.addEvidence(new Evidence(
                                        EvidenceType.BUILD_DEPENDENCY,
                                        pattern,
                                        entry.getKey(),
                                        buildModel.buildFiles().isEmpty() ? "build file" : buildModel.buildFiles().iterator().next().getFileName().toString(),
                                        "Detected build dependency: " + entry.getKey() + " (version: " + entry.getValue() + ")"
                                    ));
                                }
                            }
                        }
                        case BUILD_PLUGIN -> {
                            for (String plugin : buildModel.plugins()) {
                                if (plugin.equalsIgnoreCase(pattern) || plugin.contains(pattern)) {
                                    graph.addEvidence(new Evidence(
                                        EvidenceType.BUILD_PLUGIN,
                                        pattern,
                                        plugin,
                                        buildModel.buildFiles().isEmpty() ? "build file" : buildModel.buildFiles().iterator().next().getFileName().toString(),
                                        "Detected build plugin: " + plugin
                                    ));
                                }
                            }
                        }
                        case ANNOTATION_REFERENCE -> {
                            for (SemanticReference ref : annotations) {
                                String targetName = ref.targetSymbol().name();
                                if (targetName.equalsIgnoreCase(pattern) || targetName.endsWith("." + pattern)) {
                                    graph.addEvidence(new Evidence(
                                        EvidenceType.ANNOTATION_REFERENCE,
                                        pattern,
                                        targetName,
                                        ref.sourceFile() != null ? ref.sourceFile().getName() : "source code",
                                        "Detected annotation reference: @" + targetName
                                    ));
                                }
                            }
                        }
                        case METHOD_REFERENCE -> {
                            for (SemanticReference ref : calls) {
                                String targetName = ref.targetSymbol().name();
                                if (targetName.equalsIgnoreCase(pattern) || targetName.contains(pattern)) {
                                    graph.addEvidence(new Evidence(
                                        EvidenceType.METHOD_REFERENCE,
                                        pattern,
                                        targetName,
                                        ref.sourceFile() != null ? ref.sourceFile().getName() : "source code",
                                        "Detected method invocation: " + targetName
                                    ));
                                }
                            }
                        }
                        case IMPORT_REFERENCE -> {
                            for (SemanticReference ref : imports) {
                                String targetName = ref.targetSymbol().name();
                                if (matchImportPattern(targetName, pattern)) {
                                    graph.addEvidence(new Evidence(
                                        EvidenceType.IMPORT_REFERENCE,
                                        pattern,
                                        targetName,
                                        ref.sourceFile() != null ? ref.sourceFile().getName() : "source code",
                                        "Detected import reference: " + targetName
                                    ));
                                }
                            }
                        }
                        case FILE_STRUCTURE -> {
                            if (info != null) {
                                for (Path file : info.knownFiles()) {
                                    String filename = file.getFileName().toString();
                                    if (filename.equalsIgnoreCase(pattern) || globMatches(filename, pattern)) {
                                        graph.addEvidence(new Evidence(
                                            EvidenceType.FILE_STRUCTURE,
                                            pattern,
                                            filename,
                                            filename,
                                            "Detected file: " + filename
                                        ));
                                    }
                                }
                                for (Path folder : info.knownFolders()) {
                                    if (folder.getFileName() != null) {
                                        String foldername = folder.getFileName().toString();
                                        if (foldername.equalsIgnoreCase(pattern)) {
                                            graph.addEvidence(new Evidence(
                                                EvidenceType.FILE_STRUCTURE,
                                                pattern,
                                                foldername,
                                                foldername,
                                                "Detected directory: " + foldername
                                            ));
                                        }
                                    }
                                }
                            }
                        }
                        case PROJECT_ROOT -> {
                            if (info != null && info.projectRootInfo() != null) {
                                for (String archetype : info.projectRootInfo().detectedArchetypes()) {
                                    if (archetype.equalsIgnoreCase(pattern)) {
                                        graph.addEvidence(new Evidence(
                                            EvidenceType.PROJECT_ROOT,
                                            pattern,
                                            archetype,
                                            "ProjectRoots",
                                            "Detected project archetype: " + archetype
                                        ));
                                    }
                                }
                            }
                        }
                        case METHOD_DECLARATION -> {
                            for (var entity : context.getEntities()) {
                                if (entity.getStructure() != null && entity.getStructure().callableDeclarations() != null) {
                                    for (var callable : entity.getStructure().callableDeclarations()) {
                                        if (callable.name().equalsIgnoreCase(pattern)) {
                                            graph.addEvidence(new Evidence(
                                                EvidenceType.METHOD_DECLARATION,
                                                pattern,
                                                callable.name(),
                                                entity.getSourceFile() != null ? entity.getSourceFile().getName() : "source code",
                                                "Detected method declaration: " + callable.name()
                                            ));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        return graph;
    }

    private boolean matchImportPattern(String importStr, String pattern) {
        if (pattern.endsWith(".*")) {
            String prefix = pattern.substring(0, pattern.length() - 2);
            return importStr.startsWith(prefix);
        }
        return importStr.equalsIgnoreCase(pattern);
    }

    private boolean globMatches(String filename, String glob) {
        try {
            PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:" + glob);
            return matcher.matches(Paths.get(filename));
        } catch (Exception e) {
            return false;
        }
    }
}
