package com.example.bodhakfrontend.core.model.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A completely language-neutral AST node.
 *
 * Language plugins build a tree of these from their native parse tree
 * (JavaParser CompilationUnit, Tree-sitter TSNode, etc.), and the
 * ASTViewer renders this tree without knowing anything about the parser.
 *
 * @param label      Human-readable label shown in the tree (e.g. "Class: Foo")
 * @param kind       Node type category (e.g. "class", "method", "field", "import")
 * @param detail     Optional supplementary info (e.g. return type, value)
 * @param children   Child nodes
 */
public class GenericAstNode {

    private final String label;
    private final String kind;
    private final String detail;
    private final List<GenericAstNode> children = new ArrayList<>();

    // 0-indexed positions for easy mapping to RichTextFX CodeArea
    private int beginLine = -1;
    private int beginColumn = -1;
    private int endLine = -1;
    private int endColumn = -1;

    public GenericAstNode(String label, String kind, String detail) {
        this.label  = label;
        this.kind   = kind;
        this.detail = detail;
    }

    public GenericAstNode(String label, String kind) {
        this(label, kind, "");
    }

    public void addChild(GenericAstNode child) {
        children.add(child);
    }

    public void setPosition(int beginLine, int beginColumn, int endLine, int endColumn) {
        this.beginLine = beginLine;
        this.beginColumn = beginColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public int getBeginLine()   { return beginLine;   }
    public int getBeginColumn() { return beginColumn; }
    public int getEndLine()     { return endLine;     }
    public int getEndColumn()   { return endColumn;   }

    public String getLabel()              { return label;                           }
    public String getKind()               { return kind;                            }
    public String getDetail()             { return detail;                          }
    public List<GenericAstNode> getChildren() { return Collections.unmodifiableList(children); }
    public boolean isLeaf()               { return children.isEmpty();              }

    @Override
    public String toString() {
        if (detail != null && !detail.isEmpty()) {
            return label + " [" + detail + "]";
        }
        return label;
    }
}
