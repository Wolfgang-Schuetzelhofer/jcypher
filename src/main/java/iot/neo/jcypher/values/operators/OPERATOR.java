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

package iot.neo.jcypher.values.operators;

import iot.neo.jcypher.values.Operator;

public class OPERATOR {

	/*********************************************/
	public static class String {
		public static final Operator CONCAT = new Operator("+", " + ", OPTYPE.String.CONCAT);
	}
	
	/*********************************************/
	public static class Common {
		public static final Operator COMMA_SEPARATOR = new Operator(",", ", ", OPTYPE.String.REPLACE_SEPARATOR);
	}
	
	/*********************************************/
	public static class PropertyContainer {
		public static final Operator PROPERTY_ACCESS = new Operator(".", ".", OPTYPE.PropertyContainer.PROPERTY_ACCESS);
	}
	
	/*********************************************/
	public static class Number {
		public static final Operator PLUS = new Operator("+", " + ", OPTYPE.Number.PLUS);
		public static final Operator MINUS = new Operator("-", " - ", OPTYPE.Number.MINUS);
		public static final Operator MULT = new Operator("*", " * ", OPTYPE.Number.MULT);
		public static final Operator DIV = new Operator("/", " / ", OPTYPE.Number.DIV);
	}
	
	/*********************************************/
	public static class Node {
		public static final Operator LABEL_ACCESS = new Operator(":", ":", OPTYPE.Node.LABEL_ACCESS);
	}
}
