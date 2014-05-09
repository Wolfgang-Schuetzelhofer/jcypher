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

import iot.jcypher.values.functions.FUNCTION;
import iot.jcypher.values.operators.OPERATOR;

public class SubString extends JcString {

	SubString() {
		super();
	}
	
	SubString(Object val, ValueElement predecessor, IOperatorOrFunction opf) {
		super(null, val, predecessor, opf);
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the length of a substring to be extracted</i></div>
	 * <br/>
	 */
	public JcString subLength(int len) {
		JcNumber sub = new JcNumber(len, this.getPredecessor(), OPERATOR.Common.COMMA_SEPARATOR); 
		return new JcString(null, sub,
				new FunctionInstance(FUNCTION.String.SUBSTRING, 3));
	}
}
