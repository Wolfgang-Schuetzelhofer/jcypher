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

public class ValueAccess {

	public static ValueElement timeStamp() {
		return new ValueElement(null,
				new FunctionInstance(FUNCTION.Common.TIMESTAMP));
	}
	
	public static ValueElement coalesce(ValueElement... expression) {
		ValueElement next = null;
		ValueElement last = null;
		boolean init = true;
		// need clone to avoid infinite loops
		for (int i = expression.length - 1; i >= 0; i--) {
			if (next != null) {
				next = findAndCloneFirst(next);
				if (init) {
					if (findFirst(last) == last) // last has no predecessors
						last = next;
					init = false;
				}
				next.setPredecessor(expression[i]);
				next.setOperatorOrFunction(OPERATOR.Common.COMMA_SEPARATOR);
			} else
				last = expression[i];
			next = expression[i];
		}
		return new ValueElement(last,
				new FunctionInstance(FUNCTION.Common.COALESCE, expression.length));
	}
	
	private static ValueElement findAndCloneFirst(ValueElement ve) {
		ValueElement next = null;
		ValueElement ret;
		ValueElement first = ve;
		while (first.getPredecessor() != null) {
			next = first;
			first = first.getPredecessor();
		}
		
		try {
			ret = first.getClass().newInstance();
			ret.setOperatorOrFunction(first.getOperatorOrFunction());
			ret.setPredecessor(first.getPredecessor());
			if (ret instanceof JcPrimitive) {
				((JcPrimitive)ret).setValue(((JcPrimitive)first).getValue());
			} else if (ret instanceof JcValue) {
				((JcValue)ret).setName(((JcValue)first).getName());
			}
			if (next != null)
				next.setPredecessor(ret);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return ret;
	}
	
	public static ValueElement findFirst(ValueElement ve) {
		ValueElement first = ve;
		while (first.getPredecessor() != null) {
			first = ve.getPredecessor();
		}
		return first;
	}
	
	public static String getName(JcValue jcValue) {
		return jcValue.getName();
	}
	
	public static Object getValue(JcPrimitive jcPrimitive) {
		return jcPrimitive.getValue();
	}
	
	public static ValueElement getPredecessor(ValueElement ve) {
		return ve.getPredecessor();
	}
}
