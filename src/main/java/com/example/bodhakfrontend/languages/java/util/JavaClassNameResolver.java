package com.example.bodhakfrontend.languages.java.util;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.TypeDeclaration;

public class JavaClassNameResolver {

    public static String resolveFqn(CompilationUnit cu, TypeDeclaration<?> typeDecl) {
        String packageName = cu.getPackageDeclaration()
                .map(p -> p.getNameAsString())
                .orElse("");
                
        String simpleName = typeDecl.getNameAsString();
        
        if (packageName.isEmpty()) {
            return simpleName;
        }
        return packageName + "." + simpleName;
    }
}
