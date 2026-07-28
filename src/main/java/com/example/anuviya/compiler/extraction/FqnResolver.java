package com.example.anuviya.compiler.extraction;

import com.example.anuviya.compiler.CompilationUnit;
import com.example.anuviya.ir.SourceRange;
import com.example.anuviya.model.entity.EntityInfo;

public class FqnResolver {
    public static String resolveEntityFqn(CompilationUnit cu, String nodeName, SourceRange sourceRange, String defaultFqn) {
        for (EntityInfo entity : cu.getEntities()) {
            if (entity.getIdentity().simpleName().equals(nodeName) &&
                entity.getSourceLocation().beginLine() == sourceRange.startLine()) {
                return entity.getEntityName();
            }
        }
        return defaultFqn;
    }
}
