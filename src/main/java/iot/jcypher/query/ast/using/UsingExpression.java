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

package iot.jcypher.query.ast.using;

import iot.jcypher.query.ast.ASTNode;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcProperty;
import iot.jcypher.query.values.JcValue;

public class UsingExpression extends ASTNode {

	private String indexLabel;
	private JcValue valueRef;
	
	public String getIndexLabel() {
		return indexLabel;
	}

	public void setIndexLabel(String indexLabel) {
		this.indexLabel = indexLabel;
	}

	public JcValue getValueRef() {
		return valueRef;
	}

	public void setValueRef(JcValue valueRef) {
		this.valueRef = valueRef;
	}
	
	public boolean isUsingIndex() {
		return this.valueRef instanceof JcProperty;
	}
	
	public boolean isUsingScan() {
		return this.valueRef instanceof JcNode;
	}
}
