package test.bookingtest;

import java.util.Arrays;
import java.util.List;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.MERGE;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;

public class JCypherClient {

	public static final String BOOKING_LABEL = "Booking";
	public static final String ID_PARAMETER = "id";
	public static final String EMAIL_LABEL = "Email";
	public static final String EMAIL_PROPERTY = "email";
	
	private IDBAccess dbAccess;

	public JCypherClient(IDBAccess dbAccess) {
		super();
		this.dbAccess = dbAccess;
	}

	public void insertBooking(Booking booking) {

		JcNode b = new JcNode("b");
		JcNode e = new JcNode("e");

		JcQuery nodeQuery = new JcQuery();
		nodeQuery.setClauses(
				new IClause[] { MERGE.node(b).label(BOOKING_LABEL).property(ID_PARAMETER).value(booking.getId()),
						DO.SET(b.property("fraud")).to(booking.getFraud()),
						DO.SET(b.property("status")).to(booking.getStatus()),
						MERGE.node(e).label(EMAIL_LABEL).property(EMAIL_PROPERTY).value(booking.getEmail()) });

		JcQuery relationQuery = new JcQuery();
		relationQuery.setClauses(
				new IClause[] { MATCH.node(b).label(BOOKING_LABEL).property(ID_PARAMETER).value(booking.getId()),
						MATCH.node(e).label(EMAIL_LABEL).property("email").value(booking.getEmail()),
						MERGE.node(b).relation().type("WITH_EMAIL").node(e) });

		dbAccess.execute(Arrays.asList(nodeQuery, relationQuery));
	}

	public List<GrRelation> getBookingsRelated(Long bookingId, int maxHops) {
		JcQuery nodeQuery = new JcQuery();
		JcNode b = new JcNode("b");
		JcNode b2 = new JcNode("b2");
		JcRelation r = new JcRelation("r");
		nodeQuery.setClauses(new IClause[] { MATCH.node(b).label(BOOKING_LABEL).property(ID_PARAMETER).value(bookingId)
				.relation(r).maxHops(maxHops).node(b2), RETURN.DISTINCT().value(r) });
		return dbAccess.execute(nodeQuery).resultOf(r);
	}
}
