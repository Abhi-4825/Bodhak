package com.example.bodhakfrontend.ir.analysis;

import com.example.bodhakfrontend.ir.model.*;

/**
 * Debug printer that renders an {@link IrProject} as a human-readable tree.
 *
 * <p>Output example:
 * <pre>
 * ENTITY  com.example.OrderService
 *   METHOD  loadUsers
 *     LOOP depth=1
 *       ├─ CALL  DATABASE  userRepo.findAll
 *       └─ RETURN
 *   METHOD  processOrder
 *     IF
 *       ├─ CALL  NETWORK  restTemplate.postForEntity
 *       └─ ALLOCATION  OrderEvent
 * </pre>
 * </p>
 *
 * <p>This class is for debugging and validation only. It is not part of the
 * analysis pipeline and does not affect analysis results.</p>
 */
public class IrProjectPrinter {

    public static String print(IrProject project) {
        StringBuilder sb = new StringBuilder();
        for (IrEntity entity : project.entities()) {
            sb.append("ENTITY  ").append(entity.entityName()).append('\n');
            for (IrMethod method : entity.methods()) {
                sb.append("  METHOD  ").append(method.methodName()).append('\n');
                printOps(method.operations(), sb, "    ", true);
            }
        }
        return sb.toString();
    }

    private static void printOps(java.util.List<IrOperation> ops, StringBuilder sb, String indent, boolean isRoot) {
        int last = ops.size() - 1;
        for (int i = 0; i <= last; i++) {
            IrOperation op = ops.get(i);
            String connector = isRoot ? "  " : (i == last ? "└─ " : "├─ ");
            String childIndent = isRoot ? indent : indent + (i == last ? "   " : "│  ");

            switch (op) {
                case IrLoop loop ->  {
                    sb.append(indent).append(connector).append("LOOP depth=").append(loop.nestingDepth()).append('\n');
                    printOps(loop.body(), sb, childIndent, false);
                }
                case IrConditional cond -> {
                    sb.append(indent).append(connector).append("IF").append('\n');
                    printOps(cond.body(), sb, childIndent, false);
                }
                case IrCall call -> {
                    sb.append(indent).append(connector)
                            .append("CALL  ").append(call.callType())
                            .append("  ").append(call.target()).append('\n');
                }
                case IrAllocation alloc -> {
                    sb.append(indent).append(connector)
                            .append("ALLOCATION  ").append(alloc.type()).append('\n');
                }
                case IrCollectionOperation colOp -> {
                    sb.append(indent).append(connector)
                            .append("COLLECTION_OP  ").append(colOp.operationType()).append('\n');
                }
                case IrReturn ret -> {
                    sb.append(indent).append(connector).append("RETURN").append('\n');
                }
            }
        }
    }
}
