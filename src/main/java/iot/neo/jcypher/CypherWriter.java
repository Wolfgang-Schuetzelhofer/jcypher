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

package iot.neo.jcypher;

import iot.neo.jcypher.api.APIObject;
import iot.neo.jcypher.api.APIObjectAccess;
import iot.neo.jcypher.ast.ASTNode;
import iot.neo.jcypher.ast.collection.CollectExpression;
import iot.neo.jcypher.ast.collection.CollectExpression.CollectXpressionType;
import iot.neo.jcypher.ast.collection.CollectionSpec;
import iot.neo.jcypher.ast.collection.DoEvalExpression;
import iot.neo.jcypher.ast.collection.EvalExpression;
import iot.neo.jcypher.ast.collection.ExtractEvalExpression;
import iot.neo.jcypher.ast.collection.PredicateEvalExpression;
import iot.neo.jcypher.ast.collection.PropertyEvalExpresssion;
import iot.neo.jcypher.ast.collection.ReduceEvalExpression;
import iot.neo.jcypher.ast.index.IndexExpression;
import iot.neo.jcypher.ast.modify.ModifyExpression;
import iot.neo.jcypher.ast.modify.ModifyExpression.ModifyAction;
import iot.neo.jcypher.ast.modify.PropertiesCopy;
import iot.neo.jcypher.ast.nativ.NativeCypherExpression;
import iot.neo.jcypher.ast.pattern.PatternElement;
import iot.neo.jcypher.ast.pattern.PatternExpression;
import iot.neo.jcypher.ast.pattern.PatternNode;
import iot.neo.jcypher.ast.pattern.PatternPath;
import iot.neo.jcypher.ast.pattern.PatternPath.PathFunction;
import iot.neo.jcypher.ast.pattern.PatternProperty;
import iot.neo.jcypher.ast.pattern.PatternRelation;
import iot.neo.jcypher.ast.pattern.PatternRelation.Direction;
import iot.neo.jcypher.ast.predicate.BooleanOp;
import iot.neo.jcypher.ast.predicate.BooleanOp.Operator;
import iot.neo.jcypher.ast.predicate.ExistsPattern;
import iot.neo.jcypher.ast.predicate.Predicate;
import iot.neo.jcypher.ast.predicate.PredicateConcatenator;
import iot.neo.jcypher.ast.predicate.PredicateExpression;
import iot.neo.jcypher.ast.predicate.PredicateFunction;
import iot.neo.jcypher.ast.predicate.PredicateFunction.PredicateFunctionType;
import iot.neo.jcypher.ast.predicate.SubExpression;
import iot.neo.jcypher.ast.returns.Order;
import iot.neo.jcypher.ast.returns.ReturnBoolean;
import iot.neo.jcypher.ast.returns.ReturnCollection;
import iot.neo.jcypher.ast.returns.ReturnElement;
import iot.neo.jcypher.ast.returns.ReturnExpression;
import iot.neo.jcypher.ast.returns.ReturnPattern;
import iot.neo.jcypher.ast.returns.ReturnValue;
import iot.neo.jcypher.ast.start.PropertyOrQuery;
import iot.neo.jcypher.ast.start.StartExpression;
import iot.neo.jcypher.ast.union.UnionExpression;
import iot.neo.jcypher.ast.using.UsingExpression;
import iot.neo.jcypher.clause.ClauseType;
import iot.neo.jcypher.clause.IClause;
import iot.neo.jcypher.values.JcElement;
import iot.neo.jcypher.values.JcLabel;
import iot.neo.jcypher.values.JcNode;
import iot.neo.jcypher.values.JcProperty;
import iot.neo.jcypher.values.JcValue;
import iot.neo.jcypher.values.ValueAccess;
import iot.neo.jcypher.values.ValueElement;
import iot.neo.jcypher.values.ValueWriter;
import iot.neo.jcypher.writer.Pretty;
import iot.neo.jcypher.writer.WriterContext;

public class CypherWriter {

	public static void toCypherExpression(IClause[] clauses, int index, WriterContext context) {
		int idx = index;
		for (IClause clause : clauses) {
			CypherWriter.toCypherExpression(clause, idx, context);
			idx++;
		}
	}
	
