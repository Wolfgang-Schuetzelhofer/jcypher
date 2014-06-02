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

package iot.jcypher.samples;

import iot.jcypher.JcQuery;
import iot.jcypher.api.IClause;
import iot.jcypher.factories.clause.CREATE;
import iot.jcypher.factories.clause.MATCH;
import iot.jcypher.factories.clause.RETURN;
import iot.jcypher.values.JcNode;
import iot.jcypher.writer.Format;

/**
 * This JCypher sample is constructing and querying the 'Movie Database'.
 * It is based on the 'Movie Database' sample, provided in the Neo4j Manual in the Tutorials section
 */
public class MovieDatabase {

	public static void main(String[] args) {
		JcNode matrix1 = new JcNode("matrix1");
		JcNode matrix2 = new JcNode("matrix2");
		JcNode matrix3 = new JcNode("matrix3");
		JcNode keanu = new JcNode("keanu");
		JcNode laurence = new JcNode("laurence");
		JcNode carrieanne = new JcNode("carrieanne");
		
		/**
		 * -----------------------------------------------------------------
		 * Create the movie database
		 */
		JcQuery query = new JcQuery();
		query.setClauses(new IClause[] {
				CREATE.node(matrix1).label("Movie")
						.property("title").value("The Matrix")
						.property("year").value("1999-03-31"),
				CREATE.node(matrix1).label("Movie")
						.property("title").value("The Matrix Reloaded")
						.property("year").value("2003-05-07"),
				CREATE.node(matrix1).label("Movie")
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
		print(query, "CREATE MOVIE DATABASE", Format.PRETTY_3);
		
		/**
		 * -----------------------------------------------------------------
		 * Check how many nodes we have now
		 */
		JcNode n = new JcNode("n");
		
		query = new JcQuery();
		query.setClauses(new IClause[] {
				MATCH.node(n),
				RETURN.count().value(n)
		});
		print(query, "COUNT NODES", Format.PRETTY_3);
	}
	
	private static void print(JcQuery query, String title, Format format) {
		System.out.println(title + " --------------------");
		// map to Cypher
		String cypher = Util.toCypher(query, format);
		System.out.println("CYPHER --------------------");
		System.out.println(cypher);
		
		// map to JSON
		String json = Util.toJSON(query, format);
		System.out.println("");
		System.out.println("JSON   --------------------");
		System.out.println(json);
		
		System.out.println("");
	}
}
