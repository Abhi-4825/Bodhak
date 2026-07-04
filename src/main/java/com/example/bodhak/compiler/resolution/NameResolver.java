package com.example.bodhak.compiler.resolution;

import com.example.bodhak.compiler.CompilationUnit;
import com.example.bodhak.ir.declaration.ImportDeclaration;
import java.util.*;

/**
 * Dedicated, language-agnostic name resolver that resolves simple/qualified/aliased
 * names to fully qualified names based on compilation unit imports and package context.
 */
public final class NameResolver {

    private final CompilationUnit cu;
    private final Set<String> knownEntityNames;

    private NameResolver(CompilationUnit cu, Set<String> knownEntityNames) {
        this.cu = cu;
        this.knownEntityNames = knownEntityNames;
    }

    public static NameResolver build(CompilationUnit cu, Set<String> knownEntityNames) {
        return new NameResolver(cu, knownEntityNames);
    }

    /**
     * Resolves a simple or partially-qualified name to a fully-qualified entity name.
     * Returns the best-match FQN from knownEntityNames, or a best-effort FQN fallback.
     */
    public String resolve(String name) {
        if (name == null || name.isEmpty()) return "";
        
        // 1. If it's already a known entity, return it
        if (knownEntityNames.contains(name)) {
            return name;
        }
        
        // Split name into first segment and suffix (e.g. "EvidenceCategory.AST_PATTERN" -> first segment "EvidenceCategory", suffix ".AST_PATTERN")
        int dotIndex = name.indexOf('.');
        String firstSegment = dotIndex == -1 ? name : name.substring(0, dotIndex);
        String suffix = dotIndex == -1 ? "" : name.substring(dotIndex);
        
        String resolvedFirst = firstSegment;
        boolean found = false;

        // 2. Check explicit imports (e.g. import com.example.Foo;)
        for (ImportDeclaration imp : cu.getImports()) {
            if (!imp.isWildcard() && imp.path().endsWith("." + firstSegment)) {
                resolvedFirst = imp.path();
                found = true;
                break;
            }
            if (!imp.isWildcard() && imp.path().equals(firstSegment)) {
                resolvedFirst = imp.path();
                found = true;
                break;
            }
        }
        
        // 3. Check wildcard imports
        if (!found) {
            for (ImportDeclaration imp : cu.getImports()) {
                if (imp.isWildcard()) {
                    String candidate = imp.path() + "." + firstSegment;
                    if (knownEntityNames.contains(candidate)) {
                        resolvedFirst = candidate;
                        found = true;
                        break;
                    }
                }
            }
        }
        
        // 4. Check same-package context (from package/namespace declarations or other entities in the CU)
        if (!found) {
            String pkgPrefix = cu.getEntities().stream()
                .map(e -> e.getNamespaceName())
                .filter(p -> p != null && !p.isEmpty())
                .findFirst().orElse("");
            if (!pkgPrefix.isEmpty()) {
                String candidate = pkgPrefix + "." + firstSegment;
                if (knownEntityNames.contains(candidate)) {
                    resolvedFirst = candidate;
                    found = true;
                }
            }
        }
        
        // If we found a resolution for the first segment, build the full name
        if (found) {
            return resolvedFirst + suffix;
        }
        
        // 5. Fallback package prefix
        String pkgPrefix = cu.getEntities().stream()
            .map(e -> e.getNamespaceName())
            .filter(p -> p != null && !p.isEmpty())
            .findFirst().orElse("");
        if (!pkgPrefix.isEmpty()) {
            return pkgPrefix + "." + name;
        }
        
        return name;
    }

    /**
     * Resolves a member name that may have arrived via a static import.
     * E.g. "CLI" with static import "com.example.Capability.*" -> "com.example.Capability"
     */
    public Optional<String> resolveStaticMember(String memberName) {
        for (ImportDeclaration imp : cu.getImports()) {
            if (imp.isStatic()) {
                if (imp.isWildcard()) {
                    // Static wildcard import: e.g. import static com.example.Capability.*;
                    if (knownEntityNames.contains(imp.path())) {
                        return Optional.of(imp.path());
                    }
                } else if (imp.path().endsWith("." + memberName)) {
                    // Explicit static import: e.g. import static com.example.Capability.CLI;
                    return Optional.of(imp.path().substring(0, imp.path().length() - memberName.length() - 1));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves a FieldAccess chain represented as ordered segments, bottom-up.
     * E.g. ["AST_PATTERN", "EvidenceCategory", "classification", "bodhak", "example", "com"]
     */
    public Optional<String> resolveFieldAccessChain(List<String> segmentsBottomUp) {
        if (segmentsBottomUp == null || segmentsBottomUp.isEmpty()) {
            return Optional.empty();
        }
        
        List<String> segments = new ArrayList<>(segmentsBottomUp);
        Collections.reverse(segments);
        
        for (int i = segments.size(); i >= 1; i--) {
            List<String> prefixSegments = segments.subList(0, i);
            String candidate = String.join(".", prefixSegments);
            String resolved = resolve(candidate);
            if (knownEntityNames.contains(resolved)) {
                return Optional.of(resolved);
            }
        }
        return Optional.empty();
    }
}
