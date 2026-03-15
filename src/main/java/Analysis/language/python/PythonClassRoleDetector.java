package Analysis.language.python;

import com.example.bodhakfrontend.IncrementalPart.model.Class.ClassRole;
import org.treesitter.TSNode;

import java.util.HashSet;
import java.util.Set;

import org.treesitter.*;

public class PythonClassRoleDetector {

    public static Set<ClassRole> detectRoles(
            TSNode classNode,
            String className,
            String source
    ) {

        Set<ClassRole> roles = new HashSet<>();

        detectDecorators(classNode, source, roles);
        detectInheritance(classNode, source, roles);
        detectNaming(className, roles);

        return roles;
    }

    private static void detectDecorators(
            TSNode classNode,
            String source,
            Set<ClassRole> roles
    ) {

        TSNode parent = classNode.getParent();

        if (parent != null && parent.getType().equals("decorated_definition")) {

            for (int i = 0; i < parent.getChildCount(); i++) {

                TSNode child = parent.getChild(i);

                if (child.getType().equals("decorator")) {

                    TSNode nameNode = child.getChild(0);

                    String decorator = source.substring(
                            nameNode.getStartByte(),
                            nameNode.getEndByte()
                    );

                    if (decorator.equals("dataclass")) {
                        roles.add(ClassRole.ENTITY);
                    }

                    if (decorator.equals("controller")) {
                        roles.add(ClassRole.CONTROLLER);
                    }

                    if (decorator.equals("configuration")) {
                        roles.add(ClassRole.CONFIGURATION);
                    }
                }
            }
        }
    }

    private static void detectInheritance(
            TSNode classNode,
            String source,
            Set<ClassRole> roles
    ) {

        for (int i = 0; i < classNode.getChildCount(); i++) {

            TSNode child = classNode.getChild(i);

            if (child.getType().equals("argument_list")) {

                for (int j = 0; j < child.getChildCount(); j++) {

                    TSNode base = child.getChild(j);

                    if (base.getType().equals("identifier")) {

                        String baseClass = source.substring(
                                base.getStartByte(),
                                base.getEndByte()
                        );

                        switch (baseClass) {

                            case "Enum":
                                roles.add(ClassRole.ENUM_TYPE);
                                break;

                            case "ABC":
                                roles.add(ClassRole.ABSTRACT_TYPE);
                                break;

                            case "BaseModel":
                                roles.add(ClassRole.DTO);
                                break;

                            case "Model":
                                roles.add(ClassRole.ENTITY);
                                break;

                            case "MethodView":
                                roles.add(ClassRole.CONTROLLER);
                                break;
                        }
                    }
                }
            }
        }
    }

    private static void detectNaming(
            String className,
            Set<ClassRole> roles
    ) {

        if (className.startsWith("Test") || className.endsWith("Test")) {
            roles.add(ClassRole.TEST);
        }

        if (className.endsWith("Service")) {
            roles.add(ClassRole.SERVICE);
        }

        if (className.endsWith("Controller")) {
            roles.add(ClassRole.CONTROLLER);
        }

        if (className.endsWith("Util") || className.endsWith("Utils")) {
            roles.add(ClassRole.UTILITY);
        }
    }
}
