/************************************************************************
 * mapping JCypher queries to JSON (using JSONWriter)
 ************************************************************************/

package iot.jcypher.snippets;

import iot.jcypher.JSONWriter;
import iot.jcypher.JcQuery;
import iot.jcypher.query.writer.Format;
import iot.jcypher.query.writer.WriterContext;


/**
 * mapping JCypher queries to JSON (using JSONWriter)
 *
 */
public class Snippet_03 {

	/**
	 * create (map) JSON out of a JCypher query
	 * @param query a JcQuery
	 * @return a String containing the mapped JSON
	 */
	public static String toCypher(JcQuery query) {
		
		WriterContext context = new WriterContext();

		/** if you omit the next line, the default formatting option Format.NONE is used
		      Available 'pretty' format options are Format.PRETTY_1, Format.PRETTY_2, and Format.PRETTY_3.
		      Note: For JSON mapping there is no difference between Format.PRETTY_1, Format.PRETTY_2,
		      and Format.PRETTY_3*/
		context.cypherFormat = Format.PRETTY_1;
		
		JSONWriter.toJSON(query, context);
		
		/** context.buffer is an instance of StringBuilder which now contains the mapped JSON */
		return context.buffer.toString();
	}

}
