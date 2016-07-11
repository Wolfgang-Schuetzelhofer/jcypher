package test.bookingtest;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;

public class JCypherClientTest {

	private static IDBAccess dbAccess;
	
	@Test
	public void testBooking() {
		JCypherClient jCypherClient = new JCypherClient(dbAccess);
		jCypherClient.insertBooking(new Booking(1l, "FRAUD", "CONTRACT", "iam@fraudst.er"));
		jCypherClient.insertBooking(new Booking(2l, "LEGIT", "INIT", "iam@fraudst.er"));
		
		jCypherClient = new JCypherClient(dbAccess);
		List<GrRelation> bookingsRelated = jCypherClient.getBookingsRelated(1L, 6);
		bookingsRelated = removeMultiples(bookingsRelated); // needed when duplicates are included
		assertEquals(1, bookingsRelated.size());
		
		GrRelation br = bookingsRelated.get(0);
		GrNode sn = br.getStartNode();
		GrProperty fr = sn.getProperty("fraud");
		GrProperty stat = sn.getProperty("status");
		Object frVal = fr.getValue();
		Object statVal = stat.getValue();
		
		assertEquals("FRAUD", frVal);
		assertEquals("CONTRACT", statVal);
		
		return;
	}
	
	private List<GrRelation> removeMultiples(List<GrRelation> list) {
		List<GrRelation> ret = new ArrayList<GrRelation>();
		List<Long> ids = new ArrayList<Long>();
		for (GrRelation r : list) {
			if (!ids.contains(Long.valueOf(r.getId()))) {
				ret.add(r);
				ids.add(Long.valueOf(r.getId()));
			}
		}
		return ret;
	}
	
	@BeforeClass
	public static void before() {
		dbAccess = createDBAccess();
		List<JcError> errors = dbAccess.clearDatabase();
		if (errors.size() > 0) {
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
	
	private static IDBAccess createDBAccess() {
		Properties props = new Properties();
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");
		return DBAccessFactory.createDBAccess(DBType.IN_MEMORY, props);
	}
}
