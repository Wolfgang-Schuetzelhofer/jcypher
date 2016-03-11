package test.bookingtest;

import java.util.List;

import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import iot.jcypher.graph.GrRelation;

@Ignore
public class JCypherClientTest {

	JCypherClient jCypherClient = new JCypherClient();

	@Test
	public void testInsertBooking() {
		jCypherClient.insertBooking(new Booking(1l, "FRAUD", "CONTRACT", "iam@fraudst.er"));
		jCypherClient.insertBooking(new Booking(2l, "LEGIT", "INIT", "iam@fraudst.er"));
	}

	@Test
	public void testGetGraph() {
		List<GrRelation> bookingsRelated = jCypherClient.getBookingsRelated(1L, 6);
		return;
	}
	
	@BeforeClass
	public static void before() {
//		List<JcError> errors = new JCypherClient().createDbAccess().clearDatabase();
//		if (errors.size() > 0) {
//			throw new JcResultException(errors);
//		}
	}
}
