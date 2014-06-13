/************************************************************************
 * constructing JCypher queries
 ************************************************************************/

package iot.jcypher.snippets;

import iot.jcypher.JcQuery;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.values.JcNode;

/**
 * constructing JCypher queries
 *
 */
public class Snippet_01 {

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

		/**
		 * -----------------------------------------------------------------
		 * Query all movies
		 */
		JcNode movie = new JcNode("movie");
		
		query = new JcQuery();
		query.setClauses(new IClause[] {
				MATCH.node(movie).label("Movie"),
				RETURN.value(movie)
		});
		
		/**
		 * -----------------------------------------------------------------
		 * Search for a distinct movie
		 */
		JcNode matrix = new JcNode("matrix");
		
		query.setClauses(new IClause[] {
				MATCH.node(matrix).label("Movie").property("title").value("The Matrix"),
				RETURN.value(matrix)
		});
	}

}
