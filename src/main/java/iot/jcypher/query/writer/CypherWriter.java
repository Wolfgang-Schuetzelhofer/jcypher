/************************************************************************
 * Copyright (c) 2014-2015 IoT-Solutions e.U.
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

package iot.jcypher.query.writer;

import iot.jcypher.database.DBVersion;
import iot.jcypher.domainquery.internal.Settings;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.api.APIObjectAccess;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.ast.ASTNode;
import iot.jcypher.query.ast.ClauseType;
import iot.jcypher.query.ast.cases.CaseExpression;
import iot.jcypher.query.ast.cases.CaseExpression.WhenJcValue;
import iot.jcypher.query.ast.collection.CollectExpression;
import iot.jcypher.query.ast.collection.CollectExpression.CollectXpressionType;
import iot.jcypher.query.ast.collection.CollectionSpec;
import iot.jcypher.query.ast.collection.DoEvalExpression;
import iot.jcypher.query.ast.collection.EvalExpression;
import iot.jcypher.query.ast.collection.ExtractEvalExpression;
import iot.jcypher.query.ast.collection.PredicateEvalExpression;
import iot.jcypher.query.ast.collection.PropertyEvalExpresssion;
import iot.jcypher.query.ast.collection.ReduceEvalExpression;
import iot.jcypher.query.ast.index.IndexExpression;
import iot.jcypher.query.ast.modify.ModifyExpression;
import iot.jcypher.query.ast.modify.ModifyExpression.ModifyAction;
import iot.jcypher.query.ast.modify.PropertiesCopy;
import iot.jcypher.query.ast.nativ.NativeCypherExpression;
import iot.jcypher.query.ast.pattern.PatternElement;
import iot.jcypher.query.ast.pattern.PatternExpression;
import iot.jcypher.query.ast.pattern.PatternNode;
import iot.jcypher.query.ast.pattern.PatternPath;
import iot.jcypher.query.ast.pattern.PatternPath.PathFunction;
import iot.jcypher.query.ast.pattern.PatternProperty;
import iot.jcypher.query.ast.pattern.PatternRelation;
import iot.jcypher.query.ast.pattern.PatternRelation.Direction;
import iot.jcypher.query.ast.predicate.BooleanOp;
import iot.jcypher.query.ast.predicate.BooleanOp.Operator;
import iot.jcypher.query.ast.predicate.BooleanValue;
import iot.jcypher.query.ast.predicate.ExistsPattern;
import iot.jcypher.query.ast.predicate.Predicate;
import iot.jcypher.query.ast.predicate.PredicateConcatenator;
import iot.jcypher.query.ast.predicate.PredicateExpression;
import iot.jcypher.query.ast.predicate.PredicateFunction;
import iot.jcypher.query.ast.predicate.PredicateFunction.PredicateFunctionType;
import iot.jcypher.query.ast.predicate.SubExpression;
import iot.jcypher.query.ast.returns.Order;
import iot.jcypher.query.ast.returns.ReturnAggregate;
import iot.jcypher.query.ast.returns.ReturnAggregate.AggregateFunctionType;
import iot.jcypher.query.ast.returns.ReturnBoolean;
import iot.jcypher.query.ast.returns.ReturnCollection;
import iot.jcypher.query.ast.returns.ReturnElement;
import iot.jcypher.query.ast.returns.ReturnExpression;
import iot.jcypher.query.ast.returns.ReturnPattern;
import iot.jcypher.query.ast.returns.ReturnValue;
import iot.jcypher.query.ast.start.PropertyOrQuery;
import iot.jcypher.query.ast.start.StartExpression;
import iot.jcypher.query.ast.union.UnionExpression;
import iot.jcypher.query.ast.using.UsingExpression;
import iot.jcypher.query.values.JcElement;
import iot.jcypher.query.values.JcLabel;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcProperty;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.ValueElement;
import iot.jcypher.query.values.ValueWriter;

import java.util.List;

public class CypherWriter {

	// currently if you use parameters in a cypher query, and a parameter is a list,
	// this does not work properly. Instead of storing a list as property value,
	// everything is converted to a string.
	// The workaround is not to use paramaters with cypher queries and lists.
	// This is true for NEO4J Versions at least up to 2.1.4
	// If this is corrected in NEO4J some time to come, you simply in class CypherWriter
	// can set CORRECT_FOR_LIST_WITH_PARAMS to false and the workaround
	// will be removed
	private static final boolean CORRECT_FOR_LIST_WITH_PARAMS = true;
	private static final String dBVersion_21x = "2.1.x";
	
	public static void toCypherExpression(JcQuery query, WriterContext context) {
		if (!DBVersion.Neo4j_Version.equals(dBVersion_21x)) {
			context.buffer.append("CYPHER planner=");
			context.buffer.append(Settings.plannerStrategy.name().toLowerCase());
			Pretty.writePreClauseSeparator(context, context.buffer);
		}
		toCypherExpression(query.getClauses(), 0, context);
	}
	
	public static void toCypherExpression(IClause[] clauses, int index, WriterContext context) {
		int idx = index;
		for (IClause clause : clauses) {
			CypherWriter.toCypherExpression(clause, idx, context);
			idx++;
		}
		addFilterExpressionsIfNeeded(context, true);
	}
	
	public static void toCypherExpression(IClause clause, int index, WriterContext context) {
		toCypherExpression(APIObjectAccess.getAstNode((APIObject) clause), index, context);
	}

	private static void toCypherExpression(ASTNode astNode, int index, WriterContext context) {
		boolean hasStart = index > 0;
		ClauseType clauseType = astNode.getClauseType();
		context.currentClause = clauseType;
		addFilterExpressionsIfNeeded(context, false);
		
		/*** CYPHER NATIVE CLAUSE **************************************/
		if (clauseType == ClauseType.CYPHER_NATIVE) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			NativeCypherWriter.toCypherExpression((NativeCypherExpression)astNode, context);
		}
		
		/*** START CLAUSE **************************************/
		else if (clauseType == ClauseType.START) {
			if (context.previousClause != ClauseType.START) { // otherwise concat multiple starts
				if (hasStart)
					Pretty.writePreClauseSeparator(context, context.buffer);
				context.buffer.append("START");
				Pretty.writePostClauseSeparator(context, context.buffer);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context, context.buffer);
			}
			StartCypherWriter.toCypherExpression((StartExpression)astNode, context);
		}
		
		/*** UNION CLAUSE, UNION ALL CLAUSE **************************************/
		else if (clauseType == ClauseType.UNION || clauseType == ClauseType.UNION_ALL) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			UnionExpression ux = (UnionExpression)astNode;
			if (ux.isDistinct())
				context.buffer.append("UNION");
			else
				context.buffer.append("UNION ALL");
			Pretty.writePostClauseSeparator(context, context.buffer);
		}
		
		/*** WITH CLAUSE **************************************/
		else if (clauseType == ClauseType.WITH) {
			if (context.previousClause != ClauseType.WITH) { // otherwise concat multiple withs
				if (hasStart)
					Pretty.writePreClauseSeparator(context, context.buffer);
				context.buffer.append("WITH");
				Pretty.writePostClauseSeparator(context, context.buffer);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context, context.buffer);
			}
			ReturnCypherWriter.toCypherExpression((ReturnExpression)astNode, context);
		}
		
		/*** MATCH CLAUSE **************************************/
		else if (clauseType == ClauseType.MATCH) {
			if (context.previousClause != ClauseType.MATCH) { // otherwise concat multiple matches
				if (hasStart)
					Pretty.writePreClauseSeparator(context, context.buffer);
				context.buffer.append("MATCH");
				Pretty.writePostClauseSeparator(context, context.buffer);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context, context.buffer);
			}
			PatternCypherWriter.toCypherExpression((PatternExpression)astNode, context);
		}
		
		/*** OPTIONAL MATCH CLAUSE **************************************/
		else if (clauseType == ClauseType.OPTIONAL_MATCH) {
			if (context.previousClause != ClauseType.OPTIONAL_MATCH) { // otherwise concat multiple matches
				if (hasStart)
					Pretty.writePreClauseSeparator(context, context.buffer);
				context.buffer.append("OPTIONAL MATCH");
				Pretty.writePostClauseSeparator(context, context.buffer);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context, context.buffer);
			}
			PatternCypherWriter.toCypherExpression((PatternExpression)astNode, context);
		}
		
		/*** USING INDEX CLAUSE **************************************/
		else if (clauseType == ClauseType.USING_INDEX) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append("USING INDEX");
			Pretty.writePostClauseSeparator(context, context.buffer);
			UCypherWriter.toCypherExpression((UsingExpression)astNode, context);
		}
		
		/*** USING SCAN CLAUSE **************************************/
		else if (clauseType == ClauseType.USING_SCAN) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append("USING SCAN");
			Pretty.writePostClauseSeparator(context, context.buffer);
			UCypherWriter.toCypherExpression((UsingExpression)astNode, context);
		}
		
		/*** WHERE CLAUSE **************************************/
		else if (clauseType == ClauseType.WHERE) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append("WHERE");
			Pretty.writePostClauseSeparator(context, context.buffer);
			PredicateCypherWriter.toCypherExpression((PredicateExpression)astNode, context);
		}
		
		/*** CREATE CLAUSE **************************************/
		else if (clauseType == ClauseType.CREATE) {
			if (context.previousClause != ClauseType.CREATE) { // otherwise concat multiple creates
				if (hasStart)
					Pretty.writePreClauseSeparator(context, context.buffer);
				context.buffer.append("CREATE");
				Pretty.writePostClauseSeparator(context, context.buffer);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context, context.buffer);
			}
			PatternCypherWriter.toCypherExpression((PatternExpression)astNode, context);
		}
		
		/*** CREATE UNIQUE CLAUSE **************************************/
		else if (clauseType == ClauseType.CREATE_UNIQUE) {
			if (context.previousClause != ClauseType.CREATE_UNIQUE) { // otherwise concat multiple creates
				if (hasStart)
					Pretty.writePreClauseSeparator(context, context.buffer);
				context.buffer.append("CREATE UNIQUE");
				Pretty.writePostClauseSeparator(context, context.buffer);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context, context.buffer);
			}
			PatternCypherWriter.toCypherExpression((PatternExpression)astNode, context);
		}
		
		/*** RETURN CLAUSE **************************************/
		else if (clauseType == ClauseType.RETURN) {
			if (context.previousClause != ClauseType.RETURN) { // otherwise concat multiple returns
				if (hasStart)
					Pretty.writePreClauseSeparator(context, context.buffer);
				context.buffer.append("RETURN");
				Pretty.writePostClauseSeparator(context, context.buffer);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context, context.buffer);
			}
			ReturnCypherWriter.toCypherExpression((ReturnExpression)astNode, context);
		}
		
		/*** SET CLAUSE **************************************/
		else if (clauseType == ClauseType.SET) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append("SET");
			Pretty.writePostClauseSeparator(context, context.buffer);
			STCypherWriter.toCypherExpression((ModifyExpression)astNode, context);
		}
		
		/*** DELETE CLAUSE **************************************/
		else if (clauseType == ClauseType.DELETE) {
			if (context.previousClause != ClauseType.DELETE) { // otherwise concat multiple deletes
				if (hasStart)
					Pretty.writePreClauseSeparator(context, context.buffer);
				context.buffer.append("DELETE");
				Pretty.writePostClauseSeparator(context, context.buffer);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context, context.buffer);
			}
			STCypherWriter.toCypherExpression((ModifyExpression)astNode, context);
		}
		
		/*** REMOVE CLAUSE **************************************/
		else if (clauseType == ClauseType.REMOVE) {
			if (context.previousClause != ClauseType.REMOVE) { // otherwise concat multiple removes
				if (hasStart)
					Pretty.writePreClauseSeparator(context, context.buffer);
				context.buffer.append("REMOVE");
				Pretty.writePostClauseSeparator(context, context.buffer);
			} else {
				context.buffer.append(',');
				Pretty.writeStatementSeparator(context, context.buffer);
			}
			STCypherWriter.toCypherExpression((ModifyExpression)astNode, context);
		}
		
		/*** FOREACH CLAUSE **************************************/
		else if (clauseType == ClauseType.FOREACH) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append("FOREACH");
			Pretty.writePostClauseSeparator(context, context.buffer);
			CollectionCypherWriter.toCypherSubExpression((CollectExpression)astNode, context);
		}
		
		/*** CREATE INDEX CLAUSE **************************************/
		else if (clauseType == ClauseType.CREATE_INDEX) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append("CREATE INDEX ON");
			Pretty.writePostClauseSeparator(context, context.buffer);
			IndexCypherWriter.toCypherExpression((IndexExpression)astNode, context);
		}
		
		/*** DROP INDEX CLAUSE **************************************/
		else if (clauseType == ClauseType.DROP_INDEX) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append("DROP INDEX ON");
			Pretty.writePostClauseSeparator(context, context.buffer);
			IndexCypherWriter.toCypherExpression((IndexExpression)astNode, context);
		}
		
		/*** CASE CLAUSE **************************************/
		else if (clauseType == ClauseType.CASE) {
			if (context.previousClause == ClauseType.RETURN)
				context.buffer.append(',');
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append("CASE");
			Pretty.writePostClauseSeparator(context, context.buffer);
			CaseCypherWriter.toCaseExpression((CaseExpression)astNode, context);
		}
		
		/*** WHEN CLAUSE **************************************/
		else if (clauseType == ClauseType.WHEN) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append("WHEN");
			Pretty.writePostClauseSeparator(context, context.buffer);
			CaseCypherWriter.toWhenExpression((PredicateExpression)astNode, context);
			Pretty.writeStatementSeparator(context, context.buffer);
			context.buffer.append("THEN");
		}
		
		/*** ELSE CLAUSE **************************************/
		else if (clauseType == ClauseType.ELSE) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append(clauseType.name());
			//Pretty.writePostClauseSeparator(context, context.buffer);
		}
		
		/*** END CLAUSE **************************************/
		else if (clauseType == ClauseType.END) {
			if (hasStart)
				Pretty.writePreClauseSeparator(context, context.buffer);
			context.buffer.append(clauseType.name());
			Pretty.writePostClauseSeparator(context, context.buffer);
			CaseCypherWriter.toEndExpression((CaseExpression)astNode, context);
		}
		
		context.previousClause = context.currentClause;
	}
	
	private static void addFilterExpressionsIfNeeded(WriterContext context, boolean end) {
		if (context.filterBuffer != null) {
			if ((context.currentClause != ClauseType.WITH && context.previousClause == ClauseType.WITH) ||
					(context.currentClause != ClauseType.RETURN && context.previousClause == ClauseType.RETURN) ||
					end) {
				context.buffer.append(context.filterBuffer);
				context.filterBuffer = null;
			}
		}
	}
	
	/*****************************************************/
	private static class CollectionCypherWriter {

		private static void toCypherExpression(CollectExpression collectExpression, WriterContext context) {
			//Pretty.writeStatementSeparator(context, context.buffer);
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
					ValueWriter.toValueExpression(collSpec.getJcCollection(), context, context.buffer);
				} else if (collSpec.getCollectionValues() != null) {
					context.buffer.append('[');
					int idx = 0;
					for (Object val : collSpec.getCollectionValues()) {
						if (idx > 0)
							context.buffer.append(", ");
						PrimitiveCypherWriter.writePrimitiveValue(val, context, context.buffer);
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
				ValueWriter.toValueExpression(collectExpression.getIterationVariable(), context, context.buffer);
				context.buffer.append(" IN ");
			}
			
			if (collectExpression.getType() == CollectXpressionType.CREATE &&
					collectExpression.getNestedClauses() != null) {
				CollectionCypherWriter.writeInnerClauses(collectExpression.getNestedClauses(), context);
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
			
			if (collectExpression.getNestedClauses() != null &&
					collectExpression.getType() == CollectXpressionType.FOREACH) {
				CollectionCypherWriter.writeInnerClauses(collectExpression.getNestedClauses(), context);
			}
			
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
					ValueWriter.toValueExpression(jcValue, context, context.buffer);
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
				ValueWriter.toValueExpression(reduceEval.getResultVariable(), context, context.buffer);
				context.buffer.append(" = ");
				PredicateCypherWriter.toCypherExpression(reduceEval.getInitialValue(), context);
				context.buffer.append(", ");
			} else if (evalExpression instanceof ReduceEvalExpression && !preCollectionSpec) {
				ReduceEvalExpression reduceEval = (ReduceEvalExpression)evalExpression;
				ValueWriter.toValueExpression(reduceEval.getReduceExpression(), context, context.buffer);
			} else if (evalExpression instanceof ExtractEvalExpression && !preCollectionSpec) {
				ExtractEvalExpression extractEval = (ExtractEvalExpression)evalExpression;
				ValueWriter.toValueExpression(extractEval.getExpression(), context, context.buffer);
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
		
		private static void writeInnerClauses(IClause[] innerClauses, WriterContext context) {
			Format orgFormat = context.cypherFormat;
			ClauseType curClause = context.currentClause;
			ClauseType prevClause = context.previousClause;
			context.cypherFormat = Format.NONE;
			context.previousClause = null;
			context.currentClause = null;
			CypherWriter.toCypherExpression(innerClauses, 0, context);
			context.cypherFormat = orgFormat;
			context.previousClause = prevClause;
			context.currentClause = curClause;
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
			} else if (pred instanceof BooleanValue) {
				PrimitiveCypherWriter.writePrimitiveValue(((BooleanValue)pred).isTRUE() ? Boolean.TRUE : Boolean.FALSE,
						context, context.buffer);
			}
		}
		
		private static void toCypherExpression(BooleanOp boolOp, WriterContext context) {
			boolean hasLabel = boolOp.getOperator() == Operator.HAS &&
					boolOp.getOperand1() instanceof JcLabel;
			boolean hasProperty = boolOp.getOperator() == Operator.HAS &&
					boolOp.getOperand1() instanceof JcProperty;
			boolean isEmptyWhenValue = boolOp.getOperand1() instanceof WhenJcValue;
			
			if (hasProperty)
				context.buffer.append("HAS(");
			
			if (boolOp.getOperand1() != null) {
				ValueWriter.toValueExpression(boolOp.getOperand1(), context, context.buffer);
			}
			
			if (!hasLabel && !hasProperty) {
				if (!isEmptyWhenValue) {
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
				} else
					PredicateCypherWriter.toCypherExpression(boolOp.getOperand2(), context);
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
				ValueWriter.toValueExpression((ValueElement)valueElement_Or_PrimitiveValue, context, context.buffer);
			else if (valueElement_Or_PrimitiveValue != null) {
				if (QueryParam.isExtractParams(context)) {
					QueryParam qp = QueryParam.createAddParam(null,
							valueElement_Or_PrimitiveValue, context);
					PrimitiveCypherWriter.writeParameter(qp, context.buffer);
				} else
					PrimitiveCypherWriter.writePrimitiveValue(valueElement_Or_PrimitiveValue, context, context.buffer);
			}
		}
	}
	
	/*****************************************************/
	private static class ReturnCypherWriter {

		private static void toCypherExpression(ReturnExpression rx, WriterContext context) {
			boolean closeBracket = false;
			if (rx.isCount()) {
				context.buffer.append("count(");
				closeBracket = true;
			}
			if (rx.isDistinct())
				context.buffer.append("DISTINCT ");
			
			ReturnValue re = rx.getReturnValue();
			if (re instanceof ReturnElement) {
				if (((ReturnElement)re).isAll())
					context.buffer.append("*");
				else {
					JcValue jcVal = ((ReturnElement)re).getElement();
					ValueWriter.toValueExpression(jcVal, context, context.buffer);
				}
			} else if (re instanceof ReturnBoolean) {
				PredicateCypherWriter.toCypherExpression(((ReturnBoolean)re).getPredicateExpression(), context);
			} else if (re instanceof ReturnPattern) {
				PatternCypherWriter.toCypherExpression(((ReturnPattern)re).getPatternExpression(), context);
			} else if (re instanceof ReturnCollection) {
				CollectExpression cx = ((ReturnCollection)re).getCollectExpression();
				CollectionCypherWriter.toCypherExpression(cx, context);
			} else if (re instanceof ReturnAggregate) {
				ReturnAggregate ra = (ReturnAggregate)re;
				toCypherExpression(ra, context);
			}
			
			if (closeBracket)
				context.buffer.append(')');
			
			if (rx.getAlias() != null) {
				context.buffer.append(" AS ");
				ValueWriter.toValueExpression(rx.getAlias(), context, context.buffer);
			}
			writeFilterExpressions(rx, context);
		}

		private static void toCypherExpression(ReturnAggregate ra,
				WriterContext context) {
			AggregateFunctionType type = ra.getType();
			if (type == AggregateFunctionType.SUM)
				context.buffer.append("sum(");
			else if (type == AggregateFunctionType.AVG)
				context.buffer.append("avg(");
			else if (type == AggregateFunctionType.PERCENTILE_DISC)
				context.buffer.append("percentileDisc(");
			else if (type == AggregateFunctionType.PERCENTILE_CONT)
				context.buffer.append("percentileCont(");
			else if (type == AggregateFunctionType.STDEV)
				context.buffer.append("stdev(");
			else if (type == AggregateFunctionType.STDEVP)
				context.buffer.append("stdevp(");
			else if (type == AggregateFunctionType.MAX)
				context.buffer.append("max(");
			else if (type == AggregateFunctionType.MIN)
				context.buffer.append("min(");
			
			if (ra.isDistinct())
				context.buffer.append("DISTINCT ");
			ValueWriter.toValueExpression(ra.getArgument(), context, context.buffer);
			
			if (type == AggregateFunctionType.PERCENTILE_DISC) {
				context.buffer.append(", ");
				context.buffer.append(ra.getPercentile());
			} else if (type == AggregateFunctionType.PERCENTILE_CONT) {
				context.buffer.append(", ");
				context.buffer.append(ra.getPercentile());
			}
			
			context.buffer.append(')');
		}

		private static void writeFilterExpressions(ReturnExpression rx, WriterContext context) {
			if (hasOrderExpressions(rx)) {
				int idx = 0;
				if (context.filterBuffer == null) {
					context.filterBuffer = new StringBuilder();
					Pretty.writePreClauseSeparator(context, context.filterBuffer);
					context.filterBuffer.append("ORDER BY");
					Pretty.writePostClauseSeparator(context, context.filterBuffer);
				} else
					idx++;
				for (Order order : rx.getOrders()) {
					if (idx > 0) {
						context.filterBuffer.append(',');
						Pretty.writeStatementSeparator(context, context.filterBuffer);
					}
					JcValue elem = ((ReturnElement)rx.getReturnValue()).getElement();
					ValueWriter.toValueExpression(
							ValueAccess.findFirst(elem), context, context.filterBuffer);
					context.filterBuffer.append('.');
					context.filterBuffer.append(order.getPropertyName());
					if (order.isDescending())
						context.filterBuffer.append(" DESC");
					idx++;
				}
			}
			
			int skip = rx.getSkip();
			if (skip != -1) {
				if (context.filterBuffer == null)
					context.filterBuffer = new StringBuilder();
				Pretty.writePreClauseSeparator(context, context.filterBuffer);
				context.filterBuffer.append("SKIP ");
				if (QueryParam.isExtractParams(context)) {
					QueryParam qp = QueryParam.createAddParam(null,
							skip, context);
					PrimitiveCypherWriter.writeParameter(qp, context.filterBuffer);
				} else
					context.filterBuffer.append(skip);
			}
			
			int limit = rx.getLimit();
			if (limit != -1) {
				if (context.filterBuffer == null)
					context.filterBuffer = new StringBuilder();
				Pretty.writePreClauseSeparator(context, context.filterBuffer);
				context.filterBuffer.append("LIMIT ");
				if (QueryParam.isExtractParams(context)) {
					QueryParam qp = QueryParam.createAddParam(null,
							limit, context);
					PrimitiveCypherWriter.writeParameter(qp, context.filterBuffer);
				} else
					context.filterBuffer.append(limit);
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
					ValueWriter.toValueExpression(mx.getToModify(), context, context.buffer);
					if (mx.getModifyAction() == ModifyAction.SET)
						context.buffer.append(" = ");
					if (mx.getValue() != null) { // only in case of SET
						if (QueryParam.isExtractParams(context) && (!CORRECT_FOR_LIST_WITH_PARAMS ||
								!(mx.getValue() instanceof List<?>))) {
							QueryParam qp = QueryParam.createAddParam(null,
									mx.getValue(), context);
							PrimitiveCypherWriter.writeParameter(qp, context.buffer);
						} else
							PrimitiveCypherWriter.writePrimitiveValue(mx.getValue(), context, context.buffer);
					} else if (mx.getValueExpression() != null)
						ValueWriter.toValueExpression(mx.getValueExpression(), context, context.buffer);
					else if (mx.isToNull())
						context.buffer.append("NULL");
				} else if (mx.getPropertiesCopy() != null) {
					PropertiesCopy pc = mx.getPropertiesCopy();
					ValueWriter.toValueExpression(pc.getTarget(), context, context.buffer);
					context.buffer.append(" = ");
					ValueWriter.toValueExpression(pc.getSource(), context, context.buffer);
				} else if (mx.getModifyLabels() != null) {
					ValueWriter.toValueExpression(mx.getModifyLabels().getTargetNode(), context, context.buffer);
					for (String label : mx.getModifyLabels().getLabels()) {
						context.buffer.append(':');
						context.buffer.append(label);
					}
				}
			} else if (mx.getModifyAction() == ModifyAction.DELETE) {
				ValueWriter.toValueExpression(mx.getElementToDelete(), context, context.buffer);
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
			ValueWriter.toValueExpression(path.getJcPath(), context, context.buffer);
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
				QueryParamSet.disableUseSet(context);
				context.buffer.append(' ');
				ValueWriter.toValueExpression((ValueElement)property.getValue(), context, context.buffer);
			} else if (property.getValue() != null) {
				// TODO remove later (test with future versions of Neo4J)
				// workaround for bug: using lists with query parameters
				// they are mapped to strings instead of being stored as lists (Neo4J Version 2.1.4)
				boolean disableParamSet = property.getValue() instanceof List<?> &&
						CORRECT_FOR_LIST_WITH_PARAMS;
				if (disableParamSet)
					QueryParamSet.disableUseSet(context);
				if (QueryParam.isExtractParams(context) && (!CORRECT_FOR_LIST_WITH_PARAMS ||
						!(property.getValue() instanceof List<?>))) {
					QueryParam qp = QueryParam.createParam(property.getName(), property.getValue(), context);
					QueryParamSet.addQueryParam(qp, context);
					PrimitiveCypherWriter.writeParameter(qp, context.buffer);
 				} else
					PrimitiveCypherWriter.writePrimitiveValue(property.getValue(), context, context.buffer);
			}
		}
		
		private static void toCypherExpression(PatternElement element, WriterContext context) {
			if (element instanceof PatternNode) {
				PatternNode n = (PatternNode)element;
				context.buffer.append('(');
				if (n.getJcElement() != null)
					ValueWriter.toValueExpression(n.getJcElement(), context, context.buffer);
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
					ValueWriter.toValueExpression(r.getJcElement(), context, context.buffer);
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
				StringBuilder buf = context.buffer;
				context.buffer = new StringBuilder();
				int idx = 0;
				QueryParamSet.createAddParamSet(context);
				int paramIdx = QueryParam.getParamIndex(context);
				for (PatternProperty property : element.getProperties()) {
					if (idx > 0)
						context.buffer.append(", ");
					PatternCypherWriter.toCypherExpression(property, context);
					idx++;
				}
				
				if (QueryParam.isExtractParams(context) && QueryParamSet.canUseSet(context) &&
						QueryParamSet.getCurrentSet(context).getQueryParams().size() > 1) {
					QueryParam.setParamIndex(paramIdx, context);
					context.buffer = buf;
					PrimitiveCypherWriter.writeParameterSet(QueryParamSet.getCurrentSet(context), context);
				} else {
					buf.append(context.buffer);
					context.buffer = buf;
				}
				QueryParamSet.finishParamSet(context);
				
				context.buffer.append('}');
			}
		}
	}
	
	/*****************************************************/
	private static class StartCypherWriter {

		private static void toCypherExpression(StartExpression sx, WriterContext context) {
			JcElement jcElem = sx.getJcElement();
			ValueWriter.toValueExpression(jcElem, context, context.buffer);
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
					if (QueryParam.isExtractParams(context)) {
						QueryParam qp = QueryParam.createAddParam(null,
								poq.getLuceneQuery(), context);
						PrimitiveCypherWriter.writeParameter(qp, context.buffer);
					} else {
						context.buffer.append('"');
						context.buffer.append(poq.getLuceneQuery());
						context.buffer.append('"');
					}
				} else if (poq.getPropertyValue() != null) {
					context.buffer.append(poq.getPropertyName());
					context.buffer.append(" = ");
					if (QueryParam.isExtractParams(context)) {
						QueryParam qp = QueryParam.createAddParam(null,
								poq.getPropertyValue(), context);
						PrimitiveCypherWriter.writeParameter(qp, context.buffer);
					} else
						PrimitiveCypherWriter.writePrimitiveValue(poq.getPropertyValue(), context, context.buffer);
				}
				context.buffer.append(')');
			} else if (sx.getIndexOrId().getIds() != null) {
				context.buffer.append('(');
				if (QueryParam.isExtractParams(context)) {
					Object val;
					if (sx.getIndexOrId().getIds().size() == 1)
						val = sx.getIndexOrId().getIds().get(0);
					else
						val = sx.getIndexOrId().getIds();
					QueryParam qp = QueryParam.createAddParam(null,
							val, context);
					PrimitiveCypherWriter.writeParameter(qp, context.buffer);
				} else {
					boolean first = true;
					for (Long id : sx.getIndexOrId().getIds()) {
						if (!first)
							context.buffer.append(", ");
						context.buffer.append(id.toString());
						first = false;
					}
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
	private static class CaseCypherWriter {

		private static void toCaseExpression(CaseExpression cx, WriterContext context) {
			JcValue cv = cx.getCaseValue();
			if (cv != null)
				ValueWriter.toValueExpression(cv, context, context.buffer);
		}
		
		private static void toWhenExpression(PredicateExpression px, WriterContext context) {
			PredicateCypherWriter.toCypherExpression(px, context);
		}
		
		private static void toEndExpression(CaseExpression cx, WriterContext context) {
			if (cx.getEndAlias() != null) {
				context.buffer.append(" AS ");
				ValueWriter.toValueExpression(cx.getEndAlias(), context, context.buffer);
			}
		}
	}
	
	/*****************************************************/
	public static class PrimitiveCypherWriter {

		public static void writePrimitiveValue(Object val, WriterContext context, StringBuilder sb) {
			if (val instanceof Number) {
				sb.append(val.toString());
			} else if (val instanceof Boolean) {
				sb.append(val.toString());
			} else if (val instanceof List<?>) {
				sb.append('[');
				List<?> list = (List<?>)val;
				for (int i = 0; i < list.size(); i++) {
					if (i > 0)
						sb.append(", ");
					PrimitiveCypherWriter.writePrimitiveValue(list.get(i), context, sb);
				}
				sb.append(']');
			} else if (val instanceof JcValue) {
				sb.append(ValueAccess.getName((JcValue)val));
			} else {
				sb.append('\'');
				sb.append(val.toString());
				sb.append('\'');
			}
		}
		
		private static void writeParameter(QueryParam param, StringBuilder sb) {
			sb.append('{');
			sb.append(param.getKey());
			sb.append('}');
		}
		
		private static void writeParameterSet(QueryParamSet paramSet, WriterContext context) {
			context.buffer.append(paramSet.getKey());
		}
	}
}