	public static void toCypherExpression(IClause clause, int index, WriterContext context) {
		toCypherExpression(APIObjectAccess.getAstNode((APIObject) clause), index, context);
	}

	private static void toCypherExpression(ASTNode astNode, int index, WriterContext context) {
		boolean hasStart = index > 0;
		ClauseType clauseType = astNode.getClauseType();
		context.currentClause = clauseType;
		
		/*** CYPHER NATIVE CLAUSE **************************************/
		if (clauseType == ClauseType.CYPHER_NATIVE) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context);
			NativeCypherWriter.toCypherExpression((NativeCypherExpression)astNode, context);
		}
		
		/*** START CLAUSE **************************************/
		if (clauseType == ClauseType.START) {
			if (context.previousClause != ClauseType.START) { // otherwise concat multiple starts
				if (hasStart)
					Pretty.writePreClauseSeparator(context);
				context.buffer.append("START");
				Pretty.writePostClauseSeparator(context);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context);
			}
			StartCypherWriter.toCypherExpression((StartExpression)astNode, context);
		}
		
		/*** UNION CLAUSE, UNION ALL CLAUSE **************************************/
		if (clauseType == ClauseType.UNION || clauseType == ClauseType.UNION_ALL) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context);
			UnionExpression ux = (UnionExpression)astNode;
			if (ux.isDistinct())
				context.buffer.append("UNION");
			else
				context.buffer.append("UNION ALL");
			Pretty.writePostClauseSeparator(context);
		}
		
		/*** WITH CLAUSE **************************************/
		if (clauseType == ClauseType.WITH) {
			if (context.previousClause != ClauseType.WITH) { // otherwise concat multiple withs
				if (hasStart)
					Pretty.writePreClauseSeparator(context);
				context.buffer.append("WITH");
				Pretty.writePostClauseSeparator(context);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context);
			}
			ReturnCypherWriter.toCypherExpression((ReturnExpression)astNode, context);
		}
		
		/*** MATCH CLAUSE **************************************/
		if (clauseType == ClauseType.MATCH) {
			if (context.previousClause != ClauseType.MATCH) { // otherwise concat multiple matches
				if (hasStart)
					Pretty.writePreClauseSeparator(context);
				context.buffer.append("MATCH");
				Pretty.writePostClauseSeparator(context);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context);
			}
			PatternCypherWriter.toCypherExpression((PatternExpression)astNode, context);
		}
		
		/*** USING INDEX CLAUSE **************************************/
		if (clauseType == ClauseType.USING_INDEX) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context);
			context.buffer.append("USING INDEX");
			Pretty.writePostClauseSeparator(context);
			UCypherWriter.toCypherExpression((UsingExpression)astNode, context);
		}
		
		/*** USING SCAN CLAUSE **************************************/
		if (clauseType == ClauseType.USING_SCAN) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context);
			context.buffer.append("USING SCAN");
			Pretty.writePostClauseSeparator(context);
			UCypherWriter.toCypherExpression((UsingExpression)astNode, context);
		}
		
		/*** WHERE CLAUSE **************************************/
		if (clauseType == ClauseType.WHERE) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context);
			context.buffer.append("WHERE");
			Pretty.writePostClauseSeparator(context);
			PredicateCypherWriter.toCypherExpression((PredicateExpression)astNode, context);
		}
		
		/*** CREATE CLAUSE **************************************/
		if (clauseType == ClauseType.CREATE) {
			if (context.previousClause != ClauseType.CREATE) { // otherwise concat multiple creates
				if (hasStart)
					Pretty.writePreClauseSeparator(context);
				context.buffer.append("CREATE");
				Pretty.writePostClauseSeparator(context);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context);
			}
			PatternCypherWriter.toCypherExpression((PatternExpression)astNode, context);
		}
		
		/*** CREATE UNIQUE CLAUSE **************************************/
		if (clauseType == ClauseType.CREATE_UNIQUE) {
			if (context.previousClause != ClauseType.CREATE_UNIQUE) { // otherwise concat multiple creates
				if (hasStart)
					Pretty.writePreClauseSeparator(context);
				context.buffer.append("CREATE UNIQUE");
				Pretty.writePostClauseSeparator(context);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context);
			}
			PatternCypherWriter.toCypherExpression((PatternExpression)astNode, context);
		}
		
		/*** RETURN CLAUSE **************************************/
		if (clauseType == ClauseType.RETURN) {
			if (context.previousClause != ClauseType.RETURN) { // otherwise concat multiple returns
				if (hasStart)
					Pretty.writePreClauseSeparator(context);
				context.buffer.append("RETURN");
				Pretty.writePostClauseSeparator(context);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context);
			}
			ReturnCypherWriter.toCypherExpression((ReturnExpression)astNode, context);
		}
		
		/*** SET CLAUSE **************************************/
		if (clauseType == ClauseType.SET) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context);
			context.buffer.append("SET");
			Pretty.writePostClauseSeparator(context);
			STCypherWriter.toCypherExpression((ModifyExpression)astNode, context);
		}
		
		/*** DELETE CLAUSE **************************************/
		if (clauseType == ClauseType.DELETE) {
			if (context.previousClause != ClauseType.DELETE) { // otherwise concat multiple deletes
				if (hasStart)
					Pretty.writePreClauseSeparator(context);
				context.buffer.append("DELETE");
				Pretty.writePostClauseSeparator(context);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context);
			}
			STCypherWriter.toCypherExpression((ModifyExpression)astNode, context);
		}
		
		/*** REMOVE CLAUSE **************************************/
		if (clauseType == ClauseType.REMOVE) {
			if (context.previousClause != ClauseType.REMOVE) { // otherwise concat multiple removes
				if (hasStart)
					Pretty.writePreClauseSeparator(context);
				context.buffer.append("REMOVE");
				Pretty.writePostClauseSeparator(context);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context);
			}
			STCypherWriter.toCypherExpression((ModifyExpression)astNode, context);
		}
		
		/*** FOREACH CLAUSE **************************************/
		if (clauseType == ClauseType.FOREACH) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context);
			context.buffer.append("FOREACH");
			Pretty.writePostClauseSeparator(context);
			CollectionCypherWriter.toCypherSubExpression((CollectExpression)astNode, context);
		}
		
		/*** CREATE INDEX CLAUSE **************************************/
		if (clauseType == ClauseType.CREATE_INDEX) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context);
			context.buffer.append("CREATE INDEX ON");
			Pretty.writePostClauseSeparator(context);
			IndexCypherWriter.toCypherExpression((IndexExpression)astNode, context);
		}
		
		/*** DROP INDEX CLAUSE **************************************/
		if (clauseType == ClauseType.DROP_INDEX) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context);
			context.buffer.append("DROP INDEX ON");
			Pretty.writePostClauseSeparator(context);
			IndexCypherWriter.toCypherExpression((IndexExpression)astNode, context);
		}
		
		context.previousClause = context.currentClause;
	}
	
	/*****************************************************/
	private static class CollectionCypherWriter {

		private static void toCypherExpression(CollectExpression collectExpression, WriterContext context) {
			//Pretty.writeStatementSeparator(context);
			toCypherExpressionRecursive(collectExpression, context);
		}
		
		private static void toCypherExpression(PredicateFunction pf, WriterContext context) {
			context.incrementLevel();
			Pretty.writePreFunctionSeparator(context);
			if (pf.getType() == PredicateFunctionType.ALL)
				context.buffer.append("ALL(");
			else if (pf.getType() == PredicateFunctionType.ANY)
				context.buffer.append("ANY(");
			else if (pf.getType() == PredicateFunctionType.NONE)
				context.buffer.append("NONE(");
			else if (pf.getType() == PredicateFunctionType.SINGLE)
				context.buffer.append("SINGLE(");
			
			CollectExpression collXpr = pf.getCollectExpression();
			toCypherExpressionRecursive(collXpr, context);
			
			context.decrementLevel();
			context.buffer.append(')');
		}
		
		private static void toCypherSubExpression(CollectExpression collXpr, WriterContext context) {
			context.buffer.append('(');
			toCypherExpression(collXpr, context);
			context.buffer.append(')');
		}
		
		private static void toCypherExpression(CollectionSpec collSpec, WriterContext context) {
			if (collSpec != null) {
				if (collSpec.getCollection() != null) {
					toCypherExpressionRecursive(collSpec.getCollection(), context);
				} else if (collSpec.getJcCollection() != null) {
					ValueWriter.toValueExpression(collSpec.getJcCollection(), context);
				} else if (collSpec.getCollectionValues() != null) {
					context.buffer.append('[');
					int idx = 0;
					for (Object val : collSpec.getCollectionValues()) {
						if (idx > 0)
							context.buffer.append(", ");
						PrimitiveCypherWriter.writePrimitiveValue(val, context);
						idx++;
					}
					context.buffer.append(']');
				}
			}
		}

		private static void toCypherExpressionRecursive(CollectExpression collectExpression, WriterContext context) {
			boolean closeBracket = true;
			if (collectExpression.getType() == CollectXpressionType.EXTRACT)
				context.buffer.append("extract(");
			else if (collectExpression.getType() == CollectXpressionType.FILTER)
				context.buffer.append("filter(");
			else if (collectExpression.getType() == CollectXpressionType.TAIL)
				context.buffer.append("tail(");
			else if (collectExpression.getType() == CollectXpressionType.NODES)
				context.buffer.append("nodes(");
			else if (collectExpression.getType() == CollectXpressionType.RELATIONS)
				context.buffer.append("relationships(");
			else if (collectExpression.getType() == CollectXpressionType.LABELS)
				context.buffer.append("labels(");
			else if (collectExpression.getType() == CollectXpressionType.COLLECT)
				context.buffer.append("collect(");
			else if (collectExpression.getType() == CollectXpressionType.REDUCE)
				context.buffer.append("reduce(");
			else
				closeBracket = false;
			
			if (collectExpression.getEvalExpression() != null)
				toCypherExpression(collectExpression.getEvalExpression(), collectExpression.getIterationVariable(),
						true, context);
			
			if (collectExpression.getIterationVariable() != null) {
				ValueWriter.toValueExpression(collectExpression.getIterationVariable(), context);
				context.buffer.append(" IN ");
			}
			
			CollectionSpec collSpec = collectExpression.getCollectionToOperateOn();
			toCypherExpression(collSpec, context);
			
			if (collectExpression.getType() == CollectXpressionType.EXTRACT ||
					collectExpression.getType() == CollectXpressionType.FOREACH ||
					collectExpression.getType() == CollectXpressionType.REDUCE)
				context.buffer.append(" | ");
			else if (collectExpression.getType() == CollectXpressionType.FILTER ||
					collectExpression.getType() == CollectXpressionType.PREDICATE_FUNCTION)
				context.buffer.append(" WHERE ");
			
			if (collectExpression.getEvalExpression() != null)
				toCypherExpression(collectExpression.getEvalExpression(), collectExpression.getIterationVariable(),
						false, context);
			
			
			if (closeBracket)
				context.buffer.append(')');
		}

		private static void toCypherExpression(EvalExpression evalExpression,
				JcValue jcValue, boolean preCollectionSpec, WriterContext context) {
			if (evalExpression instanceof PropertyEvalExpresssion && !preCollectionSpec) {
				PropertyEvalExpresssion propEval = (PropertyEvalExpresssion)evalExpression;
				if (jcValue != null)
					ValueWriter.toValueExpression(jcValue, context);
				context.buffer.append('.');
				context.buffer.append(propEval.getPropertyName());
			} else if (evalExpression instanceof PredicateEvalExpression && !preCollectionSpec) {
				PredicateEvalExpression pEval = (PredicateEvalExpression)evalExpression;
				PredicateCypherWriter.toCypherExpression(pEval.getPredicateExpression(), context);
			} else if (evalExpression instanceof DoEvalExpression && !preCollectionSpec) {
				DoEvalExpression doEval = (DoEvalExpression)evalExpression;
				toCypherExpression(doEval, context);
			} else if (evalExpression instanceof ReduceEvalExpression && preCollectionSpec) {
				ReduceEvalExpression reduceEval = (ReduceEvalExpression)evalExpression;
				ValueWriter.toValueExpression(reduceEval.getResultVariable(), context);
				context.buffer.append(" = ");
				PredicateCypherWriter.toCypherExpression(reduceEval.getInitialValue(), context);
				context.buffer.append(", ");
			} else if (evalExpression instanceof ReduceEvalExpression && !preCollectionSpec) {
				ReduceEvalExpression reduceEval = (ReduceEvalExpression)evalExpression;
				ValueWriter.toValueExpression(reduceEval.getReduceExpression(), context);
			} else if (evalExpression instanceof ExtractEvalExpression && !preCollectionSpec) {
				ExtractEvalExpression extractEval = (ExtractEvalExpression)evalExpression;
				ValueWriter.toValueExpression(extractEval.getExpression(), context);
			}
		}
		
		private static void toCypherExpression(DoEvalExpression doEval, WriterContext context) {
			int idx = 0;
			boolean inF = context.inFunction;
			context.inFunction = true;
			for (ASTNode astNode : doEval.getClauses()) {
				CypherWriter.toCypherExpression(astNode, idx, context);
				idx++;
			}
			context.inFunction = inF;
		}
	}
	
	/*****************************************************/
	private static class PredicateCypherWriter {

		private static void toCypherExpression(PredicateExpression pxpr, WriterContext context) {
			PredicateConcatenator concat;
			Predicate predicate = pxpr.getPredicate();
			if (predicate != null) {
				PredicateCypherWriter.toCypherExpression(predicate, context);
				while (predicate != null && (concat = predicate.getNext()) != null) {
					PredicateCypherWriter.toCypherExpression(concat, context);
					predicate = concat.getPredicate();
				}
			}
		}
		
		private static void toCypherExpression(PredicateConcatenator concat, WriterContext context) {
			context.buffer.append(' ');
			context.buffer.append(concat.getConcatOperator().name());
			context.buffer.append(' ');
			PredicateCypherWriter.toCypherExpression(concat.getPredicate(), context);
		}
		
		private static void toCypherExpression(Predicate pred, WriterContext context) {
			for (int i = 0; i< pred.getNotCount();i++)
				context.buffer.append("NOT ");
			if (pred instanceof SubExpression) {
				context.buffer.append('(');
				PredicateCypherWriter.toCypherExpression(((SubExpression)pred).getPredicateExpression(), context);
				context.buffer.append(')');
			} else if (pred instanceof ExistsPattern) {
				PatternCypherWriter.toCypherExpression(((ExistsPattern)pred).getPatternExpression(), context);
			} else if (pred instanceof BooleanOp) {
				PredicateCypherWriter.toCypherExpression((BooleanOp)pred, context);
			} else if (pred instanceof PredicateFunction) {
				PredicateFunction pf = (PredicateFunction)pred;
				CollectionCypherWriter.toCypherExpression(pf, context);
			}
		}
		
		private static void toCypherExpression(BooleanOp boolOp, WriterContext context) {
			boolean hasLabel = boolOp.getOperator() == Operator.HAS &&
					boolOp.getOperand1() instanceof JcLabel;
			boolean hasProperty = boolOp.getOperator() == Operator.HAS &&
					boolOp.getOperand1() instanceof JcProperty;
			
			if (hasProperty)
				context.buffer.append("HAS(");
			
			if (boolOp.getOperand1() != null) {
				ValueWriter.toValueExpression(boolOp.getOperand1(), context);
			}
			
			if (!hasLabel && !hasProperty) {
				context.buffer.append(' ');
				if (boolOp.getOperator() == Operator.IN) {
					CollectionSpec collSpec = (CollectionSpec) boolOp.getOperand2();
					context.buffer.append("IN");
					if (collSpec.getCollectionValues() == null)
						context.buffer.append(' ');
					CollectionCypherWriter.toCypherExpression(collSpec, context);
				} else if (boolOp.getOperator() == Operator.IS_NULL) { 
					context.buffer.append("IS NULL");
				} else {
					context.buffer.append(PredicateCypherWriter.getOperatorSymbol(boolOp.getOperator()));
					context.buffer.append(' ');
					PredicateCypherWriter.toCypherExpression(boolOp.getOperand2(), context);
				}
			} else {
				if (hasProperty)
					context.buffer.append(')');
			}
		}
		
		private static String getOperatorSymbol(Operator operator) {
			if (operator == Operator.EQUALS)
				return "=";
			else if (operator == Operator.NOT_EQUALS)
				return "<>";
			else if (operator == Operator.LT)
				return "<";
			else if (operator == Operator.LTE)
				return "<=";
			else if (operator == Operator.GT)
				return ">";
			else if (operator == Operator.GTE)
				return ">=";
			else if (operator == Operator.REGEX)
				return "=~";
			throw new RuntimeException("Operator: " + operator + " not yet implemented");
		}
		
		private static void toCypherExpression(Object valueElement_Or_PrimitiveValue, WriterContext context) {
			if (valueElement_Or_PrimitiveValue instanceof ValueElement)
				ValueWriter.toValueExpression((ValueElement)valueElement_Or_PrimitiveValue, context);
			else if (valueElement_Or_PrimitiveValue != null)
				PrimitiveCypherWriter.writePrimitiveValue(valueElement_Or_PrimitiveValue, context);
		}
	}
	
	/*****************************************************/
	private static class ReturnCypherWriter {

		private static void toCypherExpression(ReturnExpression rx, WriterContext context) {
			if (rx.isCount())
				context.buffer.append("count(");
			if (rx.isDistinct())
				context.buffer.append("DISTINCT ");
			
			ReturnValue re = rx.getReturnValue();
			if (re instanceof ReturnElement) {
				if (((ReturnElement)re).isAll())
					context.buffer.append("*");
				else {
					JcValue jcVal = ((ReturnElement)re).getElement();
					ValueWriter.toValueExpression(jcVal, context);
				}
			} else if (re instanceof ReturnBoolean) {
				PredicateCypherWriter.toCypherExpression(((ReturnBoolean)re).getPredicateExpression(), context);
			} else if (re instanceof ReturnPattern) {
				PatternCypherWriter.toCypherExpression(((ReturnPattern)re).getPatternExpression(), context);
			} else if (re instanceof ReturnCollection) {
				CollectExpression cx = ((ReturnCollection)re).getCollectExpression();
				CollectionCypherWriter.toCypherExpression(cx, context);
			}
			
			if (rx.getAlias() != null) {
				context.buffer.append(" AS ");
				context.buffer.append(rx.getAlias());
			}
			writeFilterExpressions(rx, context);
		}

		private static void writeFilterExpressions(ReturnExpression rx, WriterContext context) {
			if (hasOrderExpressions(rx)) {
				Pretty.writePreClauseSeparator(context);
				context.buffer.append("ORDER BY");
				Pretty.writePostClauseSeparator(context);
				int idx = 0;
				for (Order order : rx.getOrders()) {
					if (idx > 0) {
						context.buffer.append(',');
						Pretty.writeStatementSeparator(context);
					}
					JcValue elem = ((ReturnElement)rx.getReturnValue()).getElement();
					ValueWriter.toValueExpression(
							ValueAccess.findFirst(elem), context);
					context.buffer.append('.');
					context.buffer.append(order.getPropertyName());
					if (order.isDescending())
						context.buffer.append(" DESC");
					idx++;
				}
			}
			
			int limit = rx.getLimit();
			if (limit != -1) {
				Pretty.writePreClauseSeparator(context);
				context.buffer.append("LIMIT ");
				context.buffer.append(limit);
			}
			
			int skip = rx.getSkip();
			if (skip != -1) {
				Pretty.writePreClauseSeparator(context);
				context.buffer.append("SKIP ");
				context.buffer.append(skip);
			}
		}

		private static boolean hasOrderExpressions(ReturnExpression rx) {
			return rx.getOrders() != null && rx.getOrders().size() > 0;
		}
	}	
	
	/*****************************************************/
	private static class IndexCypherWriter {

		private static void toCypherExpression(IndexExpression ix, WriterContext context) {
			context.buffer.append(':');
			context.buffer.append(ix.getLabelName());
			context.buffer.append('(');
			context.buffer.append(ix.getPropertyName());
			context.buffer.append(')');
		}
	}
	
	/*****************************************************/
	private static class STCypherWriter {

		private static void toCypherExpression(ModifyExpression mx, WriterContext context) {
			if (mx.getModifyAction() == ModifyAction.SET ||
					mx.getModifyAction() == ModifyAction.REMOVE) {
				if (mx.getToModify() != null) {
					ValueWriter.toValueExpression(mx.getToModify(), context);
					if (mx.getModifyAction() == ModifyAction.SET)
						context.buffer.append(" = ");
					if (mx.getValue() != null)
						PrimitiveCypherWriter.writePrimitiveValue(mx.getValue(), context);
					else if (mx.getValueExpression() != null)
						ValueWriter.toValueExpression(mx.getValueExpression(), context);
					else if (mx.isToNull())
						context.buffer.append("NULL");
				} else if (mx.getPropertiesCopy() != null) {
					PropertiesCopy pc = mx.getPropertiesCopy();
					ValueWriter.toValueExpression(pc.getTarget(), context);
					context.buffer.append(" = ");
					ValueWriter.toValueExpression(pc.getSource(), context);
				} else if (mx.getModifyLabels() != null) {
					ValueWriter.toValueExpression(mx.getModifyLabels().getTargetNode(), context);
					for (String label : mx.getModifyLabels().getLabels()) {
						context.buffer.append(':');
						context.buffer.append(label);
					}
				}
			} else if (mx.getModifyAction() == ModifyAction.DELETE) {
				ValueWriter.toValueExpression(mx.getElementToDelete(), context);
			}
		}
	}
	
	/*****************************************************/
	private static class NativeCypherWriter {

		private static void toCypherExpression(NativeCypherExpression ncxpr, WriterContext context) {
			int idx = 0;
			for(String line : ncxpr.getLines()) {
				if (idx > 0)
					context.buffer.append("\n");
				context.buffer.append(line);
				idx++;
			}
		}
	}
	
	/*****************************************************/
	private static class PatternCypherWriter {

		private static void toCypherExpression(PatternExpression xpr, WriterContext context) {
			boolean bracketClose = false;
			if (xpr.getPath() != null) {
				bracketClose = PatternCypherWriter.toCypherExpression(xpr.getPath(), context);
			}
			
			for (PatternElement elem : xpr.getElements()) {
				PatternCypherWriter.toCypherExpression(elem, context);
			}

			if (bracketClose)
				context.buffer.append(')');
		}
		
		/**
		 * @param path
		 * @param context
		 * @return true, if a closing bracket should be written
		 */
		private static boolean toCypherExpression(PatternPath path, WriterContext context) {
			boolean bracketClose = true;
			ValueWriter.toValueExpression(path.getJcPath(), context);
			context.buffer.append(" = ");
			if (path.getPathFunction() == PathFunction.PATH) {
				bracketClose = false;
			} else if (path.getPathFunction() == PathFunction.SHORTEST_PATH) {
				context.buffer.append("shortestPath(");
			} else if (path.getPathFunction() == PathFunction.ALL_SHORTEST_PATHS) {
				context.buffer.append("allShortestPaths(");
			}
			
			return bracketClose;
		}
		
		private static void toCypherExpression(PatternProperty property, WriterContext context) {
			context.buffer.append(property.getName());
			context.buffer.append(':');
			if (property.getValue() instanceof ValueElement) {
				context.buffer.append(' ');
				ValueWriter.toValueExpression((ValueElement)property.getValue(), context);
			} else if (property.getValue() != null)
				PrimitiveCypherWriter.writePrimitiveValue(property.getValue(), context);
		}
		
		private static void toCypherExpression(PatternElement element, WriterContext context) {
			if (element instanceof PatternNode) {
				PatternNode n = (PatternNode)element;
				context.buffer.append('(');
				if (n.getJcElement() != null)
					ValueWriter.toValueExpression(n.getJcElement(), context);
				for (String label : n.getLabels()) {
					context.buffer.append(':');
					context.buffer.append(label);
				}
				PatternCypherWriter.appendProperties(n, context);
				context.buffer.append(')');
			} else if (element instanceof PatternRelation) {
				PatternRelation r = (PatternRelation)element;
				if (r.getDirection() == Direction.IN)
					context.buffer.append('<');
				context.buffer.append('-');
				
				boolean hasContent = r.getJcElement() != null || r.getTypes().size() > 0 ||
						r.getMinHops() != 1 || r.getMaxHops() != 1 || r.hasProperties();
				if (hasContent)
					context.buffer.append('[');
				
				if (r.getJcElement() != null)
					ValueWriter.toValueExpression(r.getJcElement(), context);
				int idx = 0;
				for (String type : r.getTypes()) {
					if (idx > 0)
						context.buffer.append('|');
					context.buffer.append(':');
					context.buffer.append(type);
					idx++;
				}
				
				if (r.getMinHops() == 0 && r.getMaxHops() == -1) // hops unbound
					context.buffer.append('*');
				else if (r.getMinHops() == 0) {
					context.buffer.append("*..");
					context.buffer.append(r.getMaxHops());
				} else if (r.getMaxHops() == -1) {
					context.buffer.append('*');
					context.buffer.append(r.getMinHops());
					context.buffer.append("..");
				} else if (r.getMinHops() != 1 || r.getMaxHops() != 1) {
					context.buffer.append('*');
					context.buffer.append(r.getMinHops());
					context.buffer.append("..");
					context.buffer.append(r.getMaxHops());
				}
				
				PatternCypherWriter.appendProperties(r, context);
				
				if (hasContent)
					context.buffer.append(']');
				context.buffer.append('-');
				if (r.getDirection() == Direction.OUT)
					context.buffer.append('>');
			}
		}
		
		private static void appendProperties(PatternElement element, WriterContext context) {
			if (element.getProperties().size() > 0) {
				context.buffer.append('{');
				int idx = 0;
				for (PatternProperty property : element.getProperties()) {
					if (idx > 0)
						context.buffer.append(", ");
					PatternCypherWriter.toCypherExpression(property, context);
					idx++;
				}
				context.buffer.append('}');
			}
		}
	}
	
	/*****************************************************/
	private static class StartCypherWriter {

		private static void toCypherExpression(StartExpression sx, WriterContext context) {
			JcElement jcElem = sx.getJcElement();
			ValueWriter.toValueExpression(jcElem, context);
			context.buffer.append(" = ");
			if (jcElem instanceof JcNode)
				context.buffer.append("node");
			else
				context.buffer.append("relationship");
			if (sx.isAll()) {
				context.buffer.append("(*)");
			} else if (sx.getIndexOrId().getIndexName() != null) {
				context.buffer.append(':');
				context.buffer.append(sx.getIndexOrId().getIndexName());
				context.buffer.append('(');
				PropertyOrQuery poq = sx.getPropertyOrQuery();
				if (poq.getLuceneQuery() != null) {
					context.buffer.append('\"');
					context.buffer.append(poq.getLuceneQuery());
					context.buffer.append('\"');
				}
				else if (poq.getPropertyValue() != null) {
					context.buffer.append(poq.getPropertyName());
					context.buffer.append(" = ");
					PrimitiveCypherWriter.writePrimitiveValue(poq.getPropertyValue(), context);
				}
				context.buffer.append(')');
			} else if (sx.getIndexOrId().getIds() != null) {
				context.buffer.append('(');
				boolean first = true;
				for (Long id : sx.getIndexOrId().getIds()) {
					if (!first)
						context.buffer.append(", ");
					context.buffer.append(id.toString());
					first = false;
				}
				context.buffer.append(')');
			}
		}
	}
	
	/*****************************************************/
	private static class UCypherWriter {

		private static void toCypherExpression(UsingExpression ux, WriterContext context) {
			JcValue vr = ux.getValueRef();
			if (vr instanceof JcNode) // index scan
				context.buffer.append(ValueAccess.getName(vr));
			else if (vr instanceof JcProperty) // label scan
				context.buffer.append(ValueAccess.getName((JcValue)ValueAccess.getPredecessor(vr)));
			context.buffer.append(':');
			context.buffer.append(ux.getIndexLabel());
			if (vr instanceof JcProperty) {
				context.buffer.append('(');
				context.buffer.append(ValueAccess.getName(vr));
				context.buffer.append(')');
			}
		}
	}
	
	/*****************************************************/
	public static class PrimitiveCypherWriter {

		public static void writePrimitiveValue(Object val, WriterContext context) {
			if (val instanceof String) {
				context.buffer.append('\'');
				context.buffer.append(val.toString());
				context.buffer.append('\'');
			} else {
				context.buffer.append(val.toString());
			}
		}
	}
}
