package com.example.anuviya.endpoint.discoverer.spring;
import com.example.anuviya.frontend.FrontendRegistry;

import com.example.anuviya.endpoint.FrameworkEndpointDiscoverer;
import com.example.anuviya.model.endpoint.ServiceEndpoint;
import com.example.anuviya.classification.detection.DetectionContext;
import com.example.anuviya.frontend.java.JavaParserService;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.AnnotationExpr;
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr;
import com.github.javaparser.ast.expr.NormalAnnotationExpr;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class SpringBootEndpointDiscoverer implements FrameworkEndpointDiscoverer {

    @Override
    public String frameworkName() {
        return "spring-boot";
    }

    @Override
    public List<ServiceEndpoint> discover(DetectionContext context, FrontendRegistry registry) {
        List<ServiceEndpoint> endpoints = new ArrayList<>();

        if (context.projectInfo() == null || context.projectInfo().knownFiles() == null) {
            return endpoints;
        }

        List<Path> javaFiles = context.projectInfo().knownFiles().stream()
                .filter(p -> p.toString().endsWith(".java"))
                .toList();
        if (javaFiles.isEmpty()) return endpoints;

        List<Path> sourceRoots = new ArrayList<>();
        sourceRoots.add(context.projectInfo().projectRoot());

        JavaParserService parser = new JavaParserService(sourceRoots);

        for (Path file : javaFiles) {
            CompilationUnit cu = parser.parse(file);
            if (cu != null) {
                List<ClassOrInterfaceDeclaration> classes = cu.findAll(ClassOrInterfaceDeclaration.class);
                for (ClassOrInterfaceDeclaration cls : classes) {
                    if (isRestController(cls)) {
                        String basePath = extractPath(cls, "RequestMapping");
                        if (basePath == null) basePath = "";

                    for (MethodDeclaration method : cls.getMethods()) {
                        String methodPath = extractPath(method, "RequestMapping", "GetMapping", "PostMapping", "PutMapping", "DeleteMapping", "PatchMapping");
                        if (methodPath != null) {
                            String httpMethod = extractHttpMethod(method);
                            String fullPath = normalizePath(basePath + methodPath);
                            
                            // Approximate entity name: className.methodName
                            String entityName = cls.getNameAsString() + "." + method.getNameAsString();
                            endpoints.add(new ServiceEndpoint(httpMethod, fullPath, entityName));
                        }
                    }
                }
            }
        }
        }
        return endpoints;
    }

    private boolean isRestController(ClassOrInterfaceDeclaration cls) {
        return cls.getAnnotationByName("RestController").isPresent();
    }

    private String extractPath(com.github.javaparser.ast.nodeTypes.NodeWithAnnotations<?> node, String... annotationNames) {
        for (String annName : annotationNames) {
            var annOpt = node.getAnnotationByName(annName);
            if (annOpt.isPresent()) {
                AnnotationExpr ann = annOpt.get();
                if (ann instanceof SingleMemberAnnotationExpr singleAnn) {
                    return singleAnn.getMemberValue().asStringLiteralExpr().getValue();
                } else if (ann instanceof NormalAnnotationExpr normalAnn) {
                    var valuePair = normalAnn.getPairs().stream()
                            .filter(p -> p.getNameAsString().equals("value") || p.getNameAsString().equals("path"))
                            .findFirst();
                    if (valuePair.isPresent()) {
                        try {
                            return valuePair.get().getValue().asStringLiteralExpr().getValue();
                        } catch (Exception e) {
                            // In case it's an array or complex expression
                        }
                    }
                }
                return ""; // Default to root if annotation exists but no path provided
            }
        }
        return null;
    }

    private String extractHttpMethod(MethodDeclaration method) {
        if (method.getAnnotationByName("GetMapping").isPresent()) return "GET";
        if (method.getAnnotationByName("PostMapping").isPresent()) return "POST";
        if (method.getAnnotationByName("PutMapping").isPresent()) return "PUT";
        if (method.getAnnotationByName("DeleteMapping").isPresent()) return "DELETE";
        if (method.getAnnotationByName("PatchMapping").isPresent()) return "PATCH";
        return "ANY"; // Default for generic RequestMapping without specified method
    }

    private String normalizePath(String path) {
        if (path.isEmpty()) return "/";
        String normalized = path.replaceAll("/+", "/");
        if (!normalized.startsWith("/")) normalized = "/" + normalized;
        return normalized;
    }
}
