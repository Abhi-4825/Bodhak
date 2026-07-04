package com.example.bodhak;

import com.example.bodhak.compiler.symbol.*;
import com.example.bodhak.model.reference.*;
import com.example.bodhak.model.reference.payload.*;
import com.example.bodhak.context.db.ReferenceDatabase;
import com.example.bodhak.analyzer.policy.DefaultScoringPolicy;
import com.example.bodhak.ir.SourceRange;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.util.List;

public class SemanticReferenceTest {

    @Test
    public void testSymbolTableAndIdentifiers() {
        SymbolTable table = new SymbolTable();

        NamespaceSymbol ns = table.getOrCreateNamespace("com.example.service");
        assertNotNull(ns);
        assertEquals(SymbolKind.NAMESPACE, ns.kind());
        assertEquals("com.example.service", ns.name());

        EntitySymbol entity = table.getOrCreateEntity("com.example.service.UserService", SymbolKind.CLASS);
        assertNotNull(entity);
        assertEquals(SymbolKind.CLASS, entity.kind());
        assertEquals("com.example.service.UserService", entity.name());

        MemberSymbol member = table.getOrCreateMember(entity, "registerUser", SymbolKind.METHOD);
        assertNotNull(member);
        assertEquals(SymbolKind.METHOD, member.kind());
        assertEquals("registerUser", member.name());
        assertEquals(entity, member.parent());

        // Verify ID lookups
        assertEquals(ns, table.getById(ns.id()));
        assertEquals(entity, table.getById(entity.id()));
        assertEquals(member, table.getById(member.id()));
    }

    @Test
    public void testSemanticReferenceStorageAndQueries() {
        SymbolTable table = new SymbolTable();
        ReferenceDatabase database = new ReferenceDatabase();

        EntitySymbol source = table.getOrCreateEntity("UserService", SymbolKind.CLASS);
        EntitySymbol target = table.getOrCreateEntity("UserRepository", SymbolKind.CLASS);

        SourceRange range = new SourceRange(10, 5, 10, 30);
        File file = new File("UserService.java");

        SemanticReference ref1 = new SemanticReference(
            source,
            target,
            new SymbolId(100),
            ReferenceKind.MEMBER,
            SemanticRole.MemberReferenceRole.FIELD,
            new ReferenceCharacteristics(true, true, false, false),
            file,
            range,
            new EmptyPayload()
        );

        SemanticReference ref2 = new SemanticReference(
            source,
            target,
            new SymbolId(101),
            ReferenceKind.CALL,
            SemanticRole.CallReferenceRole.INVOCATION,
            new ReferenceCharacteristics(false, true, false, false),
            file,
            range,
            new CallableInvocationPayload("save", List.of("User"))
        );

        database.addReference(ref1);
        database.addReference(ref2);

        // General list check
        assertEquals(2, database.getAllReferences().size());

        // Outgoing/Incoming index check
        assertEquals(2, database.getOutgoing(source.id()).size());
        assertEquals(2, database.getIncoming(target.id()).size());

        // Query by kind
        ReferenceQuery callQuery = ReferenceQuery.builder()
            .kind(ReferenceKind.CALL)
            .build();
        List<SemanticReference> calls = database.query(callQuery);
        assertEquals(1, calls.size());
        assertTrue(calls.get(0).payload() instanceof CallableInvocationPayload);

        // Query by role
        ReferenceQuery fieldQuery = ReferenceQuery.builder()
            .role(SemanticRole.MemberReferenceRole.FIELD)
            .build();
        List<SemanticReference> fields = database.query(fieldQuery);
        assertEquals(1, fields.size());
        assertEquals(SemanticRole.MemberReferenceRole.FIELD, fields.get(0).role());
    }

    @Test
    public void testScoringPolicy() {
        SymbolTable table = new SymbolTable();
        EntitySymbol source = table.getOrCreateEntity("UserService", SymbolKind.CLASS);
        EntitySymbol target = table.getOrCreateEntity("UserRepository", SymbolKind.CLASS);

        SemanticReference ref = new SemanticReference(
            source,
            target,
            new SymbolId(100),
            ReferenceKind.TYPE,
            SemanticRole.TypeReferenceRole.SUBTYPE,
            new ReferenceCharacteristics(true, true, true, false),
            new File("UserService.java"),
            new SourceRange(1, 1, 1, 1),
            new EmptyPayload()
        );

        DefaultScoringPolicy policy = new DefaultScoringPolicy();
        double weight = policy.calculateWeight(ref);
        assertEquals(5.0, weight, 0.001);
    }
}
