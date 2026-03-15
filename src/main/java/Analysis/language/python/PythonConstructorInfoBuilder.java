package Analysis.language.python;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ConstructorInfo;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ModifierKind;
import com.example.bodhakfrontend.IncrementalPart.model.Class.ParameterInfo;
import org.treesitter.TSNode;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class PythonConstructorInfoBuilder {

    private final List<ConstructorInfo> constructors = new ArrayList<>();

    public List<ConstructorInfo> getConstructors() {
        return constructors;
    }

    public void build(TSNode classNode, String source, Path sourceFile) {
        extractConstructors(classNode, source, sourceFile);
    }

    private void extractConstructors(TSNode classNode, String source, Path sourceFile) {

        TSNode classBody = classNode.getChildByFieldName("body");
        if (classBody == null) return;

        for (int i = 0; i < classBody.getChildCount(); i++) {

            TSNode child = classBody.getChild(i);
            TSNode constructorNode = null;
            List<ModifierKind> modifiers = new ArrayList<>();

            if (child.getType().equals("function_definition")) {
                constructorNode = child;
            }

            else if (child.getType().equals("decorated_definition")) {

                constructorNode = child.getChildByFieldName("definition");
                modifiers.addAll(extractPythonModifiers(child, source));
            }

            if (constructorNode == null) continue;

            TSNode nameNode = constructorNode.getChildByFieldName("name");
            if (nameNode == null) continue;

            String methodName = source.substring(
                    nameNode.getStartByte(),
                    nameNode.getEndByte()
            );

            // Only accept __init__ as constructor
            if (!methodName.equals("__init__")) continue;

            // parameters
            List<ParameterInfo> parameters = new ArrayList<>();
            extractParameters(constructorNode, source, parameters);

            // position info
            int startLine = constructorNode.getStartPoint().getRow() + 1;
            int endLine = constructorNode.getEndPoint().getRow() + 1;
            int startColumn = constructorNode.getStartPoint().getColumn();

            constructors.add(new ConstructorInfo(
                    methodName,
                    parameters,
                    modifiers,
                    startLine,
                    endLine,
                    startColumn,
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
