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
	 * @param pretty a 'FORMAT' enum controlling how the CYPHER output is formatted
	 * @return a String containing the mapped CYPHER statements
	 */
	public static String toCypher(JcQuery query) {
		
		WriterContext context = new WriterContext();

		/** if you omit the next line, the default formatting option Format.NONE is used
		      Available 'pretty' format options are Format.PRETTY_1, Format.PRETTY_2 and Format.PRETTY_2*/
		context.cypherFormat = Format.PRETTY_3;
		
		CypherWriter.toCypherExpression(query, context);
		
		/** context.buffer is an instance of StringBuilder which now contains the mapped CYPHER expression */
		return context.buffer.toString();
	}

}
