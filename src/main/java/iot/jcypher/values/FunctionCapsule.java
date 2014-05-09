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

package iot.jcypher.values;

public class FunctionCapsule {

	private String token;
	
	FunctionCapsule(String token) {
		super();
		this.token = token;
	}

	String getToken() {
		return token;
	}

	/***********************************/
	public static class FunctionStart extends FunctionCapsule implements IFragment {

		private Operator operator;
		
		FunctionStart(String token) {
			super(token);
		}

		Operator getOperator() {
			return operator;
		}

		void setOperator(Operator operator) {
			this.operator = operator;
		}
		
	}
	
	/***********************************/
	public static class FunctionEnd extends FunctionCapsule implements IFragment {

		FunctionEnd(String token) {
			super(token);
		}
		
	}
}
