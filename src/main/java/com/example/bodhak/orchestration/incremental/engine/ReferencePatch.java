package com.example.bodhak.orchestration.incremental.engine;

import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.model.reference.SemanticReference;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ReferencePatch {

    @SuppressWarnings("unchecked")
    public static void patchRemoveFile(ReferenceDatabase db, Path file) {
        Path normalizedFile = file.toAbsolutePath().normalize();
        
        synchronized (db) {
            try {
                java.lang.reflect.Field refsField = ReferenceDatabase.class.getDeclaredField("references");
                refsField.setAccessible(true);
                List<SemanticReference> refs = (List<SemanticReference>) refsField.get(db);

                java.lang.reflect.Field outgoingField = ReferenceDatabase.class.getDeclaredField("outgoingIndex");
                outgoingField.setAccessible(true);
                Map<?, List<SemanticReference>> outgoing = (Map<?, List<SemanticReference>>) outgoingField.get(db);

                java.lang.reflect.Field incomingField = ReferenceDatabase.class.getDeclaredField("incomingIndex");
                incomingField.setAccessible(true);
                Map<?, List<SemanticReference>> incoming = (Map<?, List<SemanticReference>>) incomingField.get(db);

                java.lang.reflect.Field kindField = ReferenceDatabase.class.getDeclaredField("kindIndex");
                kindField.setAccessible(true);
                Map<?, List<SemanticReference>> kindIdx = (Map<?, List<SemanticReference>>) kindField.get(db);

                // Collect references to remove
                List<SemanticReference> toRemove = new ArrayList<>();
                for (SemanticReference ref : refs) {
                    if (ref.sourceFile() != null) {
                        Path refPath = ref.sourceFile().toPath().toAbsolutePath().normalize();
                        if (refPath.equals(normalizedFile)) {
                            toRemove.add(ref);
                        }
                    }
                }

                // Remove from all indexes
                for (SemanticReference ref : toRemove) {
                    refs.remove(ref);
                    
                    List<SemanticReference> outList = outgoing.get(ref.sourceSymbol().id());
                    if (outList != null) {
                        outList.remove(ref);
                        if (outList.isEmpty()) {
                            outgoing.remove(ref.sourceSymbol().id());
                        }
                    }

                    List<SemanticReference> inList = incoming.get(ref.targetSymbol().id());
                    if (inList != null) {
                        inList.remove(ref);
                        if (inList.isEmpty()) {
                            incoming.remove(ref.targetSymbol().id());
                        }
                    }

                    List<SemanticReference> kindList = kindIdx.get(ref.kind());
                    if (kindList != null) {
                        kindList.remove(ref);
                        if (kindList.isEmpty()) {
                            kindIdx.remove(ref.kind());
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("[ReferencePatch] Failed to patch ReferenceDatabase: " + e.getMessage());
            }
        }
    }
}
