package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.database.internal.PlannerStrategy;
import iot.jcypher.domainquery.internal.Settings;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.graph.Graph;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.api.pattern.Node;
import iot.jcypher.query.api.pattern.Relation;
import iot.jcypher.query.ast.pattern.PatternRelation.Direction;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.query.values.JcString;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class EmbeddedDbRetrieveLabelsTest extends AbstractTestSuite {
    private static IDBAccess dbAccess;

    @BeforeClass
    public static void before() {
        dbAccess = DBAccessSettings.createDBAccess();

        List<JcError> errors = dbAccess.clearDatabase();
        if (errors.size() > 0) {
            printErrors(errors);
            throw new JcResultException(errors);
        }
        createDB_01();
    }

    @AfterClass
    public static void after() {
        if (dbAccess != null) {
            dbAccess.close();
            dbAccess = null;
        }
    }

    static {
        Settings.plannerStrategy = PlannerStrategy.COST;
    }

    @Test
    public void testDBAccess_01() {
        JcNode n0 = new JcNode("n0");
        JcNode n1 = new JcNode("n1");
        JcRelation r0 = new JcRelation("r");

        JcString relType = new JcString("relType");
        JcString name = new JcString("name");
        JcCollection labels = new JcCollection("labels");
        JcNumber id = new JcNumber("id");
        Node match = MATCH.node(n0).relation(r0).type("has_parent").in().node(n1);
        /*******************************/
        JcQuery query = new JcQuery();
        query.setClauses(new IClause[] {
            match,
            WHERE.valueOf(n0.id()).EQUALS(0),
            RETURN.DISTINCT().value(n1.id()).AS(id),
            RETURN.value(n1.property("name")).AS(name),
            RETURN.value(n1.labels()).AS(labels),
            RETURN.value(r0.type()).AS(relType)
        });

        JcQueryResult result = dbAccess.execute(query);
        if (result.hasErrors())
            printErrors(result);
        assertFalse(result.hasErrors());

        List<String> r00 = result.resultOf(relType);

        assertEquals(1, r00.size());
    }

     private static void createDB_01() {
        // create a new graph model
        Graph graph = Graph.create(dbAccess);

        GrNode aoType = graph.createNode();
        aoType.addLabel("Type");
        aoType.addProperty("name", "AO");
        aoType.addProperty("uid", "123");

        GrNode testAoType = graph.createNode();
        testAoType.addLabel("Type");
        testAoType.addProperty("name", "TAO");
        testAoType.addProperty("uid", "345");

        GrRelation rel = graph.createRelation("has_parent", testAoType, aoType);

        // store the graph
        List<JcError> errors = graph.store();

        if (!errors.isEmpty())
            printErrors(errors);
        assertTrue(errors.isEmpty());
    }


}
