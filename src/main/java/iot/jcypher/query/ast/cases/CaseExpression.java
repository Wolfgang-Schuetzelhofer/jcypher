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

package iot.jcypher.query.ast.cases;

import iot.jcypher.query.ast.ASTNode;
import iot.jcypher.query.values.JcValue;

public class CaseExpression extends ASTNode {

	private JcValue caseValue;

	public JcValue getCaseValue() {
		return caseValue;
	}

	public void setCaseValue(JcValue caseValue) {
		this.caseValue = caseValue;
	}
	
	/****************************************/
	public static class WhenJcValue extends JcValue {

		public WhenJcValue() {
			super(null);
		}
	}
}
