/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package test.greg;

import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;
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

public class GregTest {

	public static IDBAccess dbAccess;
	public static String domainName;

	@Test
	public void testGregSample() {
		List<JcError> errs;
		IDomainAccess da = DomainAccessFactory.createDomainAccess(dbAccess, domainName);

		// initially populate the domain
		initDomain(da);

		// you can have a look at the graph using Neo4j Browser
		// and the following CYPHER query:
		// MATCH (n:BIStats) MATCH (e:Env) MATCH (a:App) RETURN n,e,a

		conditionallyCreateEnv_App(da);

		DomainQuery q = da.createQuery();

		// match Environment and Application
		DomainObjectMatch<Env> envMatch = q.createMatch(Env.class);
		DomainObjectMatch<App> appMatch = q.createMatch(App.class);
		q.WHERE(envMatch.atttribute("name")).EQUALS("Paradise");
		q.WHERE(appMatch.atttribute("name")).EQUALS("ITunes");

		// match existing BIStats
		DomainObjectMatch<BIStats> statsMatch = q.createMatch(BIStats.class);

		// traverse from existing BStats to measuredIn and measuredFor
		DomainObjectMatch<Env> statEnv = q.TRAVERSE_FROM(statsMatch).FORTH("measuredIn").TO(Env.class);
		DomainObjectMatch<App> statApp = q.TRAVERSE_FROM(statsMatch).FORTH("measuredFor").TO(App.class);

		// select from existing BStats those connected to specified Env and App
		DomainObjectMatch<BIStats> n2bChainedMatch = q.SELECT_FROM(statsMatch)
				.ELEMENTS(
						q.WHERE(statEnv).CONTAINS(envMatch), q.WHERE(statApp).CONTAINS(appMatch)
				);

		// execute query
		DomainQueryResult result = q.execute();

		// obtain results
		List<BIStats> n2bChained = result.resultOf(n2bChainedMatch);
		Env env = result.resultOf(envMatch).get(0);
		App app = result.resultOf(appMatch).get(0);

		BIStats newStat = new BIStats();
		newStat.setMeasuredIn(env);
		newStat.setMeasuredFor(app);

		if (n2bChained.size() > 0) {
			BIStats n2bC = n2bChained.get(0);
			n2bC.setMeasuredFor(null);
			n2bC.setMeasuredIn(null);
			newStat.setFollows(n2bC);
		}

		// modifications to the domain object graph
		// will be stored.
		// note: modifications to n2bC will also be stored,
		// as it is reachable from newStat via follows.
		errs = da.store(newStat);
		assertTrue(errs.isEmpty());

		// you can have a look at the graph using Neo4j Browser
		// and the following CYPHER query:
		// MATCH (n:BIStats) MATCH (e:Env) MATCH (a:App) RETURN n,e,a

		return;
	}

	private void initDomain(IDomainAccess da) {
		Env env = new Env();
		env.setName("Paradise");
		App app = new App();
		app.setName("ITunes");
		BIStats stats = new BIStats();
		stats.setMeasuredIn(env);
		stats.setMeasuredFor(app);

		List<JcError> errs = da.store(stats);
		assertTrue(errs.isEmpty());

		// stats.setMeasuredFor(null);
		// stats.setMeasuredIn(null);
		//
		// errs = da.store(stats);
		// assertTrue(errs.isEmpty());
	}

	private void conditionallyCreateEnv_App(IDomainAccess da) {
		List<JcError> errs;

		DomainQuery q = da.createQuery();
		DomainObjectMatch<Env> envMatch = q.createMatch(Env.class);
		DomainObjectMatch<App> appMatch = q.createMatch(App.class);
		q.WHERE(envMatch.atttribute("name")).EQUALS("Paradise");
		q.WHERE(appMatch.atttribute("name")).EQUALS("ITunes");
		DomainQueryResult result = q.execute();
		List<Env> envs = result.resultOf(envMatch);
		List<App> apps = result.resultOf(appMatch);

		Env env = envs.size() > 0 ? envs.get(0) : null;
		if (env == null) {
			env = new Env();
			env.setName("Paradise");
			errs = da.store(env);
			assertTrue(errs.isEmpty());
		}
		App app = apps.size() > 0 ? apps.get(0) : null;
		if (app == null) {
			app = new App();
			app.setName("ITunes");
			errs = da.store(app);
			assertTrue(errs.isEmpty());
		}
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

		List<JcError> errors = dbAccess.clearDatabase();
		assertTrue(errors.isEmpty());
	}

	@AfterClass
	public static void after() {
		if (dbAccess != null) {
			dbAccess.close();
			dbAccess = null;
		}
	}
}
