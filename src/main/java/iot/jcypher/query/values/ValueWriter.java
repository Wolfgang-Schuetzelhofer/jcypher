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

package iot.jcypher.query.values;

import iot.jcypher.query.CypherWriter.PrimitiveCypherWriter;
import iot.jcypher.query.values.FunctionCapsule.FunctionEnd;
import iot.jcypher.query.values.FunctionCapsule.FunctionStart;
import iot.jcypher.query.values.operators.OPERATOR;
import iot.jcypher.query.values.operators.OPTYPE;
import iot.jcypher.query.writer.WriterContext;

import java.util.ArrayList;
import java.util.List;

public class ValueWriter {
	public static void toValueExpression(ValueElement valueElem, WriterContext context) {
		List<IFragment> elementList = new ArrayList<IFragment>();
		int insertIndex = 0;
		ValueElement ve = valueElem;
		List<FunctionStackEntry> functionStack = new ArrayList<FunctionStackEntry>();
		while (ve != null) {
			if (ve.getOperatorOrFunction() instanceof FunctionInstance) {
				FunctionInstance fi = (FunctionInstance)ve.getOperatorOrFunction();
				FunctionStart start = new FunctionCapsule.FunctionStart(fi.getFunction().getPrefix());
				FunctionEnd end = new FunctionCapsule.FunctionEnd(fi.getFunction().getPostfix());
				functionStack.add(0, new FunctionStackEntry(fi, start));
				elementList.add(insertIndex, end);
				elementList.add(insertIndex, start);
				insertIndex++;
			}
			
			elementList.add(insertIndex, ve);
			
			if (ve.getOperatorOrFunction() instanceof Operator) {
				if ((Operator)ve.getOperatorOrFunction() == OPERATOR.Common.COMMA_SEPARATOR) {
					FunctionStackEntry outmost = null;
					while (functionStack.size() > 0) {
						FunctionStackEntry fe = functionStack.get(0);
						fe.remainingNumArgs--;
						if (fe.remainingNumArgs > 0)
							break;
						outmost = fe;
						functionStack.remove(0);
						insertIndex--;
					}
					
					if (outmost != null) {
						outmost.functionStart.setOperator((Operator)ve.getOperatorOrFunction());
						ve.setOperatorOrFunction(null);
					}
				}
			}

			ve = ve.getPredecessor();
		}
		
		int idx = 0;
		for (IFragment ifrag : elementList) {
			toValueExpression(ifrag, idx, context);
			idx++;
		}
	}
	
	private static void toValueExpression(IFragment ifragment, int index, WriterContext context) {
		if (ifragment instanceof ValueElement) {
			boolean writeAsValue = true;
			ValueElement valueElem = (ValueElement)ifragment;
			IOperatorOrFunction opf = valueElem.getOperatorOrFunction();
			if (opf instanceof Operator) {
				context.buffer.append(((Operator)opf).getPrettySymbol());
				// the value represents a property name
				if (((Operator)opf).getType() == OPTYPE.PropertyContainer.PROPERTY_ACCESS)
					writeAsValue = false;
				// the value represents a label
				else if (((Operator)opf).getType() == OPTYPE.Node.LABEL_ACCESS)
					writeAsValue = false;
			}
			
			boolean nameWritten = false;
			if (valueElem instanceof JcValue) {
				if (((JcValue)valueElem).getName() != null) {
					// exception for JcString and JcNumber having name and value set
					if (!((valueElem instanceof JcString || valueElem instanceof JcNumber) &&
							((JcPrimitive)valueElem).getValue() != null)) {
						context.buffer.append(((JcValue)valueElem).getName());
						nameWritten = true;
					}
				}
			}
			
			if (valueElem instanceof JcPrimitive && !nameWritten) {
				Object val = ((JcPrimitive)valueElem).getValue();
				if (val instanceof ValueElement) {
					toValueExpression((ValueElement)val, context);
				} else if (val != null) {
					if (writeAsValue)
						PrimitiveCypherWriter.writePrimitiveValue(val, context);
					else
						context.buffer.append(val.toString());
				}
			}
		} else if (ifragment instanceof FunctionCapsule) {
			if (ifragment instanceof FunctionStart) {
				Operator op = ((FunctionStart)ifragment).getOperator();
				if (op != null)
					context.buffer.append(op.getPrettySymbol());
			}
			context.buffer.append(((FunctionCapsule)ifragment).getToken());
		}
	}
	
	/**************************************/
	private static class FunctionStackEntry {
		private FunctionStart functionStart;
		private int remainingNumArgs;
		
		FunctionStackEntry(FunctionInstance functionInstance, FunctionStart functionStart) {
			super();
			this.functionStart = functionStart;
			this.remainingNumArgs = functionInstance.getNumArgs();
		}
	}
}
