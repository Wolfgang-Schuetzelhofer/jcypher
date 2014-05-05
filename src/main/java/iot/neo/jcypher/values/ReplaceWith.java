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

package iot.neo.jcypher.values;

import iot.neo.jcypher.values.functions.FUNCTION;
import iot.neo.jcypher.values.operators.OPERATOR;



public class ReplaceWith extends JcPrimitive {

	ReplaceWith() {
		super();
	}

	ReplaceWith(Object what, ValueElement pred, IOperatorOrFunction opf) {
		super(what, pred, opf);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the replacement for a part of a string</i></div>
	 * <br/>
	 */
	public JcString with(String with) {
		JcString w = new JcString(with, this, OPERATOR.Common.COMMA_SEPARATOR);
		return new JcString(null, w,
				new FunctionInstance(FUNCTION.String.REPLACE, 3));
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the replacement for a part of a string</i></div>
	 * <br/>
	 */
	public JcString with(JcString with) {
		return new JcString(with, this,
				new FunctionInstance(FUNCTION.String.REPLACE, 3));
	}

}
