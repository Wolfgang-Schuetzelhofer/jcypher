/************************************************************************
 * mapping JCypher queries to CYPHER (using CypherWriter)
 ************************************************************************/

package iot.jcypher.snippets;

import iot.jcypher.CypherWriter;
import iot.jcypher.JcQuery;
import iot.jcypher.query.writer.Format;
import iot.jcypher.query.writer.WriterContext;


/**
 * mapping JCypher queries to CYPHER (using CypherWriter)
 *
 */
public class Snippet_02 {

	/**
	 * create (map) CYPHER out of a JCypher query
	 * @param query a JcQuery
	 * @return a String containing the mapped CYPHER statements
	 */
	public static String toCypher(JcQuery query) {
		
		WriterContext context = new WriterContext();

		/** if you omit the next line, the default formatting option Format.NONE is used
		      Available 'pretty' format options are Format.PRETTY_1, Format.PRETTY_2, and Format.PRETTY_3*/
		context.cypherFormat = Format.PRETTY_3;
		
		CypherWriter.toCypherExpression(query, context);
		
		/** context.buffer is an instance of StringBuilder which now contains the mapped CYPHER expression */
		return context.buffer.toString();
	}

}
