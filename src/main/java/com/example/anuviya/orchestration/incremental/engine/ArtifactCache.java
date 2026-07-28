package com.example.anuviya.orchestration.incremental.engine;

import com.example.anuviya.ir.IRNode;
import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.model.entity.EntityInfo;
import com.example.anuviya.model.namespace.NamespaceInfo;
import com.example.anuviya.model.project.ProjectRootInfo;
import com.example.anuviya.model.project.ProjectInfo;
import com.example.anuviya.model.project.ProjectSnapshot;
import com.example.anuviya.classification.classifier.ProjectClassificationResult;
import com.example.anuviya.compiler.symbol.SymbolTable;
import com.example.anuviya.context.db.ReferenceDatabase;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ArtifactCache {

    private final Map<Path, IRNode> irCache = new ConcurrentHashMap<>();
    private final Map<Path, CompilationUnit> cuCache = new ConcurrentHashMap<>();
    private final Map<Path, List<EntityInfo>> entityCache = new ConcurrentHashMap<>();
    private final Map<String, NamespaceInfo> namespaceCache = new ConcurrentHashMap<>();

    private volatile ProjectRootInfo projectRoots;
    private volatile ProjectInfo projectInfo;
    private volatile ProjectSnapshot projectSnapshot;
    private volatile ProjectClassificationResult classificationResult;
    private final SymbolTable symbolTable = new SymbolTable();
    private final ReferenceDatabase referenceDatabase = new ReferenceDatabase();

    public void putIR(Path file, IRNode ir) {
        if (ir == null) {
            irCache.remove(file);
        } else {
            irCache.put(file, ir);
        }
    }

    public IRNode getIR(Path file) {
        return irCache.get(file);
    }

    public void putCU(Path file, CompilationUnit cu) {
        if (cu == null) {
            cuCache.remove(file);
        } else {
            cuCache.put(file, cu);
        }
    }

    public CompilationUnit getCU(Path file) {
        return cuCache.get(file);
    }

    public Map<Path, CompilationUnit> getCuCache() {
        return cuCache;
    }

    public void putEntities(Path file, List<EntityInfo> entities) {
        if (entities == null) {
            entityCache.remove(file);
        } else {
            entityCache.put(file, entities);
        }
    }

    public List<EntityInfo> getEntities(Path file) {
        return entityCache.get(file);
    }

    public Map<Path, List<EntityInfo>> getEntityCache() {
        return entityCache;
    }

    public void putNamespace(String name, NamespaceInfo info) {
        if (info == null) {
            namespaceCache.remove(name);
        } else {
            namespaceCache.put(name, info);
        }
    }

    public NamespaceInfo getNamespace(String name) {
        return namespaceCache.get(name);
    }

    public Map<String, NamespaceInfo> getNamespaceCache() {
        return namespaceCache;
    }

    public ProjectRootInfo getProjectRoots() {
        return projectRoots;
    }

    public void setProjectRoots(ProjectRootInfo projectRoots) {
        this.projectRoots = projectRoots;
    }

    public ProjectInfo getProjectInfo() {
        return projectInfo;
    }

    public void setProjectInfo(ProjectInfo projectInfo) {
        this.projectInfo = projectInfo;
    }

    public ProjectSnapshot getProjectSnapshot() {
        return projectSnapshot;
    }

    public void setProjectSnapshot(ProjectSnapshot projectSnapshot) {
        this.projectSnapshot = projectSnapshot;
    }

    public ProjectClassificationResult getClassificationResult() {
        return classificationResult;
    }

    public void setClassificationResult(ProjectClassificationResult classificationResult) {
        this.classificationResult = classificationResult;
    }

    public SymbolTable getSymbolTable() {
        return symbolTable;
    }

    public ReferenceDatabase getReferenceDatabase() {
        return referenceDatabase;
    }

    public void evict(Path file) {
        irCache.remove(file);
        cuCache.remove(file);
        entityCache.remove(file);
    }

    public void clear() {
        irCache.clear();
        cuCache.clear();
        entityCache.clear();
        namespaceCache.clear();
        projectRoots = null;
        projectInfo = null;
        projectSnapshot = null;
        classificationResult = null;
    }
}
