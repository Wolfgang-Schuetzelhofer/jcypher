/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
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

package test;

import iot.jcypher.JcQuery;
import iot.jcypher.api.IClause;
import iot.jcypher.database.DBAccessFactory;
import iot.jcypher.database.DBProperties;
import iot.jcypher.database.IDBAccess;
import iot.jcypher.factories.clause.CREATE;
import iot.jcypher.factories.clause.MATCH;
import iot.jcypher.factories.clause.RETURN;
import iot.jcypher.result.JcQueryResult;
import iot.jcypher.values.JcNode;

import java.util.Properties;

import org.junit.Test;

public class DBAccessTest {

	//@Test
	public void testCreateDB_01() {
		
		Properties props = new Properties();
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7475");
		
		IDBAccess dbAccess = DBAccessFactory.createRemoteDBAccess(props);
		
		JcNode matrix1 = new JcNode("matrix1");
		JcNode matrix2 = new JcNode("matrix2");
		JcNode matrix3 = new JcNode("matrix3");
		JcNode keanu = new JcNode("keanu");
		JcNode laurence = new JcNode("laurence");
		JcNode carrieanne = new JcNode("carrieanne");
		
		/*******************************/
		JcQuery query = new JcQuery();
		query.setClauses(new IClause[] {
				CREATE.node(matrix1).label("Movie")
						.property("title").value("The Matrix")
						.property("year").value("1999-03-31"),
				CREATE.node(matrix2).label("Movie")
						.property("title").value("The Matrix Reloaded")
						.property("year").value("2003-05-07"),
				CREATE.node(matrix3).label("Movie")
						.property("title").value("The Matrix Revolutions")
						.property("year").value("2003-10-27"),
				CREATE.node(keanu).label("Actor")
						.property("name").value("Keanu Reeves"),
				CREATE.node(laurence).label("Actor")
						.property("name").value("Laurence Fishburne"),
				CREATE.node(carrieanne).label("Actor")
						.property("name").value("Carrie-Anne Moss"),
				CREATE.node(keanu).relation().out().type("ACTS_IN").property("role").value("Neo").node(matrix1),
				CREATE.node(keanu).relation().out().type("ACTS_IN").property("role").value("Neo").node(matrix2),
				CREATE.node(keanu).relation().out().type("ACTS_IN").property("role").value("Neo").node(matrix3),
				CREATE.node(laurence).relation().out().type("ACTS_IN").property("role").value("Morpheus").node(matrix1),
				CREATE.node(laurence).relation().out().type("ACTS_IN").property("role").value("Morpheus").node(matrix2),
				CREATE.node(laurence).relation().out().type("ACTS_IN").property("role").value("Morpheus").node(matrix3),
				CREATE.node(carrieanne).relation().out().type("ACTS_IN").property("role").value("Trinity").node(matrix1),
				CREATE.node(carrieanne).relation().out().type("ACTS_IN").property("role").value("Trinity").node(matrix2),
				CREATE.node(carrieanne).relation().out().type("ACTS_IN").property("role").value("Trinity").node(matrix3)
		});
		
		JcQueryResult result = dbAccess.execute(query);
		
		return;
	}
	
	//@Test
	public void testQueryDB_01() {
		
		Properties props = new Properties();
		props.setProperty(DBProperties.SERVER_ROOT_URI, "http://localhost:7475");
		
		IDBAccess dbAccess = DBAccessFactory.createRemoteDBAccess(props);
		
		JcNode movie = new JcNode("movie");
		
		/*******************************/
		JcQuery query = new JcQuery();
		query.setClauses(new IClause[] {
				MATCH.node(movie).label("Movie").property("title").value("The Matrix"),
				RETURN.value(movie)
		});
		JcQueryResult result = dbAccess.execute(query);
		
		return;
	}
}
