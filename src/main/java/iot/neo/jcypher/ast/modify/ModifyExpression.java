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

package iot.neo.jcypher.ast.modify;

import iot.neo.jcypher.ast.ASTNode;
import iot.neo.jcypher.values.JcElement;
import iot.neo.jcypher.values.JcProperty;
import iot.neo.jcypher.values.ValueElement;

public class ModifyExpression extends ASTNode {

	private ModifyAction modifyAction;
	
	private JcProperty toModify;
	private Object value;
	private ValueElement valueExpression;
	private boolean toNull = false;
	private ModifyLabels modifyLabels;
	
	private PropertiesCopy propertiesCopy;
	private JcElement elementToDelete;
	
	public ModifyExpression(ModifyAction modifyAction) {
		super();
		this.modifyAction = modifyAction;
	}

	public ModifyAction getModifyAction() {
		return modifyAction;
	}

	public JcProperty getToModify() {
		return toModify;
	}

	public void setToModify(JcProperty toModify) {
		this.toModify = toModify;
	}

	public Object getValue() {
		return value;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public ValueElement getValueExpression() {
		return valueExpression;
	}

	public void setValueExpression(ValueElement valueExpression) {
		this.valueExpression = valueExpression;
	}

	public boolean isToNull() {
		return toNull;
	}

	public void setToNull() {
		this.toNull = true;
	}

	public PropertiesCopy getPropertiesCopy() {
		return propertiesCopy;
	}

	public void setPropertiesCopy(PropertiesCopy propertiesCopy) {
		this.propertiesCopy = propertiesCopy;
	}

	public ModifyLabels getModifyLabels() {
		return modifyLabels;
	}

	public void setModifyLabels(ModifyLabels modifyLabels) {
		this.modifyLabels = modifyLabels;
	}
	
	public JcElement getElementToDelete() {
		return elementToDelete;
	}

	public void setElementToDelete(JcElement elementToDelete) {
		this.elementToDelete = elementToDelete;
	}

	/******************************************/
	public enum ModifyAction {
		SET, REMOVE, DELETE
	}
}
