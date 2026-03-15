package Analysis.language.python;

import com.example.bodhakfrontend.IncrementalPart.model.Class.MethodCallInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.MethodInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ModifierKind;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ParameterInfo;
import org.treesitter.TSNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PythonMethodInfoBuilder {

    private final List<MethodInfo> methods = new ArrayList<>();

    public List<MethodInfo> getMethods() {
        return methods;
    }

    public void build(TSNode classNode, String source, Path sourceFile) {
        extractMethods(classNode, source, sourceFile);
    }

    private void extractMethods(TSNode classNode, String source, Path sourceFile) {

        TSNode classNameNode = classNode.getChildByFieldName("name");
        String className = source.substring(classNameNode.getStartByte(), classNameNode.getEndByte());

        TSNode body = classNode.getChildByFieldName("body");
        if (body == null) return;

        for (int i = 0; i < body.getChildCount(); i++) {

            TSNode child = body.getChild(i);
            TSNode methodNode = null;
            List<ModifierKind> modifiers = new ArrayList<>();

            if (child.getType().equals("function_definition")) {
                methodNode = child;
            }

            else if (child.getType().equals("decorated_definition")) {

                modifiers.addAll(extractPythonModifiers(child, source));
                methodNode = child.getChildByFieldName("definition");
            }

            if (methodNode == null) continue;

            TSNode nameNode = methodNode.getChildByFieldName("name");
            if (nameNode == null) continue;

            String methodName = source.substring(
                    nameNode.getStartByte(),
                    nameNode.getEndByte()
            );

            // visibility (python naming convention)
            if (methodName.startsWith("__") && !methodName.endsWith("__")) {
                modifiers.add(ModifierKind.PRIVATE);
            }
            else if (methodName.startsWith("_")) {
                modifiers.add(ModifierKind.PROTECTED);
            }
            else {
                modifiers.add(ModifierKind.PUBLIC);
            }

            // return type
            String returnType = null;
            TSNode returnTypeNode = methodNode.getChildByFieldName("return_type");

            if (returnTypeNode != null) {
                returnType = source.substring(
                        returnTypeNode.getStartByte(),
                        returnTypeNode.getEndByte()
                );
            }

            // parameters
            List<ParameterInfo> parameters = new ArrayList<>();
            extractParameters(methodNode, source, parameters);

            // called methods
            List<MethodCallInfo> calledMethods = new ArrayList<>();
            extractCalledMethods(methodNode, source, calledMethods, className);

            // position
            int startLine = methodNode.getStartPoint().getRow() + 1;
            int endLine = methodNode.getEndPoint().getRow() + 1;
            int startColumn = methodNode.getStartPoint().getColumn();

            // method body
            TSNode methodBody = methodNode.getChildByFieldName("body");

            boolean isBodyEmpty = true;
            int statementCount = 0;

            if (methodBody != null) {

                for (int j = 0; j < methodBody.getChildCount(); j++) {

                    TSNode stmt = methodBody.getChild(j);

                    if (!stmt.getType().equals("comment")) {
                        statementCount++;
                    }
                }

                isBodyEmpty = statementCount == 0;
            }

            methods.add(new MethodInfo(
                    methodName,
                    returnType,
                    parameters,
                    calledMethods,
                    modifiers,
                    startLine,
                    endLine,
                    startColumn,
                    isBodyEmpty,
                    statementCount,
                    sourceFile.toFile()
            ));
        }
    }

    private void extractParameters(TSNode methodNode, String source, List<ParameterInfo> parameters) {

        TSNode parameterNode = methodNode.getChildByFieldName("parameters");
        if (parameterNode == null) return;

        for (int i = 0; i < parameterNode.getChildCount(); i++) {

            TSNode param = parameterNode.getChild(i);
            String paramType = param.getType();

            String name = null;
            String type = "Unknown";

            switch (paramType) {

                case "identifier":
                    name = source.substring(
                            param.getStartByte(),
                            param.getEndByte()
                    );
                    break;

                case "typed_parameter":

                    TSNode nameNode = param.getChildByFieldName("name");
                    TSNode typeNode = param.getChildByFieldName("type");

                    if (nameNode != null) {
                        name = source.substring(
                                nameNode.getStartByte(),
                                nameNode.getEndByte()
                        );
                    }

                    if (typeNode != null) {
                        type = source.substring(
                                typeNode.getStartByte(),
                                typeNode.getEndByte()
                        );
                    }
                    break;

                case "default_parameter":

                    TSNode defaultName = param.getChildByFieldName("name");

                    if (defaultName != null) {
                        name = source.substring(
                                defaultName.getStartByte(),
                                defaultName.getEndByte()
                        );
                    }
                    break;

                case "typed_default_parameter":

                    TSNode n = param.getChildByFieldName("name");
                    TSNode t = param.getChildByFieldName("type");

                    if (n != null) {
                        name = source.substring(
                                n.getStartByte(),
                                n.getEndByte()
                        );
                    }

                    if (t != null) {
                        type = source.substring(
                                t.getStartByte(),
                                t.getEndByte()
                        );
                    }
                    break;
            }

            if (name != null) {
                parameters.add(new ParameterInfo(name, type));
            }
        }
    }

    private void extractCalledMethods(TSNode node, String source, List<MethodCallInfo> calledMethods, String currentClass) {

        if (node.getType().equals("call")) {

            TSNode functionNode = node.getChildByFieldName("function");

            if (functionNode != null) {

                String methodName = null;
                String fromClass = null;
                MethodCallInfo.CallType type = MethodCallInfo.CallType.LIBRARY;

                if (functionNode.getType().equals("identifier")) {

                    methodName = source.substring(
                            functionNode.getStartByte(),
                            functionNode.getEndByte()
                    );
                }

                else if (functionNode.getType().equals("attribute")) {

                    TSNode objectNode = functionNode.getChild(0);
                    TSNode methodNode = functionNode.getChild(2);

                    methodName = source.substring(
                            methodNode.getStartByte(),
                            methodNode.getEndByte()
                    );

                    fromClass = source.substring(
                            objectNode.getStartByte(),
                            objectNode.getEndByte()
                    );

                    if ("self".equals(fromClass)) {
                        type = MethodCallInfo.CallType.INTERNAL;
                        fromClass = currentClass;
                    } else {
                        type = MethodCallInfo.CallType.EXTERNAL;
                    }
                }

                if (methodName != null) {

                    calledMethods.add(new MethodCallInfo(
                            methodName,
                            currentClass,
                            fromClass,
                            type
                    ));
                }
            }
        }

        for (int i = 0; i < node.getChildCount(); i++) {
            extractCalledMethods(node.getChild(i), source, calledMethods, currentClass);
        }
    }

    private List<ModifierKind> extractPythonModifiers(TSNode node, String source) {

        List<ModifierKind> modifiers = new ArrayList<>();

        for (int i = 0; i < node.getChildCount(); i++) {

            TSNode child = node.getChild(i);

            if (child.getType().equals("decorator")) {

                TSNode idNode = child.getChildByFieldName("name");

                if (idNode == null) continue;

                String decorator = source.substring(
                        idNode.getStartByte(),
                        idNode.getEndByte()
                );

                switch (decorator) {
                    case "staticmethod" -> modifiers.add(ModifierKind.STATIC);
                    case "abstractmethod" -> modifiers.add(ModifierKind.ABSTRACT);
                    case "final" -> modifiers.add(ModifierKind.FINAL);
                }
            }
        }

        return modifiers;
    }
}