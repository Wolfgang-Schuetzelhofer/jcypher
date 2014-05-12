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

package iot.jcypher.api.collection;

import iot.jcypher.api.APIObject;
import iot.jcypher.api.APIObjectAccess;
import iot.jcypher.api.modify.CopyProperties;
import iot.jcypher.api.modify.ModifyFactory;
import iot.jcypher.api.modify.ModifyTerminal;
import iot.jcypher.api.modify.Set;
import iot.jcypher.api.pattern.IElement;
import iot.jcypher.api.pattern.XFactory;
import iot.jcypher.ast.ASTNode;
import iot.jcypher.ast.ClauseType;
import iot.jcypher.ast.collection.CollectExpression;
import iot.jcypher.ast.collection.DoEvalExpression;
import iot.jcypher.ast.pattern.PatternExpression;
import iot.jcypher.values.JcElement;
import iot.jcypher.values.JcLabel;
import iot.jcypher.values.JcProperty;

public class Do extends APIObject {
	
	Do(CollectExpression cx) {
		super();
		this.astNode = cx;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set a property of a node or relation in the DO part of a FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>SET(n.property("age"))</b>.to(20)...</i></div>
	 * <br/>
	 */
	public Set<DoConcat> SET(JcProperty property) {
		Set<DoConcat> ret = ModifyFactory.setPropertyInFOREACH(property, createConcat());
		ModifyTerminal mt = ModifyFactory.createModifyTerminal(ret);
		ASTNode clause = APIObjectAccess.getAstNode(mt);
		clause.setClauseType(ClauseType.SET);
		DoEvalExpression doEval = ((DoEvalExpression)((CollectExpression)this.astNode).getEvalExpression());
		doEval.getClauses().add(clause);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>set a label of a node in the DO part of a FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>SET(n.label("Person"))</b></i></div>
	 * <br/>
	 */
	public DoConcat SET(JcLabel label) {
		ModifyTerminal mt = ModifyFactory.setLabel(label);
		ASTNode clause = APIObjectAccess.getAstNode(mt);
		clause.setClauseType(ClauseType.SET);
		return createConcat(clause);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>select a node or relation to be the source for copying all properties</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>copyPropertiesFrom(n)</b>.to(m)</i></div>
	 * <br/>
	 */
	public CopyProperties<DoConcat> copyPropertiesFrom(JcElement source) {
		CopyProperties<DoConcat> ret = ModifyFactory.copyPropertiesFromInFOREACH(source, createConcat());
		ModifyTerminal mt = ModifyFactory.createModifyTerminal(ret);
		ASTNode clause = APIObjectAccess.getAstNode(mt);
		clause.setClauseType(ClauseType.SET);
		DoEvalExpression doEval = ((DoEvalExpression)((CollectExpression)this.astNode).getEvalExpression());
		doEval.getClauses().add(clause);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a graph element or an entire subgraph in the DO part of a FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>X</b> to create Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>CREATE(X</b>.node(movie)...)</i></div>
	 * <br/>
	 */
	public DoConcat CREATE(IElement... X) {
		for (int i = 0; i < X.length;i++) {
			PatternExpression clause = XFactory.getExpression(X[i]);
			clause.setClauseType(ClauseType.CREATE);
			addClause(clause);
		}
		return createConcat();
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a unique graph element or an entire subgraph in the DO part of a FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>X</b> to create Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>CREATE_UNIQUE(X</b>.node(movie)...)</i></div>
	 * <br/>
	 */
	public DoConcat CREATE_UNIQUE(IElement... X) {
		for (int i = 0; i < X.length;i++) {
			PatternExpression clause = XFactory.getExpression(X[i]);
			clause.setClauseType(ClauseType.CREATE_UNIQUE);
			addClause(clause);
		}
		return createConcat();
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>delete a graph element in the DO part of a FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>DELETE(n)</b></i></div>
	 * <br/>
	 */
	public DoConcat DELETE(JcElement element) {
		ModifyTerminal mt = ModifyFactory.deleteElement(element);
		ASTNode clause = APIObjectAccess.getAstNode(mt);
		clause.setClauseType(ClauseType.DELETE);
		return createConcat(clause);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>remove a property from a node or relation in the DO part of a FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>REMOVE(n.property("age"))</b></i></div>
	 * <br/>
	 */
	public DoConcat REMOVE(JcProperty property) {
		ModifyTerminal mt = ModifyFactory.removeProperty(property);
		ASTNode clause = APIObjectAccess.getAstNode(mt);
		clause.setClauseType(ClauseType.REMOVE);
		return createConcat(clause);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>remove a label from a node in the DO part of a FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>REMOVE(n.label("Person"))</b></i></div>
	 * <br/>
	 */
	public DoConcat REMOVE(JcLabel label) {
		ModifyTerminal mt = ModifyFactory.removeLabel(label);
		ASTNode clause = APIObjectAccess.getAstNode(mt);
		clause.setClauseType(ClauseType.REMOVE);
		return createConcat(clause);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>create a FOREACH expression in the DO part of another FOREACH expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>F</b> to create FOREACH Expressions</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>FOR_EACH(F</b>.element("n")...)</i></div>
	 * <br/>
	 */
	public DoConcat FOR_EACH(DoConcat F) {
		ASTNode clause = APIObjectAccess.getAstNode(F);
		clause.setClauseType(ClauseType.FOREACH);
		return createConcat(clause);
	}
	
	private DoConcat createConcat(ASTNode clause) {
		addClause(clause);
		return createConcat();
	}
	
	private void addClause(ASTNode clause) {
		DoEvalExpression doEval = ((DoEvalExpression)((CollectExpression)this.astNode).getEvalExpression());
		doEval.getClauses().add(clause);
	}
	
	private DoConcat createConcat() {
		DoConcat ret = new DoConcat((CollectExpression)this.astNode, this);
		return ret;
	}
}
