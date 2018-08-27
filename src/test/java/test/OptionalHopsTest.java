package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
import java.util.List;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class OptionalHopsTest extends AbstractTestSuite {
    private static IDBAccess dbAccess;

    @BeforeClass
    public static void before() {
        dbAccess = DBAccessSettings.createDBAccess();

        List<JcError> errors = dbAccess.clearDatabase();
        if (errors.size() > 0) {
            printErrors(errors);
            throw new JcResultException(errors);
        }
    }

    @AfterClass
    public static void after() {
        if (dbAccess != null) {
            dbAccess.close();
            dbAccess = null;
        }
    }

    @Test
    public void testDBAccess_01() {
        createDB_01();
        queryDB_01();
        return;
    }

    private void createDB_01() {

        JcNode andreas = new JcNode("Andres");
        JcNode adam = new JcNode("Adam");
        JcNode joe = new JcNode("Joe");
        JcNode c1 = new JcNode("c1");
        JcNode c2 = new JcNode("c2");
        JcNode c3 = new JcNode("c3");
        JcNode n1 = new JcNode("notebook");

        /*******************************/
        JcQuery query = new JcQuery();
        query.setClauses(new IClause[] {
            CREATE.node(andreas).label("JcTestPerson")
                .property("name").value("Andres")
                .property("title").value("Developer"),
            CREATE.node(adam).label("JcTestPerson")
                .property("name").value("Adam")
                .property("title").value("Developer"),
            CREATE.node(joe).label("JcTestPerson")
                .property("name").value("Joe")
                .property("title").value("Manager"),
            CREATE.node(c1).label("JcTestComputer")
                .property("code").value("ABC123"),
            CREATE.node(c2).label("JcTestComputer")
                .property("code").value("ABC345"),
            CREATE.node(c3).label("JcTestComputer")
                .property("code").value("ABC678"),
            CREATE.node(n1).label("JcTestNotebook")
                .property("code").value("N123"),
            CREATE.node(andreas).relation().out().type("POSSESS").node(c1),
            CREATE.node(adam).relation().out().type("POSSESS").node(c2),
            CREATE.node(joe).relation().out().type("POSSESS").node(c3),
            CREATE.node(joe).relation().out().type("LEASE").node(n1),
        });

        JcQueryResult result = dbAccess.execute(query);
        if (result.hasErrors())
            printErrors(result);
        assertFalse(result.hasErrors());

        return;
    }

    private void queryDB_01() {
        JcNode n0 = new JcNode("n0");
        JcNode n1 = new JcNode("n1");
        JcRelation r0 = new JcRelation("r");

        /*******************************/
        JcQuery query = new JcQuery();
        query.setClauses(new IClause[] {
            MATCH.node(n0).label("JcTestPerson")
                .relation(r0).out().type("LEASE").minHops(0)
                .node(n1),
            RETURN.value(n0),
            RETURN.value(r0),
            RETURN.value(n1),
        });

        JcQueryResult result = dbAccess.execute(query);
        if (result.hasErrors())
            printErrors(result);

        List<GrNode> n00 = result.resultOf(n0);
        List<GrRelation> r00 = result.resultOf(r0);
        List<GrNode> n11 = result.resultOf(n1);

        // how *0 should work:
        // https://graphaware.com/graphaware/2015/05/19/neo4j-cypher-variable-length-relationships-by-example.html
        assertEquals(3, n00.size());
        assertEquals(1, r00.size());
        assertEquals(4, n11.size()); //this is all n0 nodes + all possible nodes from LEASE

        return;
    }
}
