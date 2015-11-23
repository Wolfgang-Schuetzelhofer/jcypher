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

package iot.jcypher.domain.internal;

import iot.jcypher.query.writer.CypherWriter;

import java.util.ArrayList;
import java.util.List;

public class RecordedQuery {

	private boolean generic;
	private List<Statement> statements;
	
	public RecordedQuery(boolean generic) {
		super();
		this.generic = generic;
		this.statements = new ArrayList<Statement>();
	}
	
	public void addInvocation(String on, String method, String retObjectRef, List<Statement> params) {
		Invocation inv = new Invocation(on, method, retObjectRef, params);
		this.statements.add(inv);
	}
	
	public void addBracketOpen() {
		BR_Open br = new BR_Open();
		this.statements.add(br);
	}
	
	public void addBracketClose() {
		BR_Close br = new BR_Close();
		this.statements.add(br);
	}
	
	public Literal literal(Object value) {
		return new Literal(value);
	}
	
	public List<Statement> getStatements() {
		return statements;
	}

	public Statement getLastStatement() {
		if (this.statements.size() > 0)
			return this.statements.get(this.statements.size() - 1);
		return null;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.generic ? "Generic-DomainQuery" : "DomainQuery");
		sb.append("\n");
		Statement prev = null;
		int idx = 0;
		for(Statement s : this.statements) {
			if (prev != null && prev instanceof Invocation) {
				
			}
			sb.append(s.toString());
			sb.append(";\n");
			idx++;
			prev = s;
		}
		return sb.toString();
	}

	/**************************************/
	public interface Statement {
		
	}

	/**************************************/
	public class Invocation implements Statement {
		private String onObjectRef;
		private String method;
		private String returnObjectRef;
		private List<Statement> params;
		
		public Invocation(String onObjectRef, String method, String retObjectRef, List<Statement> params) {
			super();
			this.onObjectRef = onObjectRef;
			this.method = method;
			this.returnObjectRef = retObjectRef;
			this.params = params;
		}

		public void setOnObjectRef(String onObjectRef) {
			this.onObjectRef = onObjectRef;
		}

		public String getOnObjectRef() {
			return onObjectRef;
		}

		public String getReturnObjectRef() {
			return returnObjectRef;
		}

		public void setReturnObjectRef(String returnObjectRef) {
			this.returnObjectRef = returnObjectRef;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(this.onObjectRef);
			sb.append('.');
			sb.append(this.method);
			sb.append('(');
			for (int i = 0; i < this.params.size(); i++) {
				sb.append(this.params.get(i));
				if (i < this.params.size() - 1)
					sb.append(", ");
			}
			sb.append(')');
			return sb.toString();
		}
	}
	
	/**************************************/
	public class Literal implements Statement {
		
		private Object value;

		public Literal(Object value) {
			super();
			this.value = value;
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			if (this.value != null)
				CypherWriter.PrimitiveCypherWriter.writePrimitiveValue(this.value, null, sb);
			else
				sb.append("null");
			return sb.toString();
		}
	}
	
	/**************************************/
	public class BR_Open implements Statement {
		@Override
		public String toString() {
			return "(";
		}
	}
	
	/**************************************/
	public class BR_Close implements Statement {
		@Override
		public String toString() {
			return ")";
		}
	}
}
