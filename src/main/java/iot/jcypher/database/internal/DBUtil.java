/************************************************************************
 * Copyright (c) 2014-2015 IoT-Solutions e.U.
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

package iot.jcypher.database.internal;

import iot.jcypher.database.IDBAccess;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrRelation;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.DO;
import iot.jcypher.query.factories.clause.MATCH;
import iot.jcypher.query.factories.clause.OPTIONAL_MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.SEPARATE;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcRelation;
import iot.jcypher.util.Util;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.StatusType;

public class DBUtil {

	public static List<JcError> clearDatabase(IDBAccess dbAccess) {
		JcNode n = new JcNode("n");
		JcRelation r = new JcRelation("r");
		IClause[] clauses = new IClause[] {
				MATCH.node(n),
				OPTIONAL_MATCH.node(n).relation(r).node(),
				DO.DELETE(n),
				DO.DELETE(r)
		};
		JcQuery query = new JcQuery();
		query.setClauses(clauses);
		JcQueryResult result = dbAccess.execute(query);
		List<JcError> errors = Util.collectErrors(result);
		return errors;
	}
	
	public static boolean isDatabaseEmpty(IDBAccess dbAccess) {
		JcNode n = new JcNode("n");
		JcRelation r = new JcRelation("r");
		JcQuery query = new JcQuery();
		query.setClauses(new IClause[] {
				MATCH.node(n),
				SEPARATE.nextClause(),
				MATCH.node().relation(r).node(),
				RETURN.ALL()
		});
//		Util.printQuery(query, "CHECK", Format.PRETTY_1);
		JcQueryResult result = dbAccess.execute(query);
		if (result.hasErrors()) {
			List<JcError> errors = Util.collectErrors(result);
			throw new JcResultException(errors);
		}
//		Util.printResult(result, "CHECK", Format.PRETTY_1);
		
		// perform check
		List<GrNode> nodes = result.resultOf(n);
		List<GrRelation> relations = result.resultOf(r);
		return nodes.size() == 0 && relations.size() == 0;
	}
	
	public static String getStacktrace(Throwable exception) {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bo);
		exception.printStackTrace(ps);
		ps.flush();
		String ret;
		try {
			ret = bo.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		ps.close();
		return ret;
	}
	
	public static List<JcError> buildErrorList(Response response, Throwable exception) {
		List<JcError> errors = new ArrayList<JcError>();
		
		if (exception == null) {
			if (response != null) {
				StatusType status = response.getStatusInfo();
				if (status != null && status.getStatusCode() >= 400) {
					String code = String.valueOf(status.getStatusCode());
					String msg = status.getReasonPhrase();
					errors.add(new JcError(code, msg, null));
				}
			}
		} else {
			String typ = exception.getClass().getSimpleName();
			String msg = exception.getLocalizedMessage();
			errors.add(new JcError(typ, msg, DBUtil.getStacktrace(exception)));
		}
		
		return errors;
	}
}
