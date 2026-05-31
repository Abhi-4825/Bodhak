package com.example.bodhakfrontend.languages.python.ast;

import com.example.bodhakfrontend.core.model.ast.GenericAstNode;
import com.example.bodhakfrontend.core.plugin.AstProvider;
import com.example.bodhakfrontend.core.plugin.Parser;
import org.treesitter.TSNode;
import org.treesitter.TSTree;

import java.nio.file.Path;

/**
 * Builds a GenericAstNode tree from a Python file using Tree-sitter.
 * Filters out "unnamed" nodes (brackets, punctuation, keywords) to keep the tree readable.
 */
public class PythonAstProvider implements AstProvider {

    private final Parser<TSTree> parser;

    public PythonAstProvider(Parser<TSTree> parser) {
        this.parser = parser;
    }

    @Override
    public GenericAstNode parse(Path file) {
        TSTree tree = parser.parse(file);
        if (tree == null) return null;

        TSNode rootNode = tree.getRootNode();
        if (rootNode == null) return null;

        return buildTree(rootNode);
    }

    private GenericAstNode buildTree(TSNode node) {
        // We only care about named nodes (i.e. those possessing structural syntax meaning)
        if (!node.isNamed()) {
            return null; // Ignore punctuation like '(', ')', ':', etc.
        }

        String type = node.getType();
        String label = type;

        // Extract some specific labels if possible for readability (e.g. function names)
        if (type.equals("function_definition") || type.equals("class_definition")) {
            TSNode nameNode = node.getChildByFieldName("name");
            if (nameNode != null) {
                // We'd ideally pull the exact text, but we don't have the source string handy directly here
                // We'd need to extract it from the file again. Let's just use the kind for now.
                label = type + " [named]";
            }
        }

        GenericAstNode astNode = new GenericAstNode(label, type);

        // Tree-sitter points are 0-indexed, matching RichTextFX expectations
        if (node.getStartPoint() != null && node.getEndPoint() != null) {
            astNode.setPosition(
                    node.getStartPoint().getRow(),
                    node.getStartPoint().getColumn(),
                    node.getEndPoint().getRow(),
                    node.getEndPoint().getColumn()
            );
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            TSNode child = node.getChild(i);
            GenericAstNode childAst = buildTree(child);
            if (childAst != null) {
                astNode.addChild(childAst);
            }
        }

        return astNode;
    }
}
