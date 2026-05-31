package com.example.bodhakfrontend.core.model.entity;

import java.io.File;
import java.util.List;

/**
 * Language-neutral representation of a callable/structural member of an entity.
 *
 * Replaces both MethodInfo and ConstructorInfo from the old design.
 * A MemberInfo can represent:
 *   - A Java method       (kind = METHOD)
 *   - A Java constructor  (kind = CONSTRUCTOR)
 *   - A Python function   (kind = FUNCTION — inside a class or at module level)
 *   - A Kotlin property   (kind = PROPERTY)
 */
public class MemberInfo {

    private final MemberKind kind;
    private final String name;
    /** Raw return type string. Empty for void/None/constructors. */
    private final String returnType;
    private final List<ParameterInfo> parameters;
    private final List<MethodCallInfo> calledMembers;
    /** Modifier strings: "public", "static", "async", etc. */
    private final List<ModifierKind> modifiers;
    private final int startLine;
    private final int endLine;
    private final int startColumn;
    private final int statementCount;
    private final File sourceFile;

    public MemberInfo(MemberKind kind, String name, String returnType,
                      List<ParameterInfo> parameters, List<MethodCallInfo> calledMembers,
                      List<ModifierKind> modifiers,
                      int startLine, int endLine, int startColumn,
                      int statementCount, File sourceFile) {
        this.kind          = kind;
        this.name          = name;
        this.returnType    = returnType;
        this.parameters    = parameters;
        this.calledMembers = calledMembers;
        this.modifiers     = modifiers;
        this.startLine     = startLine;
        this.endLine       = endLine;
        this.startColumn   = startColumn;
        this.statementCount = statementCount;
        this.sourceFile    = sourceFile;
    }

    public MemberKind getKind()                  { return kind;          }
    public String getName()                      { return name;          }
    public String getReturnType()                { return returnType;    }
    public List<ParameterInfo> getParameters()   { return parameters;   }
    public List<MethodCallInfo> getCalledMembers(){ return calledMembers;}
    public List<ModifierKind> getModifiers()     { return modifiers;    }
    public int getStartLine()                    { return startLine;    }
    public int getEndLine()                      { return endLine;      }
    public int getStartColumn()                  { return startColumn;  }
    public int getStatementCount()               { return statementCount;}
    public File getSourceFile()                  { return sourceFile;   }

    public boolean isAsync() {
        return modifiers.contains(ModifierKind.ASYNC);
    }
    public boolean isPublic() {
        return modifiers.contains(ModifierKind.PUBLIC);
    }

    @Override
    public String toString() { return kind + " " + name; }
}
