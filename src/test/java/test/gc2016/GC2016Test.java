package test.gc2016;

import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.DBType;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainAccessFactory;
import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domainquery.DomainQuery;
import iot.jcypher.domainquery.DomainQueryResult;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.query.result.JcError;
import iot.jcypher.util.QueriesPrintObserver;
import test.domainquery.model.Area;
import test.domainquery.model.Person;

@Ignore
public class GC2016Test {

	public static IDBAccess dbAccess;
	public static String domainName;
	private static List<Object> storedDomainObjects;

	@Test
	public void testSample_02() {
		List<JcError> errs;
		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);

		DomainQuery q = da.createQuery();
		DomainObjectMatch<Person> smithMatch = q.createMatch(Person.class);
		q.WHERE(smithMatch.atttribute("lastName")).EQUALS("Smith");
		
		DomainObjectMatch<Area> europeMatch = q.createMatch(Area.class);
		q.WHERE(europeMatch.atttribute("name")).EQUALS("Europe");
		
		DomainObjectMatch<Area> smithAreasMatch = 
				q.TRAVERSE_FROM(smithMatch).FORTH("pointsOfContact").FORTH("area").FORTH("partOf").DISTANCE(0, -1).TO(Area.class);
		
		DomainObjectMatch<Person> smithInEuropeMatch = q.SELECT_FROM(smithMatch).ELEMENTS(
				q.WHERE(smithAreasMatch).CONTAINS(europeMatch)
		);
		
		DomainQueryResult result = q.execute();
		
		List<Person> smith = result.resultOf(smithMatch);
		List<Area> europe = result.resultOf(europeMatch);
		List<Area> smithAreas = result.resultOf(smithAreasMatch);
		List<Person> smithInEurope = result.resultOf(smithInEuropeMatch);
		
		return;
	}

	@BeforeClass
	public static void before() {
		domainName = "QTEST-DOMAIN";
		Properties props = new Properties();

		// properties for remote access and for embedded access
		// (not needed for in memory access)
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7474");
		props.setProperty(DBProperties.DATABASE_DIR, "C:/NEO4J_DBS/01");

		dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props);
		// dbAccess = DBAccessFactory.createDBAccess(DBType.REMOTE, props,
		// "neo4j", "jcypher");

//		List<JcError> errors = dbAccess.clearDatabase();
//		assertTrue(errors.isEmpty());
//
//		// init db
//		Population population = new Population();
//
//		storedDomainObjects = population.createPopulation();
//		
//		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);
//		errors = da.store(storedDomainObjects);
//		assertTrue(errors.isEmpty());
	}

	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
		QueriesPrintObserver.removeAllEnabledQueries();
		QueriesPrintObserver.removeAllOutputStreams();
	}
}
