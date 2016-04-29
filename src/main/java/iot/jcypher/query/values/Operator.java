/************************************************************************
 * Copyright (c) 2014-2016 IoT-Solutions e.U.
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

package iot.jcypher.query.values;

public class Operator implements IOperatorOrFunction {

	private String operatorSymbol;
	private String prettySymbol;
	private String postfixSymbol;
	private Enum<?> type;

	public Operator(String operatorSymbol, String prettySymbol, Enum<?> type) {
		this(operatorSymbol, prettySymbol, null, type);
	}
	
	public Operator(String operatorSymbol, String prettySymbol, String postfixSymbol, Enum<?> type) {
		super();
		this.operatorSymbol = operatorSymbol;
		this.prettySymbol = prettySymbol;
		this.postfixSymbol = postfixSymbol;
		this.type = type;
	}
	
	String getOperatorSymbol() {
		return operatorSymbol;
	}

	String getPrettySymbol() {
		return prettySymbol;
	}

	String getPostfixSymbol() {
		return postfixSymbol;
	}

	Enum<?> getType() {
		return type;
	}
}
