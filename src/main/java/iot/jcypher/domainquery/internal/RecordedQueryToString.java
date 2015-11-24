/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.domainquery.internal;

import iot.jcypher.domainquery.internal.RecordedQuery.Assignment;
import iot.jcypher.domainquery.internal.RecordedQuery.Invocation;
import iot.jcypher.domainquery.internal.RecordedQuery.Literal;
import iot.jcypher.domainquery.internal.RecordedQuery.Statement;
import iot.jcypher.query.writer.CypherWriter;

public class RecordedQueryToString {

	public static String queryToString(RecordedQuery query) {
		StringBuilder sb = new StringBuilder();
		sb.append(query.isGeneric() ? "Generic-DomainQuery" : "DomainQuery");
		sb.append("\n");
		Statement prev = null;
		for(Statement s : query.getStatements()) {
			boolean separator = false;
			if (prev != null && prev instanceof Invocation && s instanceof Invocation) {
				if (((Invocation)prev).getReturnObjectRef().equals(((Invocation)s).getOnObjectRef())) {
					sb.append('.');
					separator = true;
				}
			}
			if (!separator) {
				if (prev != null)
					sb.append(";\n");
				sb.append(statementToString(s));
			} else
				sb.append(callToString((Invocation)s));
			prev = s;
		}
		return sb.toString();
	}
	
	private static String statementToString(Statement statement) {
		StringBuilder sb = new StringBuilder();
		if (statement instanceof Literal) {
			if (((Literal)statement).getValue() != null)
				CypherWriter.PrimitiveCypherWriter.writePrimitiveValue(((Literal)statement).getValue(), null, sb);
			else
				sb.append("null");
		} else if (statement instanceof Assignment) {
			sb.append(((Assignment)statement).getReturnObjectRef());
			sb.append(" = ");
			sb.append(invocationToString((Assignment)statement));
		} else if (statement instanceof Invocation) {
			sb.append(invocationToString((Invocation)statement));
		}
		return sb.toString();
	}
	
	private static String invocationToString(Invocation invocation) {
		StringBuilder sb = new StringBuilder();
		sb.append(invocation.getOnObjectRef());
		sb.append('.');
		sb.append(callToString(invocation));
		return sb.toString();
	}
	
	private static String callToString(Invocation invocation) {
		StringBuilder sb = new StringBuilder();
		sb.append(invocation.getMethod());
		sb.append('(');
		for (int i = 0; i < invocation.getParams().size(); i++) {
			sb.append(statementToString(invocation.getParams().get(i)));
			if (i < invocation.getParams().size() - 1)
				sb.append(", ");
		}
		sb.append(')');
		return sb.toString();
	}
}
