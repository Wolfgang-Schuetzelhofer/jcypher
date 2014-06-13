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

package iot.jcypher.query.api.returns;

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.ast.returns.ReturnExpression;
import iot.jcypher.query.values.JcValue;

public class RElement<T extends RElement<?>> extends APIObject implements IRElement {

	private T connector;
	
	@SuppressWarnings("unchecked")
	RElement(ReturnExpression rx) {
		super();
		this.astNode = rx;
		this.connector = (T) this;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify an alias for the result</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. JcString personName = new JcString("personName");
	 * <br/>RETURN.element(n.property("name")).<b>AS(personName)</b></i></div>
	 * <br/>
	 */
	public T AS(JcValue alias) {
		getReturnExpression().setAlias(alias);
		return this.connector;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a subset of the result, starting from the top</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. RETURN.resultOf(p.nodes()).<b>LIMIT(3)</b></i></div>
	 * <br/>
	 */
	public RElement<T> LIMIT(int num) {
		getReturnExpression().setLimit(num);
		return this;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return a subset of the result, starting at an offset from the top</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. RETURN.resultOf(p.nodes()).<b>SKIP(2)</b></i></div>
	 * <br/>
	 */
	public RElement<T> SKIP(int num) {
		getReturnExpression().setSkip(num);
		return this;
	}
	
	protected ReturnExpression getReturnExpression() {
		return (ReturnExpression)this.astNode;
	}

	void setConnector(T connector) {
		this.connector = connector;
	}
}
