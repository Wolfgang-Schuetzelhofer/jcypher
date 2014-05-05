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

package iot.neo.jcypher.factories.clause;

import iot.neo.jcypher.api.APIObjectAccess;
import iot.neo.jcypher.api.modify.CopyProperties;
import iot.neo.jcypher.api.modify.ModifyTerminal;
import iot.neo.jcypher.api.modify.ModifyFactory;
import iot.neo.jcypher.api.modify.Set;
import iot.neo.jcypher.ast.ASTNode;
import iot.neo.jcypher.clause.ClauseType;
import iot.neo.jcypher.values.JcElement;
import iot.neo.jcypher.values.JcLabel;
import iot.neo.jcypher.values.JcProperty;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER CLAUSE</i></b></div>
 */
public class DO {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set a property of a node or relation in a DO clause</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>SET(n.property("age"))</b>.to(20)...</i></div>
	 * <br/>
	 */
	public static Set<ModifyTerminal> SET(JcProperty property) {
		Set<ModifyTerminal> ret = ModifyFactory.setPropertyInDO(property);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.SET);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set a label of a node in a DO clause</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>SET(n.label("Person"))</b></i></div>
	 * <br/>
	 */
	public static ModifyTerminal SET(JcLabel label) {
		ModifyTerminal ret = ModifyFactory.setLabel(label);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.SET);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select a node or relation to be the source for copying all properties</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>copyPropertiesFrom(n)</b>.to(m)</i></div>
	 * <br/>
	 */
	public static CopyProperties<ModifyTerminal> copyPropertiesFrom(JcElement source) {
		CopyProperties<ModifyTerminal> ret = ModifyFactory.copyPropertiesFromInDO(source);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.SET);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>delete a graph element in a DO clause</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>DELETE(n)</b></i></div>
	 * <br/>
	 */
	public static ModifyTerminal DELETE(JcElement element) {
		ModifyTerminal ret = ModifyFactory.deleteElement(element);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.DELETE);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>remove a property from a node or relation in a DO clause</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>REMOVE(n.property("age"))</b></i></div>
	 * <br/>
	 */
	public static ModifyTerminal REMOVE(JcProperty property) {
		ModifyTerminal ret = ModifyFactory.removeProperty(property);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.REMOVE);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>remove a label from a node in a DO clause</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>REMOVE(n.label("Person"))</b></i></div>
	 * <br/>
	 */
	public static ModifyTerminal REMOVE(JcLabel label) {
		ModifyTerminal ret = ModifyFactory.removeLabel(label);
		ASTNode an = APIObjectAccess.getAstNode(ret);
		an.setClauseType(ClauseType.REMOVE);
		return ret;
	}
}
