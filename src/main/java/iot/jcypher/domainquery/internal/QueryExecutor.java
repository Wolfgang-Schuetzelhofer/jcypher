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

package iot.jcypher.domainquery.internal;

import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.internal.CurrentDomain;
import iot.jcypher.domain.internal.DomainAccess.InternalDomainAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.domain.internal.SkipLimitCalc;
import iot.jcypher.domain.internal.SkipLimitCalc.SkipsLimits;
import iot.jcypher.domain.mapping.CompoundObjectType;
import iot.jcypher.domain.mapping.FieldMapping;
import iot.jcypher.domain.mapping.MappingUtil;
import iot.jcypher.domain.mapping.ObjectMapping;
import iot.jcypher.domain.mapping.surrogate.Array;
import iot.jcypher.domain.mapping.surrogate.Collection;
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.Count;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.api.IPredicateOperand1;
import iot.jcypher.domainquery.ast.CollectExpression;
import iot.jcypher.domainquery.ast.ConcatenateExpression;
import iot.jcypher.domainquery.ast.ConcatenateExpression.Concatenator;
import iot.jcypher.domainquery.ast.IASTObject;
import iot.jcypher.domainquery.ast.OrderExpression;
import iot.jcypher.domainquery.ast.OrderExpression.OrderBy;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.domainquery.ast.PredicateExpression;
import iot.jcypher.domainquery.ast.PredicateExpression.Operator;
import iot.jcypher.domainquery.ast.SelectExpression;
import iot.jcypher.domainquery.ast.TraversalExpression;
import iot.jcypher.domainquery.ast.TraversalExpression.Step;
import iot.jcypher.domainquery.ast.UnionExpression;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.api.APIObjectAccess;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.api.pattern.Node;
import iot.jcypher.query.api.pattern.Relation;
import iot.jcypher.query.api.predicate.BooleanOperation;
import iot.jcypher.query.api.predicate.Concat;
import iot.jcypher.query.api.predicate.PFactory;
import iot.jcypher.query.api.returns.RSortable;
import iot.jcypher.query.ast.predicate.BooleanValue;
import iot.jcypher.query.ast.predicate.IPredicateHolder;
import iot.jcypher.query.ast.returns.ReturnElement;
import iot.jcypher.query.ast.returns.ReturnExpression;
import iot.jcypher.query.factories.clause.OPTIONAL_MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.SEPARATE;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.factories.clause.WITH;
import iot.jcypher.query.factories.xpression.C;
import iot.jcypher.query.factories.xpression.I;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcCollection;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.JcPath;
import iot.jcypher.query.values.JcPrimitive;
import iot.jcypher.query.values.JcValue;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.ValueElement;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class QueryExecutor implements IASTObjectsContainer {

	private static final String idPrefix = "id_";
	private static final String countPrefix = "cnt_";
	private static final String tmpNodePostPrefix = "_t";
	private static final String collNodePostPrefix = "_c";
	private static final String countXprPostPrefix = "_cnt";
	private static final String collectNodePostfix = "_col";
	private static final char separator = '_';
	
	private IDomainAccess domainAccess;
	private List<IASTObject> astObjects;
	private List<OrderExpression> orders;
	private List<DomainObjectMatch<?>> domainObjectMatches;
	private Map<String, Parameter> parameters;
	private MappingInfo mappingInfo;
	private QueryContext queryResult;
	private QueryContext countResult;
	
	public QueryExecutor(IDomainAccess domainAccess) {
		super();
		this.domainAccess = domainAccess;
		this.astObjects = new ArrayList<IASTObject>();
		this.domainObjectMatches = new ArrayList<DomainObjectMatch<?>>();
		this.parameters = new HashMap<String, Parameter>();
	}

	@Override
	public void addAstObject(IASTObject astObj) {
		this.astObjects.add(astObj);
	}
	
	public List<IASTObject> getAstObjects() {
		return astObjects;
	}

	public OrderExpression getOrderFor(DomainObjectMatch<?> dom) {
		if (this.orders == null)
			this.orders = new ArrayList<OrderExpression>();
		OrderExpression ret = null;
		for (OrderExpression oe : this.orders) {
			if (oe.getObjectMatch().equals(dom)) {
				ret = oe;
				break;
			}
		}
		if (ret == null) {
			ret = new OrderExpression(dom);
			this.orders.add(ret);
		}
		return ret;
	}

	public List<DomainObjectMatch<?>> getDomainObjectMatches() {
		return domainObjectMatches;
	}
	
	/**
	 * Get or create, if not exists, a query parameter.
	 * @param name of the parameter
	 * @return a query parameter
	 */
	public Parameter parameter(String name) {
		Parameter param = this.parameters.get(name);
		if (param == null) {
			param = new Parameter(name);
			this.parameters.put(name, param);
		}
		return param;
	}
	
	/**
	 * Execute the domain query
	 */
	public void execute() {
		String pLab = ((IIntDomainAccess)domainAccess).getInternalDomainAccess().setDomainLabel();
		try {
			executeInternal(false);
		} finally {
			CurrentDomain.setDomainLabel(pLab);
		}
	}
	
	/**
	 * Execute the count query
	 */
	public void executeCount() {
		String pLab = ((IIntDomainAccess)domainAccess).getInternalDomainAccess().setDomainLabel();
		try {
			executeInternal(true);
		} finally {
			CurrentDomain.setDomainLabel(pLab);
		}
	}
	
	private void executeInternal(boolean execCount) {
		QueryContext context = new QueryContext(execCount);
		QueryBuilder qb = new QueryBuilder();
		List<JcQuery> queries = qb.buildQueries(context);
		Util.printQueries(queries, execCount ? "COUNT QUERY" : "DOM QUERY", Format.PRETTY_1);
		List<JcQueryResult> results = ((IIntDomainAccess)domainAccess).getInternalDomainAccess().
														execute(queries);
		List<JcError> errors = Util.collectErrors(results);
		if (errors.size() > 0) {
			throw new JcResultException(errors);
		}
//		Util.printResults(results, execCount ? "COUNT QUERY" : "DOM QUERY", Format.PRETTY_1);
		if (context.execCount) {
			qb.extractCounts(results, context.resultsPerType);
			this.countResult = context;
		} else {
			qb.extractUniqueIds(results, context.resultsPerType);
			context.queryExecuted();
			this.queryResult = context;
		}
	}
	
	public MappingInfo getMappingInfo() {
		if (this.mappingInfo == null)
			this.mappingInfo = new MappingInfo();
		return this.mappingInfo;
	}
	
	public List<IASTObject> getExpressionsFor(DomainObjectMatch<?> dom, List<IASTObject> astObjs) {
		QueryBuilder.ExpressionsPerDOM xpds = new QueryBuilder().buildExpressionsPerDOM(dom, astObjs, 0);
		return xpds.xPressions;
	}
	
	@SuppressWarnings("unchecked")
	public <T> List<T> loadResult(DomainObjectMatch<T> match) {
		if (APIAccess.isPageChanged(match)) // need to execute the query
			execute();
		if (this.queryResult == null)
			throw new RuntimeException("query was not executed, call execute() on DomainQuery");
		List<QueryContext.ResultsPerType> resPerTypeList = this.queryResult.resultsMap.get(match);
		if (resPerTypeList == null)
			throw new RuntimeException("DomainObjectMatch was not defined in this query");
		Map<Class<?>, List<Long>> type2IdsMap = new HashMap<Class<?>, List<Long>>();
		List<Long> allIds = new ArrayList<Long>();
		List<T> ret = null;
		for (QueryContext.ResultsPerType resPerType : resPerTypeList) {
			if (resPerType.jcPrimitive != null) {
				if (ret == null)
					ret = new ArrayList<T>();
				if (resPerType.simpleValues != null)
					ret.addAll((java.util.Collection<? extends T>) resPerType.simpleValues);
			} else {
				List<Long> ids = type2IdsMap.get(resPerType.type);
				boolean addList = false;
				if (ids == null) {
					ids = new ArrayList<Long>();
					addList = true;
				}
				ids.addAll(resPerType.ids);
				allIds.addAll(resPerType.ids);
				if (ids.isEmpty())
					addList = false;
				if (addList)
					type2IdsMap.put(resPerType.type, ids);
			}
		}
		
		if (ret == null) { // not null in case of primitive values
			long[] idsArray = new long[allIds.size()];
			for (int i = 0; i < allIds.size(); i++) {
				idsArray[i] = allIds.get(i).longValue();
			}
			ret = ((IIntDomainAccess)domainAccess).getInternalDomainAccess().
				loadByIds(APIAccess.getDomainObjectType(match), type2IdsMap, -1, idsArray);
		}
		return ret;
	}
	
	public long getCountResult(DomainObjectMatch<?> match) {
		long res = 0;
		if (this.countResult == null)
			throw new RuntimeException("query was not executed, call executeCount() on DomainQuery");
		List<QueryContext.ResultsPerType> resPerTypeList = this.countResult.resultsMap.get(match);
		if (resPerTypeList == null)
			throw new RuntimeException("DomainObjectMatch was not defined in this query");
		for (QueryContext.ResultsPerType resPerType : resPerTypeList) {
			res = res + resPerType.count;
		}
		return res;
	}
	
	private QueryContext loadCountResultIfNeeded() {
		if (this.countResult == null) {
			executeCount();
		}
		return this.countResult;
	}
	
	/************************************/
	private class QueryBuilder {
		
		private ClauseBuilder clauseBuilder = new ClauseBuilder();
		
		List<JcQuery> buildQueries(QueryContext context) {
			List<ClausesPerType> clausesPerTypeList = new ArrayList<ClausesPerType>();
			List<ExpressionsPerDOM> xpressionsPerDom = new ArrayList<ExpressionsPerDOM>();
			for (DomainObjectMatch<?> dom : domainObjectMatches) {
				ExpressionsPerDOM xpd = buildExpressionsPerDOM(dom, astObjects, 0);
				if (xpd != null)
					xpressionsPerDom.add(xpd);
			}
			
			xpressionsPerDom = orderByDependencies(xpressionsPerDom);
			
			ClauseBuilderContext cbContext = new ClauseBuilderContext(new ArrayList<String>());
			for (ExpressionsPerDOM xpd : xpressionsPerDom) {
				clausesPerTypeList.addAll(this.clauseBuilder.buildClausesFor(xpd, cbContext, context.execCount));
			}
			
			return buildQueriesInt(clausesPerTypeList, context, cbContext);
		}
		
		/**
		 * @param dom
		 * @param astObjs
		 * @param mode 0 ... all expressions, 1 ... no count expressions, 2 ... count expressions only
		 * @return
		 */
		private ExpressionsPerDOM buildExpressionsPerDOM(DomainObjectMatch<?> dom, List<IASTObject> astObjs,
				int mode) {
			ExpressionsPerDOM ret = null;
			StateContext stateContext = findXpressionsFor(dom, astObjs, mode);
			if (stateContext.state == State.HAS_XPRESSION) {
				ret = new ExpressionsPerDOM(dom, stateContext.candidates,
						stateContext.dependencies);
			} else if (stateContext.state == State.INIT) {
				stateContext.candidates.clear();
				ret = new ExpressionsPerDOM(dom, stateContext.candidates,
						stateContext.dependencies);
			}
			return ret;
		}
		
		private List<JcQuery> buildQueriesInt(List<ClausesPerType> clausesPerTypeList,
				QueryContext context, ClauseBuilderContext cbContext) {
			List<JcQuery> ret = new ArrayList<JcQuery>();
			int queryIndex = -1;
			// if split into multiple queries, due to use of LIMIT, SKIP,
			// which must be attached to a RETURN clause
			int startIdx = 0;
			while(startIdx < clausesPerTypeList.size()) {
				queryIndex++;
				List<IClause> clauses = new ArrayList<IClause>();
				List<ClausesPerType> toReturn = new ArrayList<ClausesPerType>();
				List<ClausesPerType> added = new ArrayList<ClausesPerType>();
				OrderClausesHolder orderClausesHolder = new OrderClausesHolder();
				boolean lastIsWith = false;
				int i;
				for (i = startIdx; i < clausesPerTypeList.size(); i++) {
					ClausesPerType cpt = clausesPerTypeList.get(i);
					if (cpt.collectionClauses != null)
						cpt.clauses = null; // reset for collection clauses
														// to ensure accurate with clauses
					if (cpt.valid) {
						boolean startNewQueryAfterThis = false;
						if (!context.execCount) { // execute full query (not count query)
							// we need to split up in multiple queries
							if (cpt.needDistinctQuery()) { // need to add it to the RETURN clause
																		// does not work at the WITH clause
								if (i > startIdx) { // start a new query beginning with this one
									startIdx = i;     // at the next run
									break;
								}
								orderClausesHolder.checkForOrdeClause(cpt);
								orderClausesHolder.whereNot = WHERE.NOT().valueOf(cpt.node).IS_NULL();
								// i == startIdx (must be)
								// start a new query after this one
								startIdx = i + 1;
								startNewQueryAfterThis = true;
							} else
								orderClausesHolder.checkForOrdeClause(cpt);
						}
						
						if (cpt.startCountSelectXpr) { // we may need a distinct query.
							// A count expression is based on a traversal expression, which
							// will have been created already. It spoils the the traversal clones
							// for the select expression. To avoid this, we need a distinct query.
							if (i > startIdx) { // start a new query beginning with this one
								startIdx = i;     // at the next run
								break;
							}
						}
						
						cpt.addDependencyClauses(clauses, added, orderClausesHolder,
								cbContext, context.execCount);
						clauses.addAll(cpt.getClauses(added));
						if (APIAccess.isPartOfReturn(cpt.domainObjectMatch)) {
							toReturn.add(cpt);
							QueryContext.ResultsPerType resPerType = context.addFor(cpt, context.execCount);
							context.resultsPerType.add(resPerType);
							resPerType.queryIndex = queryIndex;
						}
						added.add(cpt);
						lastIsWith = cpt.lastIsWith;
						if (startNewQueryAfterThis)
							break;
					} else
						context.addEmptyFor(cpt); // did not produce a result
				}
				
				if (i == clausesPerTypeList.size()) // we are finished
					startIdx = i;
				
				List<IClause> returnClauses = buildReturnClauses(toReturn, context.execCount);
				// if we need to add WITH clauses,
				// add only those which will be returned
				orderClausesHolder.addClauses(clauses, lastIsWith, toReturn);
				clauses.addAll(returnClauses);
				
				IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
				JcQuery query = new JcQuery();
				query.setClauses(clausesArray);
				ret.add(query);
			}
			return ret;
		}

		private List<IClause> buildReturnClauses(List<ClausesPerType> toReturn,
				boolean isCountQuery) {
			List<IClause> ret = new ArrayList<IClause>();
			int idx = 0;
			for (ClausesPerType cpt : toReturn) {
				JcNode n = cpt.node;
				RSortable returnClause;
				if (isCountQuery)
					returnClause = RETURN.count().DISTINCT().value(n).AS(this.getJcNumber(countPrefix, n));
				else {
					if (cpt.isCollectExpression) {
						if (idx == 0)
							returnClause = RETURN.DISTINCT().value(cpt.node);
						else
							returnClause = RETURN.value(cpt.node);
					} else {
						if (idx == 0)
							returnClause = RETURN.DISTINCT().value(n.id()).AS(this.getJcNumber(idPrefix, n));
						else
							returnClause = RETURN.value(n.id()).AS(this.getJcNumber(idPrefix, n));
					}
					// add skip and limit if required
					if (cpt.pageOffset > 0)
						returnClause = (RSortable)returnClause.SKIP(cpt.pageOffset);
					if (cpt.pageLength >= 0)
						returnClause = (RSortable)returnClause.LIMIT(cpt.pageLength);
				}
				ret.add(returnClause);
				idx++;
			}
			return ret;
		}

		void extractUniqueIds(List<JcQueryResult> results, List<QueryContext.ResultsPerType> resultsPerTypeList) {
			Set<Long> uniqueIds = new LinkedHashSet<Long>();
			Set<Object> simpleVals = new LinkedHashSet<Object>();
			for (QueryContext.ResultsPerType resPerType : resultsPerTypeList) {
				uniqueIds.clear();
				simpleVals.clear();
				JcQueryResult result = results.get(resPerType.queryIndex);
				if (MappingUtil.isSimpleType(resPerType.type)) {
					flatten(result.resultOf(resPerType.jcPrimitive), simpleVals);
					resPerType.simpleValues = new ArrayList<Object>();
					resPerType.simpleValues.addAll(simpleVals);
				} else {
					List<BigDecimal> idList = result.resultOf(resPerType.jcNumber);
					for (BigDecimal bd : idList) {
						if (bd != null) {
							uniqueIds.add(bd.longValue());
						}
					}
					resPerType.ids = new ArrayList<Long>();
					resPerType.ids.addAll(uniqueIds);
				}
			}
			return;
		}
		
		@SuppressWarnings({ "unchecked", "rawtypes" })
		private void flatten(List<?> resultOf, Set<Object> simpleVals) {
			for (Object elem : resultOf) {
				if (elem instanceof List<?>)
					simpleVals.addAll((java.util.Collection) elem);
				else
					simpleVals.add(elem);
			}
		}

		void extractCounts(List<JcQueryResult> results, List<QueryContext.ResultsPerType> resultsPerTypeList) {
			for (QueryContext.ResultsPerType resPerType : resultsPerTypeList) {
				JcQueryResult result = results.get(resPerType.queryIndex);
				List<BigDecimal> count = result.resultOf(resPerType.jcNumber);
				resPerType.count = count.get(0).longValue();
			}
		}
		
		/**
		 * may return null
		 * @param cpt
		 * @return
		 */
		private List<OrderBy> getOrderExpressionsFor(ClausesPerType cpt) {
			List<OrderBy> ret = null;
			OrderExpression oe = getOrderFor(cpt.domainObjectMatch);
			if (oe != null) {
				List<OrderBy> ocs = oe.getOrderCriterias();
				for (OrderBy ob : ocs) {
					String attribName = ob.getAttributeName();
					FieldMapping fm = getMappingInfo().getFieldMapping(attribName, cpt.domainObjectType);
					if (fm != null) { // field exists for type, so we can sort
						if (ret == null)
							ret = new ArrayList<OrderBy>();
						ret.add(ob);
					}
				}
			}
			return ret;
		}

		private List<ExpressionsPerDOM> orderByDependencies(
				List<ExpressionsPerDOM> xpressionsPerDom) {
			List<ExpressionsPerDOM> ordered = new ArrayList<ExpressionsPerDOM>(xpressionsPerDom.size());
			List<ExpressionsPerDOM> tryingToAdd = new ArrayList<ExpressionsPerDOM>(xpressionsPerDom.size());
			
			for (ExpressionsPerDOM xpd : xpressionsPerDom) {
				addRecursive(xpd, xpressionsPerDom, ordered, tryingToAdd);
			}
			
			return ordered;
		}
		
		private void addRecursive(ExpressionsPerDOM add, List<ExpressionsPerDOM> xpressionsPerDom,
				List<ExpressionsPerDOM> ordered, List<ExpressionsPerDOM> tryingToAdd) {
			if (tryingToAdd.contains(add))
				throw new RuntimeException("circular dependencies in WHERE clauses");
			for (ExpressionsPerDOM xp : tryingToAdd) {
				xp.addDependencies(add.flattenedDependencies);
				xp.addDependency(add);
			}
			if (add.dependencies == null || add.dependencies.isEmpty()) {
				if (!ordered.contains(add))
					ordered.add(add);
			} else {
				if (!ordered.contains(add)) {
					tryingToAdd.add(add);
					for (DomainObjectMatch<?> dom : add.dependencies) {
						ExpressionsPerDOM xpd = getXprPerDom(dom, xpressionsPerDom);
						addRecursive(xpd, xpressionsPerDom, ordered, tryingToAdd);
					}
					tryingToAdd.remove(add);
					ordered.add(add);
				}
			}
		}
		
		private ExpressionsPerDOM getXprPerDom(DomainObjectMatch<?> dom,
				List<ExpressionsPerDOM> xpressionsPerDom) {
			for (ExpressionsPerDOM xpd : xpressionsPerDom) {
				if (xpd.domainObjectMatch.equals(dom))
					return xpd;
			}
			return null;
		}

		private SkipsLimits calcSkipsLimits(DomainObjectMatch<?> dom, int offset, int len) {
			QueryContext cRes = loadCountResultIfNeeded();
			List<QueryContext.ResultsPerType> rpts = cRes.resultsMap.get(dom);
			List<Integer> counts = new ArrayList<Integer>(rpts.size());
			for (QueryContext.ResultsPerType rpt : rpts) {
				counts.add((int)rpt.count);
			}
			SkipsLimits slc = SkipLimitCalc.calcSkipsLimits(counts, offset, len);
			return slc;
		}

		/**
		 * @param value
		 * @param op
		 * @param paramPosition 1 or 2
		 */
		private void testValidInOperation(Operator op,
				int paramPosition, PredicateExpression pred) {
			if (pred.getValue_1() instanceof Count) {
				if (!pred.isInCollectionExpression())
					throw new RuntimeException("'COUNT' operation on a DomainObjectMatch is only valid within" +
							" a collection expression");
			}
			
			if (op == Operator.CONTAINS) {
				if (pred.getValue_1() instanceof Count)
					throw new RuntimeException("'CONTAINS' cannot be applied to COUNT on DomainObject(s)");
				else if (!(pred.getValue_1() instanceof JcCollection) &&
						!(pred.isInCollectionExpression() &&
						pred.getValue_1() instanceof DomainObjectMatch<?> &&
						pred.getValue_2() instanceof DomainObjectMatch<?>)) {
					if (!(pred.getValue_1() instanceof ValueElement))
						throw new RuntimeException("'CONTAINS' operation on two DomainObjectMatch(es) is only valid within" +
								" a collection expression");
					else
						throw new RuntimeException("'CONTAINS' operation can only be performed on a 'Collection Attribute'." +
								"\nUse .collectionAttribute() on the DomainObjectMatch to access the attribute for the 'CONTAINS' operation!");
				} 
				return; // valid
			}
			
			Object value;
			if (paramPosition == 1) {
				value = pred.getValue_1();
				if (value instanceof DomainObjectMatch<?>) {
					if (!(op == Operator.IN || op == Operator.IS_NULL))
						throw new RuntimeException("invalid parameter 1 in WHERE clause [" + op.name() + "]");
				}
			} else if (paramPosition == 2) {
				value = pred.getValue_2();
				if (value instanceof DomainObjectMatch<?>) {
					if (op != Operator.IN)
						throw new RuntimeException("invalid parameter 2 in WHERE clause [" + op.name() + "]");
				}
			}
		}

		private List<JcNode> collectNodes(DomainObjectMatch<?> dom, List<String> validNodes,
				Class<?> resultType) {
			List<JcNode> ret = new ArrayList<JcNode>();
			if (resultType != null) {
				JcNode n = APIAccess.getNodeForType(dom, resultType);
				if (n != null) {
					String nnm = ValueAccess.getName(n);
					if (validNodes.contains(nnm))
						ret.add(n);
				}
			} else {
				List<JcNode> nodes = APIAccess.getNodes((DomainObjectMatch<?>)dom);
				for(JcNode n : nodes) {
					String nnm = ValueAccess.getName(n);
					if (validNodes.contains(nnm))
						ret.add(n);
				}
			}
			return ret;
		}
		
		/**
		 * @param val
		 * @param validNodes if != null, use list to filter nodes
		 * @return a list of ValueElements
		 */
		@SuppressWarnings("unchecked")
		private List<Object> buildAllInstances(ValueElement ve, List<String> validNodes) {
			List<Object> ret = new ArrayList<Object>();
			List<JcNode> validFor = null;
			if (ve != null) {
				ValueElement first = ValueAccess.findFirst(ve);
				if (first instanceof JcNode) {
					String nodeName = ValueAccess.getName((JcNode)first);
					if (validNodes.contains(nodeName))
						ret.add(ve);
					if (validFor == null) {
						Object hint = ValueAccess.getAnyHint(ve, APIAccess.hintKey_validNodes);
						if (hint instanceof List<?>) {
							validFor = (List<JcNode>) hint; 
						}
					}
					for(JcNode n : validFor) {
						String nnm = ValueAccess.getName(n);
						if (!nodeName.equals(nnm)) { // need to clone
							if (validNodes.contains(nnm))
								ret.add(cloneVe(ve, first, n));
						}
					}
				} else
					ret.add(ve);
			}
			return ret;
		}

		/**
		 * @param ve
		 * @param clausesPerType
		 * @return the expression or its clone, or null if the expression is not valid for this type
		 */
		private ValueElement testAndCloneIfNeeded(ValueElement ve, ClausesPerType clausesPerType) {
			ValueElement ret = ve;
			if (ve != null) {
				ValueElement first = ValueAccess.findFirst(ve);
				if (first instanceof JcNode) {
					testValidForType((JcNode)first, ve, clausesPerType);
					if (clausesPerType.valid) {
						if(!(ValueAccess.getName((JcNode)first).equals(ValueAccess.getName(clausesPerType.node)))) {
							ret = cloneVe(ve, first, clausesPerType.node);
						}
					} else
						ret = null;
				}
			}
			return ret;
		}
		
		private ValueElement cloneVe(ValueElement ve, ValueElement first,
				ValueElement newFirst) {
			ValueElement ret = ve;
			ValueElement prev = ve;
			ValueElement nextCloned = null;
			while(prev != first) {
				ValueElement cloned = ValueAccess.cloneShallow(prev);
				if (nextCloned != null)
					ValueAccess.setPredecessor(nextCloned, cloned);
				else // in the first iteration
					ret = cloned;
				nextCloned = cloned;
				prev = ValueAccess.getPredecessor(prev);
			}
			if (nextCloned != null) // there was at least one iteration
				ValueAccess.setPredecessor(nextCloned, newFirst);
			else 
				ret = newFirst;
			return ret;
		}

		@SuppressWarnings("unchecked")
		private void testValidForType(JcNode first,
				ValueElement ve, ClausesPerType clausesPerType) {
			if (ve == first) {
				clausesPerType.oneValid = true;
				return;
			}
			if (clausesPerType.previousOr || !Settings.strict) { // build the clause even if the expression will return no result
				clausesPerType.oneValid = true; // because it is concatenated by OR
				return;	
			}
			Object hint = ValueAccess.getAnyHint(ve, APIAccess.hintKey_validNodes);
			if (hint instanceof List<?>) {
				List<JcNode> validFor = (List<JcNode>) hint; 
				String nm = ValueAccess.getName(clausesPerType.node);
				for(JcNode n : validFor) {
					if (nm.startsWith(ValueAccess.getName(n))) {
						clausesPerType.oneValid = true;
						return;
					}
				}
			}
			clausesPerType.testForNextIsOr = true;
			return;
		}

		/**
		 * @param dom
		 * @param astObjs
		 * @param mode 0 ... all expressions, 1 ... no count expressions, 2 ... count expressions only
		 * @return
		 */
		private StateContext findXpressionsFor(DomainObjectMatch<?> dom, List<IASTObject> astObjs,
				int mode) {
			String baseNodeName = APIAccess.getBaseNodeName(dom);
			StateContext context = new StateContext(baseNodeName);
			for (int i = 0; i < astObjs.size(); i++) {
				IASTObject astObj = astObjs.get(i);
				testXpressionFor(dom, baseNodeName, astObj, context, mode);
			}
			if (context.blockCount != 0)
				throw new RuntimeException("bracket close mismatch");
			if (context.state == State.HAS_XPRESSION) {
				removeEmptyBlocks(context.candidates);
			}
			return context;
		}

		private void removeEmptyBlocks(List<IASTObject> candidates) {
			List<Integer> toRemove = new ArrayList<Integer>();
			List<Integer> orsToRemove = new ArrayList<Integer>();
			List<Integer> candidatesIndices = new ArrayList<Integer>();
			// key: openBracket index, value: or index
			Map<Integer, Integer> bracketToOr = new HashMap<Integer, Integer>();
			int orIndexToTest = -1;
			boolean orMaybeValid = false; // or must not be at begin
			for (int i = 0; i < candidates.size(); i++) {
				IASTObject astObj = candidates.get(i);
				if (astObj instanceof ConcatenateExpression) {
					Concatenator concat = ((ConcatenateExpression)astObj).getConcatenator();
					if (concat == Concatenator.BR_OPEN) {
						if (orIndexToTest != -1) // or may be before open bracket
							bracketToOr.put(i, orIndexToTest);
						orIndexToTest = -1;
						candidatesIndices.add(i);
						orMaybeValid = false; // or must not follow immediately after an open bracket
					} else if (concat == Concatenator.BR_CLOSE) {
						if (candidatesIndices.size() > 0) { // close follows immediately after open
																				// empty block will be removed
							// idx is the matching open bracket index
							Integer idx = candidatesIndices.remove(candidatesIndices.size() - 1);
							Integer orIdx = bracketToOr.get(idx);
							if (orIdx != null)
								orIndexToTest = orIdx.intValue(); // must be tested if valid
							else
								orIndexToTest = -1; // if != -1was within the block that will be removed
																 // it will be removed with the block so we can forget it
							// test and remove encapsulated pairs
							for (int j = toRemove.size() - 1; j >= 0; j--) {
								if (idx.intValue() < toRemove.get(j).intValue())
									toRemove.remove(j);
							}
							toRemove.add(idx); // start
							toRemove.add(i); // end
						} else {
							orMaybeValid = true; // or can follow a closing bracket
							if (orIndexToTest != -1)
								orsToRemove.add(orIndexToTest);  // or must not be immediately before a close bracket
							orIndexToTest = -1;
						}
					} else if (concat == Concatenator.OR) {
						if (!orMaybeValid)
							orsToRemove.add(i);
						else {
							orIndexToTest = i; // for test with next expression
							orMaybeValid = false; // no consecutive ors allowed
						}
					}
				} else { // not an empty block, contains at least one valid predicate expression
					candidatesIndices.clear();
					orMaybeValid = true; // or can follow a predicate expression
					orIndexToTest = -1;
				}
			}
			if (orIndexToTest != -1)
				orsToRemove.add(orIndexToTest);  // or must not be at the end
			
			// now remove the found empty blocks
			// indices are always pairwise for close and open brackets
			int prevCloseIndex = -1; // the previous close index paired with the actual open index
			// we always start with a close index
			for (int i = toRemove.size() - 1; i >= 0; i--) {
				int idx = toRemove.get(i).intValue();
				if (prevCloseIndex == -1) // the actual is a close index
					candidates.remove(idx);
				else { // the actual is an open index
					for (int j = prevCloseIndex - 1; j >= idx; j--) {
						// remove all between the previous close index (exclusive)
						// and the actual open index (inclusive)
						candidates.remove(j);
					}
					int adjust = prevCloseIndex - idx + 1; // this range is removed
					// remove and adjust or indices
					for (int j = orsToRemove.size() - 1; j >= 0; j--) {
						int oidx = orsToRemove.get(j).intValue();
						if (oidx > idx && oidx < prevCloseIndex) // remove
							orsToRemove.remove(j);
						else if (oidx > prevCloseIndex)
							orsToRemove.set(j, oidx - adjust);
					}
				}
				// toggle between close and open index (they are always pairwise)
				prevCloseIndex = prevCloseIndex == -1 ? idx : -1;
			}
			
			// remove invalid ors
			Collections.sort(orsToRemove);
			for (int i = orsToRemove.size() - 1; i >= 0; i--) {
				int idx = orsToRemove.get(i).intValue();
				candidates.remove(idx);
			}
		}

		/**
		 * @param baseNodeName
		 * @param astObj
		 * @param context
		 * @param mode 0 ... all expressions, 1 ... no count expressions, 2 ... count expressions only
		 */
		private void testXpressionFor(DomainObjectMatch<?> dom, String baseNodeName,
				IASTObject astObj, StateContext context, int mode) {
			if (astObj instanceof PredicateExpression) {
				PredicateExpression pred = (PredicateExpression)astObj;
				boolean isXpr = false;
				IPredicateOperand1 val1 = pred.getValue_1();
				String nodeName_1 = getNodeName(val1);
				int orRemoveState = 0;
				boolean isCount = val1 instanceof Count;
				if (mode == 0 || (isCount && mode == 2) || (!isCount && mode == 1)) {
					if (isCount) {
						DomainObjectMatch<?> udom = APIAccess.getDomainObjectMatch((Count)val1);
						if (APIAccess.isPartOfUnionExpression(udom, dom)) {
							nodeName_1 = APIAccess.getBaseNodeName(dom);
						}
					}
					
					if (nodeName_1 != null) {
						if (nodeName_1.indexOf(baseNodeName) == 0) {
							if (context.orToAdd != null) // add pending or
								context.candidates.add(context.orToAdd);
							context.candidates.add(astObj);
							isXpr = true;
							context.state = State.HAS_XPRESSION;
							orRemoveState = -1;
						}
					}
				}
				if (isXpr) {
					String nodeName_2 = getNodeName(pred.getValue_2());
					if (nodeName_2 != null) {
						if (nodeName_2.indexOf(baseNodeName) < 0) {
							context.addDependency(nodeName_2);
						}
					}
				}
				context.orRemoveState = orRemoveState;
				context.orToAdd = null; // clear or, it has either been added already or it is invalid
			} else if (astObj instanceof ConcatenateExpression) {
				boolean isOr = false;
				int orRemoveState = -1;
				ConcatenateExpression conc = (ConcatenateExpression)astObj;
				if (conc.getConcatenator() == Concatenator.BR_OPEN) {
					context.blockCount++;
					if (context.orToAdd != null) // add pending or
						context.candidates.add(context.orToAdd);
				} else if (conc.getConcatenator() == Concatenator.BR_CLOSE) {
					if (context.blockCount <= 0)
						throw new RuntimeException("bracket close mismatch");
					context.blockCount--;
				} else if (conc.getConcatenator() == Concatenator.OR) {
					orRemoveState = context.orRemoveState;
					isOr = true;
				}
				context.orToAdd = null; // clear or, it has either been added already or it is invalid
				if (orRemoveState != 0) { // prevoius was not foreign predicate expression
					if (isOr)
						context.orToAdd = astObj; // to test if next is foreign predicate expression
					else
						context.candidates.add(astObj);
				}
				context.orRemoveState = orRemoveState;
			} else if (astObj instanceof TraversalExpression) {
				TraversalExpression te = (TraversalExpression)astObj;
				String bName = APIAccess.getBaseNodeName(te.getEnd());
				if (baseNodeName.equals(bName)) {
					context.candidates.add(astObj);
					context.state = State.HAS_XPRESSION;
					bName = APIAccess.getBaseNodeName(te.getStart());
					context.addDependency(bName);
				}
			} else if (astObj instanceof SelectExpression<?>) {
				SelectExpression<?> se = (SelectExpression<?>)astObj;
				String bName = APIAccess.getBaseNodeName(se.getEnd());
				if (baseNodeName.equals(bName)) {
					context.candidates.add(astObj);
					context.state = State.HAS_XPRESSION;
					bName = APIAccess.getBaseNodeName(se.getStart());
					context.addDependency(bName);
					// add dependencies of predicate expressions within collection expression
					for(IASTObject ao : se.getAstObjects()) {
						if (ao instanceof PredicateExpression) {
							PredicateExpression pred = (PredicateExpression)ao;
							context.addDependency(getNodeName(pred.getValue_1()));
							context.addDependency(getNodeName(pred.getValue_2()));
						}
					}
				}
			} else if (astObj instanceof CollectExpression) {
				CollectExpression ce = (CollectExpression)astObj;
				String bName = APIAccess.getBaseNodeName(ce.getEnd());
				if (baseNodeName.equals(bName)) {
					context.candidates.add(astObj);
					context.state = State.HAS_XPRESSION;
					bName = APIAccess.getBaseNodeName(ce.getStartDOM());
					context.addDependency(bName);
				}
			}
		}

		private String getNodeName(Object value) {
			if (value instanceof ValueElement) {
				ValueElement ve = (ValueElement)value;
				ValueElement first = ValueAccess.findFirst(ve);
				if (first instanceof JcNode)
					return ValueAccess.getName((JcNode)first);
			} else if (value instanceof DomainObjectMatch<?>) {
				return APIAccess.getBaseNodeName((DomainObjectMatch<?>)value);
			} else if (value instanceof Count) {
				return APIAccess.getBaseNodeName(
						APIAccess.getDomainObjectMatch((Count)value));
			}
			return null;
		}
		
		private JcNumber getJcNumber(String prefix, JcNode n) {
			String nm = ValueAccess.getName(n);
			nm = nm.substring(APIAccess.nodePrefix.length());
			nm = prefix.concat(nm);
			return new JcNumber(nm);
		}
		
		/*************************************/
		private class OrderClausesHolder {
			private List<ClausesPerType> toOrder;
			private IClause whereNot;
			
			private void checkForOrdeClause(ClausesPerType cpt) {
				if (getOrderExpressionsFor(cpt) != null) { // if != null it contains at least one criteria
					if (this.toOrder == null)
						this.toOrder = new ArrayList<ClausesPerType>();
					this.toOrder.add(cpt);
				}
			}
			
			private void addClauses(List<IClause> addTo, boolean lastIsWith,
					List<ClausesPerType> withToAdd) {
				if (this.toOrder != null || this.whereNot != null) {
					if (!lastIsWith) { // need to add WITH clauses
						for (ClausesPerType cpt : withToAdd) {
							addTo.add(WITH.value(cpt.node));
						}
					}
				}
				if (this.toOrder != null) {
					for (ClausesPerType cpt : this.toOrder) {
						List<OrderBy> ocs = getOrderExpressionsFor(cpt);
						// returns an index of an RSortable
						int idx = this.indexOfOrderClause(cpt, addTo);
						if (idx != -1) {
							RSortable orderClause = (RSortable) addTo.get(idx);
							for (OrderBy ob : ocs) {
								if (ob.getDirection() == 0)
									orderClause = orderClause.ORDER_BY(ob.getAttributeName()); // TODO use property name
								else
									orderClause = orderClause.ORDER_BY_DESC(ob.getAttributeName());
							}
							addTo.set(idx, orderClause);
						}
					}
				}
				
				if (this.whereNot != null)
					addTo.add(this.whereNot);
			}
			
			private int indexOfOrderClause(ClausesPerType cpt, List<IClause> clauses) {
				int strt = clauses.size() - 1;
				while(strt >= 0 && clauses.get(strt) instanceof SEPARATE)
					strt--;
				int idx = -1;
				for (int i = strt; i >= 0; i--) {
					IClause clause = clauses.get(i);
					if (clause instanceof RSortable) { // only RSortables can be ordered
						// can only be a ReturnExpression
						ReturnExpression rexp = (ReturnExpression)APIObjectAccess.getAstNode((APIObject) clause);
						if (rexp.getAlias() != null && ValueAccess.isSame(rexp.getAlias(), cpt.node)) {
							idx = i;
							break;
						} else if (rexp.getReturnValue() instanceof ReturnElement) {
							JcValue elem = ((ReturnElement)rexp.getReturnValue()).getElement();
							if (ValueAccess.isSame(elem, cpt.node)) {
								idx = i;
								break;
							}
						}
					} else
						break;
				}
				return idx;
			}
		}
		
		/*************************************/
		private class ClauseBuilder {
			private List<ClausesPerType> buildClausesFor(ExpressionsPerDOM xpd,
					ClauseBuilderContext cbContext, boolean calcCounts) {
				DomainObjectMatch<?> dom = xpd.domainObjectMatch;
				List<IASTObject> xpressionsForDom = xpd.xPressions;
				List<ClausesPerType> clausesPerTypeList = new ArrayList<ClausesPerType>();
				List<JcNode> nodes = APIAccess.getNodes(dom);
				List<Class<?>> typeList = APIAccess.getTypeList(dom);
				int numTypes = typeList.size();
				List<Integer> offsets;
				List<Integer> lens;
				int offset = calcCounts ? 0 : APIAccess.getPageOffset(dom);
				int len = calcCounts ? -1 : APIAccess.getPageLength(dom);
				if (numTypes > 1 &&	(offset > 0 || len >= 0) &&
						APIAccess.isPageChanged(dom)) {
					SkipsLimits slc = calcSkipsLimits(dom, offset, len);
					offsets = slc.getOffsets();
					lens = slc.getLengths();
				} else {
					offsets = new ArrayList<Integer>(numTypes);
					lens = new ArrayList<Integer>(numTypes);
					if (numTypes == 1) {
						offsets.add(offset);
						lens.add(len);
					} else {
						for (int i = 0; i < numTypes; i++) {
							offsets.add(0);
							lens.add(-1);
						}
					}
				}
				int idx = 0;
				for (JcNode n : nodes) {
					ClausesPerType cpt = new ClausesPerType(n, dom, typeList.get(idx));
					cpt.expressionsPerDOM = xpd;
					cpt.pageOffset = offsets.get(idx);
					cpt.pageLength = lens.get(idx);
					clausesPerTypeList.add(cpt);
					idx++;
				}
				
				cbContext.stepClausesCount = 0;
				for (IASTObject astObj : xpressionsForDom) {
					for (ClausesPerType clausePerType : clausesPerTypeList) {
						if (clausePerType.valid) {
							List<TraversalResult> travResults = forCollectionOwnersOf(clausePerType.domainObjectMatch,
									clausePerType.domainObjectType, cbContext);
							addClause(astObj, clausePerType, cbContext);
							
							// if traversal results exist and have not been created by this expression,
							// then add the current expression to the traversal result's ClausePerType
							if (travResults != null) {
								for (TraversalResult tr : travResults) {
									for (ClausesPerType cpt : tr.getClausesPerTypeList()) {
										// the first clauses to be added after the traversal clauses
										if (cpt.concatenator == null && cpt.concat == null) {
											cpt.concat = WHERE.BR_OPEN();
										}
										addClause(astObj, cpt, cbContext);
									}
								}
							}
						}
					}
				}

				for (ClausesPerType clausePerType : clausesPerTypeList) {
					if (clausePerType.valid) {
						if (clausePerType.testForNextIsOr && !clausePerType.oneValid) // is not valid because we have reached
							clausePerType.valid = false;		// the end, there is no next OR
						else {
							// add node names for which there is (will be) a match clause
							String nm = ValueAccess.getName(clausePerType.node);
							cbContext.validNodes.add(nm);
						}
					}
				}
				xpd.clausesPerTypes = clausesPerTypeList;
				return clausesPerTypeList;
			}
			
			private List<TraversalResult> forCollectionOwnersOf(DomainObjectMatch<?> travOwner,
					Class<?> travOwnerType, ClauseBuilderContext cbContext) {
				List<DomainObjectMatch<?>> collOwners = APIAccess.getCollectExpressionOwner(travOwner);
				if (collOwners != null) {
					List<TraversalResult> ret = new ArrayList<TraversalResult>();
					for (DomainObjectMatch<?> collOwner : collOwners) {
						TraversalResult tr = cbContext.getTravResult(collOwner, travOwner, travOwnerType);
						if (tr != null)
							ret.add(tr);
					}
					return ret;
				}
				return null;
			}
			
			private void addClause(IASTObject astObj, ClausesPerType clausePerType,
					ClauseBuilderContext cbContext) {
				// The following if may only be executed after select clauses have
				// been added and when thereafter the first expression is added
				if (clausePerType.collectionClauses != null && !clausePerType.closeBracket) {
					// need to encapsulate the following expressions by brackets
					clausePerType.concat = clausePerType.concatenator.AND().BR_OPEN();
					clausePerType.concatenator = null;
					clausePerType.closeBracket = true;
				}
				
				if (astObj instanceof ConcatenateExpression) {
					ConcatenateExpression conc = (ConcatenateExpression)astObj;
					if (conc.getConcatenator() == Concatenator.BR_OPEN) {
						if (clausePerType.concatenator == null) {
							if (clausePerType.concat == null) {
								if (clausePerType.traversalClauses != null)
									initTraversalConcat(clausePerType);
								else
									clausePerType.concat = WHERE.BR_OPEN();
								clausePerType.previousOr = false;
							} else
								clausePerType.concat = clausePerType.concat.BR_OPEN();
						} else { // a predicate expression or a block close was before
							clausePerType.concat = clausePerType.concatenator.AND().BR_OPEN();
							clausePerType.concatenator = null;
							clausePerType.previousOr = false;
						}
						if (clausePerType.testForNextIsOr)
							clausePerType.valid = false;
					} else if (conc.getConcatenator() == Concatenator.BR_CLOSE) {
						if (clausePerType.concatenator == null) {
							throw new RuntimeException("illegal statement: " + conc.getConcatenator().name());
						} else {
							clausePerType.concatenator = clausePerType.concatenator.BR_CLOSE();
						}
						clausePerType.previousOr = false;
					} else if (conc.getConcatenator() == Concatenator.OR) {
						if (clausePerType.concatenator == null) {
							throw new RuntimeException("illegal statement: " + conc.getConcatenator().name());
						} else {
							clausePerType.concat = clausePerType.concatenator.OR();
							clausePerType.previousOr = true;
							clausePerType.testForNextIsOr = false; // or has occurred, we don't need further testing
							clausePerType.concatenator = null;
						}
					}
				} else if (astObj instanceof PredicateExpression) {
					if (clausePerType.testForNextIsOr) {
						clausePerType.valid = false;
						return;
					}
					PredicateExpression pred = (PredicateExpression)astObj;
					if (clausePerType.traversalClauses != null) {
						if (clausePerType.concat == null)
							initTraversalConcat(clausePerType);
					}
					Concat concat_1 = clausePerType.concat;
					if (clausePerType.concatenator != null) {
						concat_1 = clausePerType.concatenator.AND();
						clausePerType.previousOr = false;
					}
					clausePerType.concatenator = buildPredicateExpression(pred, concat_1, clausePerType, cbContext);
				} else if (astObj instanceof TraversalExpression) {
					clausePerType.traversalClauses = createTraversalClauses(
							clausePerType.node, clausePerType,
							(TraversalExpression) astObj, cbContext, false, null).getAllClauses(); // no copy with path
					List<DomainObjectMatch<?>> collXOwner =
							APIAccess.getCollectExpressionOwner(((TraversalExpression)astObj).getEnd());
					if (collXOwner != null) {
						// we need to produce copies of the traversal expression
						// for each collection expression which uses this traversal expression,
						// so that the result of the original traversal expression
						// will not be altered by the collection expression(s)
						for (DomainObjectMatch<?> dom : collXOwner) {
							String nnm = ValueAccess.getName(clausePerType.node).concat(collNodePostPrefix)
									.concat(APIAccess.getBaseNodeName(dom));
							TraversalResult travResult = createTraversalClauses(
									new JcNode(nnm), clausePerType,
									(TraversalExpression) astObj, cbContext, true, dom); // copy with path
							cbContext.addTravClausesPerColl(dom, clausePerType.domainObjectMatch,
									clausePerType.domainObjectType, travResult);
						}
					}
				} else if (astObj instanceof SelectExpression<?>) {
					createAddSelectClauses(clausePerType, (SelectExpression<?>) astObj, cbContext);
				} else if (astObj instanceof CollectExpression) {
					createCollectClauses(clausePerType, (CollectExpression) astObj, cbContext);
				}
			}
			
			private void initTraversalConcat(ClausesPerType cpt) {
				IClause cl = cpt.traversalClauses.get(
						cpt.traversalClauses.size() - 1);
				if (cl instanceof iot.jcypher.query.api.predicate.Concatenator) {
					cpt.concat = ((iot.jcypher.query.api.predicate.Concatenator)cl)
							.AND().BR_OPEN();
					cpt.closeBracket = true;
				}
			}
			
			@SuppressWarnings("unchecked")
			private iot.jcypher.query.api.predicate.Concatenator buildPredicateExpression(PredicateExpression pred,
					Concat concat, ClausesPerType clausesPerType, ClauseBuilderContext cbContext) {
				iot.jcypher.query.api.predicate.Concatenator ret = null;
				// handle negations
				int neg = pred.getNegationCount();
				Concat concat_1 = null;
				BooleanOperation booleanOp;
				Operator op = pred.getOperator();
				ValueElement val_1 = null;
				IPredicateOperand1 v_1 = pred.getValue_1();
				
				testValidInOperation(op, 1, pred); // throws an exception if not valid
				if (v_1 instanceof ValueElement)
					val_1 = testAndCloneIfNeeded((ValueElement)v_1, clausesPerType);
				else if (v_1 instanceof DomainObjectMatch<?> || v_1 instanceof Count)
					val_1 = clausesPerType.node;
				
				if (val_1 != null) { // if either really null or invalid
					Object val_2 = pred.getValue_2();
					if (val_2 instanceof Parameter)
						val_2 = ((Parameter)val_2).getValue();
					testValidInOperation(op, 2, pred); // throws an exception if not valid
					
					boolean val2IsDom = false;
					List<Object> val_2s = null;
					if (val_2 instanceof IPredicateOperand1) { // ValueElement or DomainObjectMatch
						if (val_2 instanceof DomainObjectMatch<?>) { // after test op must be IN or EQUALS
							val2IsDom = true;
							val_2s = new ArrayList<Object>();
							val_2s.add(collectNodes((DomainObjectMatch<?>)val_2, cbContext.validNodes,
									clausesPerType.domainObjectType));
						} else
							val_2s = buildAllInstances((ValueElement)val_2, cbContext.validNodes);
					} else if (val_2 != null) {
						val_2s = new ArrayList<Object>();
						val_2s.add(val_2);
					}
					int cnt = val_2s != null ? val_2s.size() : 1;
					
					boolean negate = false;
					if (val2IsDom) { // after test op must be IN or EQUALS
						if (neg > 0) {
							neg--;
							negate = true;
						}
					}
					for (int i = neg; neg > 0; neg--) {
						if (i == neg) { // the first negation
							if (concat == null)
								concat_1 = (Concat)WHERE.NOT();
							else
								concat_1 = (Concat)concat.NOT();
						} else
							concat_1 = (Concat)concat_1.NOT();
					}
					if (concat_1 == null)
						concat_1 = concat;
					
					if (cnt > 1 || val2IsDom) { // encapsulate by brackets
						if (concat_1 != null)
							concat_1 = concat_1.BR_OPEN();
						else { // no negation(s)
							concat_1 = WHERE.BR_OPEN();
						}
					}
					
					if (!val2IsDom) {
						if (concat_1 == null)
							booleanOp = WHERE.valueOf(val_1);
						else
							booleanOp = concat_1.valueOf(val_1);
					} else
						booleanOp = null;
					
					for (int i = 0; i < cnt; i++) {
						if (val_2s != null)
							val_2 = val_2s.get(i);
						if (op == Operator.EQUALS)
							ret = booleanOp.EQUALS(val_2);
						else if (op == Operator.GT)
							ret = booleanOp.GT(val_2);
						else if (op == Operator.GTE)
							ret = booleanOp.GTE(val_2);
						else if (op == Operator.IN) {
							if (val2IsDom)
								ret = createWhereIn(concat_1, val_1, (List<JcNode>) val_2, negate);
							else
								ret = booleanOp.IN_list(val_2);
						} else if (op == Operator.CONTAINS) {
							// value_1 must be a DomainObjactMatch,
							// must be in collection expression
							// validity test has already been done
							if (val2IsDom)
								ret = createWhereIn(concat_1, val_1, (List<JcNode>) val_2, negate);
							else
								ret = createContains(concat_1, (JcCollection) val_1, (Object[]) val_2, negate);
						} else if (op == Operator.IS_NULL)
							ret = booleanOp.IS_NULL();
						else if (op == Operator.LIKE)
							ret = booleanOp.REGEX(val_2.toString());
						else if (op == Operator.LT)
							ret = booleanOp.LT(val_2);
						else if (op == Operator.LTE)
							ret = booleanOp.LTE(val_2);
						
						if (i < cnt - 1)
							booleanOp = ret.OR().valueOf(val_1);
					}
					if (cnt > 1) { // encapsulate by brackets
						ret = ret.BR_CLOSE();
					}
				} else
					clausesPerType.valid = false;
				
				return ret;
			}
			
			private iot.jcypher.query.api.predicate.Concatenator createContains(
					Concat concat, JcCollection val_1, Object[] val_2,
					boolean negate) {
				JcValue x = new JcValue("x");
				iot.jcypher.query.api.predicate.Concatenator fx = null;
				if (val_2.length == 1) {
					if (concat != null)
						fx = concat.holdsTrue(I.forAny(x).IN(val_1).WHERE().valueOf(x).EQUALS(val_2[0]));
					else
						fx =	WHERE.holdsTrue(I.forAny(x).IN(val_1).WHERE().valueOf(x).EQUALS(val_2[0]));
				} else if (val_2.length > 1) {
					Concat concat_1;
					if (concat != null)
						concat_1 = concat.BR_OPEN();
					else
						concat_1 = WHERE.BR_OPEN();
					for (int i = 0; i < val_2.length;i++) {
						if (fx == null)
							fx = concat_1.holdsTrue(I.forAny(x).IN(val_1).WHERE().valueOf(x).EQUALS(val_2[i]));
						else
							fx = fx.AND().holdsTrue(I.forAny(x).IN(val_1).WHERE().valueOf(x).EQUALS(val_2[i]));
					}
					fx = fx.BR_CLOSE();
				}
				return fx;
			}

			private iot.jcypher.query.api.predicate.Concatenator createWhereIn(
					Concat concat, ValueElement val_1, List<JcNode> val_2, boolean not) {
				iot.jcypher.query.api.predicate.Concatenator booleanOp = null;
				Concat concat_1 = concat;
				if (val_2.isEmpty()) {
					iot.jcypher.query.ast.predicate.PredicateExpression px =
							(iot.jcypher.query.ast.predicate.PredicateExpression)APIObjectAccess.getAstNode(concat_1);
					IPredicateHolder ph = px.getLastPredicateHolder();
					BooleanValue bv = new BooleanValue(false);
					ph.setPredicate(bv);
					booleanOp = PFactory.createConcatenator((iot.jcypher.query.ast.predicate.PredicateExpression) ph)
							.BR_CLOSE();
				} else {
					int idx = 0;
					for (JcNode n : val_2) {
						if (idx == 0 && val_2.size() > 1) // encapsulate with brackets
							concat_1 = concat_1.BR_OPEN();
						if (not) {
							if (idx == 0)
								booleanOp = concat_1.valueOf(n).IS_NULL().OR().NOT().valueOf(val_1).IN_list(n).BR_CLOSE();
							else
								booleanOp = booleanOp.AND().BR_OPEN().valueOf(n).IS_NULL().OR().NOT().valueOf(val_1).IN_list(n).BR_CLOSE();
						} else {
							if (idx == 0)
								booleanOp = concat_1.NOT().valueOf(n).IS_NULL().AND().valueOf(val_1).IN_list(n).BR_CLOSE();
							else
								booleanOp.OR().BR_OPEN().NOT().valueOf(n).IS_NULL().AND().valueOf(val_1).IN_list(n).BR_CLOSE();
						}
						if ((idx == val_2.size() - 1) && val_2.size() > 1) // encapsulate with brackets
							booleanOp = booleanOp.BR_CLOSE();
						idx++;
					}
				}
				return booleanOp;
			}
			
			private TraversalResult createTraversalClauses(JcNode origEndNode,
					ClausesPerType clausesPerType,
					TraversalExpression travEx,
					ClauseBuilderContext cbContext, boolean copy,
					DomainObjectMatch<?> collOwner) {
				TraversalResult ret = new TraversalResult();
				ret.expressionParts = new ArrayList<List<IClause>>();
				String startBName = APIAccess.getBaseNodeName(travEx.getStart());
				String endNodeLabel = getMappingInfo().getLabelForClass(clausesPerType.domainObjectType);
				String origEndNodeName = ValueAccess.getName(origEndNode);
				
				List<StepClause> stepCls = new ArrayList<StepClause>();
				for (String validNodeName : cbContext.validNodes) {
					if (validNodeName.indexOf(startBName) == 0) {
						int tmpNodeIdx = cbContext.stepClausesCount + stepCls.size();
						// only valid clauses are returned
						List<StepClause> partStepCls = buildStepClauses(travEx, tmpNodeIdx, validNodeName,
								endNodeLabel, origEndNodeName, copy);
						List<IClause> part = new ArrayList<IClause>();
						for (StepClause sc : partStepCls) {
							part.add(SEPARATE.nextClause());
							part.add(sc.getTotalMatchNode());
						}
						ret.expressionParts.add(part);
						stepCls.addAll(partStepCls);
					}
				}
				cbContext.stepClausesCount = cbContext.stepClausesCount + stepCls.size();
				
				if (stepCls.size() == 1) { // don't need to build union of multiple sets
					StepClause sc = stepCls.get(0);
					JcNode endNd = sc.getEndNode();
					ValueAccess.setName(origEndNodeName,
							endNd);
					if (sc.jcPath != null) {
						ValueAccess.setName(pathFromNode(origEndNodeName), sc.jcPath);
						String startNodeName = ValueAccess.getName(sc.startNode);
						ret.addStartPath(startNodeName, sc.jcPath);
						ret.getClausesPerTypeList().add(
								new ClausesPerType(endNd, clausesPerType.domainObjectMatch,
										clausesPerType.domainObjectType));
						cbContext.addCountFor(collOwner, clausesPerType.domainObjectMatch,
								startNodeName, endNd);
					}
				} else if (stepCls.size() > 1) {
					List<JcNode> tempNodes = new ArrayList<JcNode>();
					for (StepClause sc : stepCls) {
						JcNode endNd = sc.getEndNode();
						tempNodes.add(endNd);
						if (sc.jcPath != null) {
							ValueAccess.setName(pathFromNode(
									ValueAccess.getName(endNd)), sc.jcPath);
							String startNodeName = ValueAccess.getName(sc.startNode);
							ret.addStartPath(startNodeName, sc.jcPath);
							ret.getClausesPerTypeList().add(
									new ClausesPerType(endNd, clausesPerType.domainObjectMatch,
											clausesPerType.domainObjectType));
							cbContext.addCountFor(collOwner, clausesPerType.domainObjectMatch,
									startNodeName, endNd);
						}
					}
					if (!copy) { // in this case the collection expression using
										// the copy works on each part-path in turn
										// therefore  we don't need to build the union
						List<IClause> part = new ArrayList<IClause>();
						part.add(SEPARATE.nextClause());
						part.add(OPTIONAL_MATCH.node(origEndNode).label(endNodeLabel));
						Concat concat = WHERE.BR_OPEN();
						part.add(createWhereIn(concat, origEndNode, tempNodes, false));
						ret.expressionParts.add(part);
					}
				} else {
					clausesPerType.valid = false;
//					throw new RuntimeException("attribute(s): " + invalidAttribs + " do(es) not exist " +
//							"in domain object type(s): " + inValidTypes);
				}
				return ret;
			}
			
			@SuppressWarnings("unchecked")
			private void createCollectClauses(ClausesPerType cpt, CollectExpression collEx,
					ClauseBuilderContext cbContext) {
				cpt.isCollectExpression = true;
				List<JcNode> nodes;
				Object hint = ValueAccess.getAnyHint(collEx.getAttribute(), APIAccess.hintKey_validNodes);
				if (hint instanceof List<?>) {
					nodes = (List<JcNode>) hint;
					nodes = cbContext.filterValidNodes(nodes);
				} else
					nodes = cbContext.filterValidNodes(APIAccess.getNodes(collEx.getStartDOM()));
				
				JcNode unionNode;
				JcValue val;
				if (nodes.size() > 0) {
					List<IClause> collectClauses = new ArrayList<IClause>();
					if (nodes.size() > 1) {
						String unionName = APIAccess.getBaseNodeName(cpt.domainObjectMatch).concat(collectNodePostfix);
						unionNode = new JcNode(unionName);
						collectClauses.add(OPTIONAL_MATCH.node(unionNode));
						collectClauses.add(createWhereIn(WHERE.BR_OPEN(), unionNode, nodes, false));
					} else
						unionNode = nodes.get(0);
					JcCollection unionColl = new JcCollection(ValueAccess.getName(unionNode));
					
					String tempName = APIAccess.getBaseNodeName(cpt.domainObjectMatch).concat(tmpNodePostPrefix);
					JcNode tempNode = new JcNode(tempName);
					ValueElement first = ValueAccess.findFirst(collEx.getAttribute());
					val = (JcValue)cloneVe(collEx.getAttribute(), first, tempNode);
					collectClauses.add(
							WITH.collection(C.EXTRACT().valueOf(val).fromAll(tempNode).IN_list(unionColl)).AS(cpt.node));
					cpt.withClausesAddIdxs = new ArrayList<Integer>();
					cpt.withClausesAddIdxs.add(collectClauses.size());
					cpt.collectionClauses = collectClauses;
					cpt.lastIsWith = true;
				} else
					cpt.valid = false;

				return;
			}
			
			private void createAddSelectClauses(ClausesPerType cpt, SelectExpression<?> selEx,
					ClauseBuilderContext cbContext) {
				// the select can return results only for valid nodes in start set,
				// because it selects from the start set.
				if (!cbContext.isNodeValid(APIAccess.getNodeForType(selEx.getStart(), cpt.domainObjectType))) {
					cpt.valid = false;
					return;
				}
				
				List<IClause> selectClauses = new ArrayList<IClause>();
				
				// get the DOMs which are derived by traversal and which occur
				// as start in a predicate expression within this collection expression.
				// The appropriate traversal expressions have been cloned, so that
				// the predicate expression does not constrain the result of the original
				// traversal expression.
				// now add the predicate expression(s) to the clones
				List<DomainObjectMatch<?>> travDOMs = selEx.getTraversalResults();
				Map<IASTObject, TraversalPathsPerDOM> astObject2TraversalPaths =
						new HashMap<IASTObject, TraversalPathsPerDOM>();
				List<IClause> countWithClauses = new ArrayList<IClause>();
				if (travDOMs != null) {
					for (DomainObjectMatch<?> dom : travDOMs) {
						int idx = 0;
						ExpressionsPerDOM xpd = null;
						ExpressionsPerDOM countXpd = null;
						TraversalPathsPerDOM tpd = new TraversalPathsPerDOM();
						// for all types of the DomainObjectMatch
						List<Class<?>> typeList = APIAccess.getTypeList(dom);
						for (int i = 0; i < typeList.size(); i++) {
							JcNode node = APIAccess.getNodes(dom).get(i);
							if (cbContext.isNodeValid(node)) {
								Class<?> clazz = typeList.get(i);
								// The TraversalResult contains the cloned traversal clauses
								// (cloned for this collection expression)
								TraversalResult travResult = cbContext.getTravResult(cpt.domainObjectMatch, dom, clazz);
								
								if (travResult != null) { // null in case of union
									// add a traversal expression clone only once for each collection expression
									if (travResult.expressionsPerDOM == null) {
										// find all expressions within the collection expression
										// which act on the DomainObjectmatch dom.
										if (xpd == null) {
											// without count expressions
											xpd = buildExpressionsPerDOM(dom, selEx.getAstObjects(), 1);
										}
										if (countXpd == null) {
											// only count expressions
											countXpd = buildExpressionsPerDOM(dom, selEx.getAstObjects(), 2);
										}
										if (countXpd != null && countXpd.xPressions.size() > 0)
											cpt.startCountSelectXpr = true;
										// also serves as marker, that the teraversal expression clone has been added
										travResult.expressionsPerDOM = xpd; 
										travResult.countExpressionsPerDOM = countXpd;
										
										tpd.addAll(travResult.getStartPathMap());
										int idx2 = 0;
										for (List<IClause> part : travResult.expressionParts) {
											if (!part.isEmpty()) {
												ClausesPerType iCpt = travResult.clausesPerTypeList.get(idx2);
												
												if (dom != iCpt.domainObjectMatch)
													throw new RuntimeException("internal error");
												
												// if there are predicate expressions which already
												// constrain the result set of the traversal expression
												// these expressions are put within brackets
												// and so are the predicate expressions added by
												// the collection expression
												boolean bracket = false;
												if (iCpt.concatenator != null) {
													iCpt.concat = iCpt.concatenator.BR_CLOSE().AND().BR_OPEN();
													iCpt.concatenator = null;
													bracket = true;
												} else
													iCpt.concat = WHERE.BR_OPEN();
												
												// the path is needed, when a predicate expression is
												// added, expressed on the result of a traversal expression
												// (then the constraint will be expressed on head(nodes(path)))
												boolean needPath = false;
												// without count expressions
												for (IASTObject ao : xpd.xPressions) {
													if (ao instanceof PredicateExpression) {
														needPath = true;
														PredicateExpression pred = (PredicateExpression)ao;
														IPredicateOperand1 val1 = pred.getValue_1();
														if (val1 instanceof ValueElement) {
															ValueElement ve = (ValueElement)val1;
															ve = cloneVe(ve, ValueAccess.findFirst(ve), iCpt.node);
															pred.setValue_1(ve);
														}
													}
													addClause(ao, iCpt, cbContext);
													if (idx == 0)
														astObject2TraversalPaths.put(ao, tpd);
												}
												if (needPath) {
													JcPath p = new JcPath(pathFromNode(ValueAccess.getName(iCpt.node)));
													countWithClauses.add(WITH.value(p));
												}
												if (bracket)
													iCpt.concatenator = iCpt.concatenator.BR_CLOSE();
												idx++;
												idx2++;
												selectClauses.addAll(part);
												if (iCpt.concatenator != null)
													selectClauses.add(iCpt.concatenator);
												selectClauses.add(SEPARATE.nextClause());
												
												// now the count expressions
												for (IASTObject ao : countXpd.xPressions) {
													if (ao instanceof PredicateExpression) {
														addCountWithClause(iCpt, countWithClauses, selectClauses, cpt);
													}
												}
											}
										}
									} else { // mark the IASTObjects which have already been processed
										tpd.addAll(travResult.getStartPathMap());
										// without count expressions, count expressions are handled differently
										for (IASTObject ao : travResult.expressionsPerDOM.xPressions) {
											astObject2TraversalPaths.put(ao, tpd);
										}
									}
								}
							}
						}
					}
				}
				
				String nodeLabel = getMappingInfo().getLabelForClass(cpt.domainObjectType);
				selectClauses.add(
					OPTIONAL_MATCH.node(cpt.node).label(nodeLabel)
				);
				Concat concat = WHERE.BR_OPEN();
				List<JcNode> nds = new ArrayList<JcNode>();
				JcNode nd = APIAccess.getNodeForType(selEx.getStart(), cpt.domainObjectType);
				if (cbContext.isNodeValid(nd))
					nds.add(nd);
				cpt.concatenator = createWhereIn(concat, cpt.node, nds, false);
				cbContext.stepClausesCount = 0;

				// scope: 0 .. constrain by head(nodes(path))
				// 1 .. constraint on source set
				// 2 .. count expression
				// 3 .. finish (close open brackets)
				// 4 .. to force scope change
				int scope = -1;
				List<IASTObject> pendingBrOpen = new ArrayList<IASTObject>();
				List<IASTObject> pendingBrClose = new ArrayList<IASTObject>();
				IASTObject pendingOr = null;
				if (selEx.getAstObjects().size() > 1) {
					// everything after the 'containment in source expression'
					// are additional constraints and if more than one must be within brackets
					addClause(new ConcatenateExpression(Concatenator.BR_OPEN), cpt, cbContext);
				}
				for (IASTObject ao : selEx.getAstObjects()) {
					TraversalPathsPerDOM tpd = astObject2TraversalPaths.get(ao);
					if (tpd != null) { // constraint defined by a traversal expression
						// test if it was not additionally created as part of a count expression
						// because in that case the constraint is expressed in form of the count
						if (!(ao instanceof PredicateExpression && ((PredicateExpression)ao).isPartOfCount())) {
							if (!tpd.consumed) {
								tpd.consumed = true;
								// force a scope change
								int oldScope = (scope == 0) ? 4 : scope;
								scope = handleScopeClose(oldScope, 0, cpt, cbContext, pendingBrClose);
								pendingOr = handleScopeOpen(oldScope, 0, cpt, cbContext, pendingBrOpen, pendingOr);
								// add clauses for the traversals (match path start)
								// as head(nodes(path))
								addTraversalConstraints2Select(tpd, nds, cpt);
								// brClose done by handleScopeClose()
							}
						}
					} else {
						// count expressions are handled differently
						if (ao instanceof PredicateExpression &&
								((PredicateExpression)ao).getValue_1() instanceof Count) {
							List<JcNode> countNodes = cbContext.getCountsFor(cpt.domainObjectMatch,
									((PredicateExpression)ao).getStartDOM(), ValueAccess.getName(nd));
							int oldScope = scope;
							scope = handleScopeClose(scope, 2, cpt, cbContext, pendingBrClose);
							pendingOr = handleScopeOpen(oldScope, 2, cpt, cbContext, pendingBrOpen, pendingOr);
							if (pendingOr != null) {
								addClause(pendingOr, cpt, cbContext);
								pendingOr = null;
							}
							JcNumber prevNum = null;
							for (JcNode n : countNodes) {
								String alias = ValueAccess.getName(n).concat(countXprPostPrefix);
								JcNumber num = new JcNumber(alias);
								if (prevNum != null)
									prevNum = prevNum.plus(num);
								else
									prevNum = num;
							}
							PredicateExpression newPred = ((PredicateExpression)ao).createCopy();
							newPred.setValue_1(prevNum);
							addClause(newPred, cpt, cbContext);
							// brClose done by handleScopeClose()
						} else {
							if (ao instanceof ConcatenateExpression) {
								if (((ConcatenateExpression)ao).getConcatenator() == Concatenator.OR) {
									pendingOr = ao;
								} else if (((ConcatenateExpression)ao).getConcatenator() == Concatenator.BR_OPEN) {
									pendingBrOpen.add(ao);
								} else if (((ConcatenateExpression)ao).getConcatenator() == Concatenator.BR_CLOSE) {
									pendingBrClose.add(ao);
								}
							} else if (ao instanceof PredicateExpression) {
								int oldScope = scope;
								scope = handleScopeClose(scope, 1, cpt, cbContext, pendingBrClose);
								pendingOr = handleScopeOpen(oldScope, 1, cpt, cbContext, pendingBrOpen, pendingOr);
								handleBrackets(cpt, cbContext, pendingBrClose);
								if (pendingOr != null) {
									addClause(pendingOr, cpt, cbContext);
									pendingOr = null;
								}
								handleBrackets(cpt, cbContext, pendingBrOpen);
								addClause(ao, cpt, cbContext);
								// the expression is valid within the select expression,
								// as it must be expressed either on the source set
								// or on a set directly derived from the source set by traversal;
								// we must avoid testing validity afterwards
								cpt.testForNextIsOr = false;
							}
						}
						if(!cpt.valid)
							break;
					}
				}
				
				handleScopeClose(scope, 3, cpt, cbContext, pendingBrClose);
				if (selEx.getAstObjects().size() > 1)
					addClause(new ConcatenateExpression(Concatenator.BR_CLOSE), cpt, cbContext);
				
				cpt.collectionClauses = selectClauses;
			}
			
			private void handleBrackets(ClausesPerType cpt,
					ClauseBuilderContext cbContext, List<IASTObject> pendingBr) {
				if (!pendingBr.isEmpty()) {
					for (IASTObject iao : pendingBr)
						addClause(iao, cpt, cbContext);
					pendingBr.clear();
				}
			}
			
			private int handleScopeClose(int scope, int newScope, ClausesPerType cpt,
					ClauseBuilderContext cbContext, List<IASTObject> pendingBrClose) {
				if (scope != -1 && scope != newScope) {
					if (!pendingBrClose.isEmpty()) {
						for (IASTObject iao : pendingBrClose)
							addClause(iao, cpt, cbContext);
						pendingBrClose.clear();
					} else
						addClause(new ConcatenateExpression(Concatenator.BR_CLOSE), cpt, cbContext);
				}
				return newScope;
			}
			
			private IASTObject handleScopeOpen(int scope, int newScope, ClausesPerType cpt,
					ClauseBuilderContext cbContext, List<IASTObject> pendingBrOpen,
					IASTObject pendingOr) {
				if (scope != newScope) {
					if (pendingOr != null)
						addClause(pendingOr, cpt, cbContext);
					if (!pendingBrOpen.isEmpty()) {
						for (IASTObject iao : pendingBrOpen)
							addClause(iao, cpt, cbContext);
						pendingBrOpen.clear();
					} else
						addClause(new ConcatenateExpression(Concatenator.BR_OPEN), cpt, cbContext);
					return null;
				} else
					return pendingOr;
			}

			private void addCountWithClause(ClausesPerType iCpt, List<IClause> countWithClauses,
					List<IClause> selectClauses, ClausesPerType cpt) {
				String nnm = ValueAccess.getName(iCpt.node);
				String alias = nnm.concat(countXprPostPrefix);
				JcNumber num = new JcNumber(alias);
				selectClauses.add(WITH.count().DISTINCT().value(iCpt.node).AS(num));
				selectClauses.addAll(countWithClauses);
				if (cpt.withClausesAddIdxs == null)
					cpt.withClausesAddIdxs = new ArrayList<Integer>();
				cpt.withClausesAddIdxs.add(0, selectClauses.size());
				countWithClauses.add(0, WITH.value(num));
			}

			private void addTraversalConstraints2Select(TraversalPathsPerDOM tpd,
					List<JcNode> startNds, ClausesPerType cpt) {
				List<JcPath> paths = tpd.getAllPaths(startNds);
				if (paths.size() > 0) {
					cpt.oneValid = true;
					boolean bracket = false;
					if (cpt.concatenator != null) {
						cpt.concat = cpt.concatenator.AND().BR_OPEN();
						bracket = true;
					}
					int idx = 0;
					for(JcPath path : paths) {
						if (idx > 0)
							cpt.concatenator =
									cpt.concatenator.OR().valueOf(cpt.node).EQUALS(path.nodes().head());
						else
							cpt.concatenator =
								cpt.concat.valueOf(cpt.node).EQUALS(path.nodes().head());
						idx++;
					}
					if (bracket)
						cpt.concatenator =
								cpt.concatenator.BR_CLOSE();
				}
			}
			
			private List<StepClause> buildStepClauses(TraversalExpression travEx, int tmpNodeIdx, String startNodeName,
					String endNodeLabel, String origEndNodeName, boolean copy) {
				StepClause stepClause = new StepClause();
				stepClause.buildAll(travEx, tmpNodeIdx, startNodeName, endNodeLabel,
						origEndNodeName, copy);
				
				return this.filterValidClauses(stepClause.stepClauses);
			}
			
			private List<StepClause> filterValidClauses(List<StepClause> toFilter) {
				List<StepClause> ret = new ArrayList<StepClause>();
				for(StepClause sc : toFilter) {
					if (sc.getTotalMatchNode() != null) {
						boolean doAdd = true;
						for (StepClause scRet : ret) {
							if (scRet.isSameAs(sc)) {
								doAdd = false;
								break;
							}
						}
						if (doAdd)
							ret.add(sc);
					}
				}
				return ret;
			}
			
			private String pathFromNode(String nodeName) {
				return nodeName.replaceFirst(APIAccess.nodePrefix, APIAccess.pathPrefix);
			}
			
			/***************************/
			private class TraversalPathsPerDOM {
				// key is the start node name
				private Map<String, List<JcPath>> pathMap;
				private boolean consumed;
				
				private TraversalPathsPerDOM() {
					super();
					this.pathMap = new HashMap<String, List<JcPath>>();
					this.consumed = false;
				}
				
				private void addAll(Map<String, List<JcPath>> pMap) {
					Iterator<Entry<String, List<JcPath>>> it = pMap.entrySet().iterator();
					while(it.hasNext()) {
						Entry<String, List<JcPath>> entry = it.next();
						List<JcPath> pths = this.pathMap.get(entry.getKey());
						if (pths == null) {
							pths = new ArrayList<JcPath>();
							this.pathMap.put(entry.getKey(), pths);
						}
						pths.addAll(entry.getValue());
					}
				}
				
				private List<JcPath> getAllPaths(List<JcNode> nodes) {
					List<JcPath> ret = new ArrayList<JcPath>();
					for (JcNode n : nodes) {
						List<JcPath> pths = this.pathMap.get(ValueAccess.getName(n));
						if (pths != null)
							ret.addAll(pths);
					}
					return ret;
				}
			}
			
			/***************************/
			private class TraversalResult {
				private List<List<IClause>> expressionParts;
				// key is the start node name
				private Map<String, List<JcPath>> startPathMap;
				private List<ClausesPerType> clausesPerTypeList;
				private ExpressionsPerDOM expressionsPerDOM;
				private ExpressionsPerDOM countExpressionsPerDOM;
				
				private List<IClause> getAllClauses() {
					List<IClause> ret = new ArrayList<IClause>();
					for (List<IClause> cls : this.expressionParts) {
						ret.addAll(cls);
					}
					return ret;
				}
				
				private Map<String, List<JcPath>> getStartPathMap() {
					if (this.startPathMap == null)
						this.startPathMap = new HashMap<String, List<JcPath>>();
					return this.startPathMap;
				}
				
				private void addStartPath(String startNodeName, JcPath startPath) {
					List<JcPath> pths = this.getStartPathMap().get(startNodeName);
					if (pths == null) {
						pths = new ArrayList<JcPath>();
						this.getStartPathMap().put(startNodeName, pths);
					}
					pths.add(startPath);
				}
				
				private List<ClausesPerType> getClausesPerTypeList() {
					if (this.clausesPerTypeList == null)
						this.clausesPerTypeList = new ArrayList<ClausesPerType>();
					return this.clausesPerTypeList;
				}
			}
			
			/***************************/
			private class StepClause {
				private Node matchNode;
				private StepClause next;
				private StepClause previous;
				private List<FieldMapping> fieldMappings;
				private int fmIndex;
				private TraversalExpression traversalExpression;
				private Step step;
				private int stepIndex;
				private String originalEndNodeName;
				private String endNodeLabel;
				private JcPath jcPath;
				private JcNode startNode;
				private JcNode endNode;
				private List<StepClause> stepClauses;
				
				private void buildAll(TraversalExpression travEx, int tmpNodeIdx, String startNodeName, String endNodeLabel,
						String origEndNodeName, boolean copy) {
					this.stepClauses = new ArrayList<StepClause>();
					this.originalEndNodeName = origEndNodeName;
					this.endNodeLabel = endNodeLabel;
					this.traversalExpression = travEx;
					this.buildFirst(tmpNodeIdx, startNodeName, copy);
				}
				
				private void buildFirst(int tmpNodeIdx, String startNodeName, boolean copy) {
					this.stepClauses.add(this);
					this.startNode = new JcNode(startNodeName);
					if (copy) {
						this.jcPath = new JcPath(pathFromNode(this.originalEndNodeName));
						this.matchNode = OPTIONAL_MATCH.path(this.jcPath).node(this.startNode);
					} else
						this.matchNode = OPTIONAL_MATCH.node(this.startNode);
					Class<?> typ = APIAccess.getTypeForNodeName(this.traversalExpression.getStart(), startNodeName);
					boolean isList = typ.equals(Collection.class) ||
							typ.equals(Array.class); // TODO what about other surrogates
					this.stepIndex = 0;
					this.step = this.traversalExpression.getSteps().get(this.stepIndex);
					List<Class<?>> types = new ArrayList<Class<?>>();
					types.add(typ);
					this.build(tmpNodeIdx, types, isList);
				}
				
				private void buildNext(int tmpNodeIdx, List<Class<?>> types, boolean isList,
						Step nextStep, int nextStepIndex) {
					StepClause stpc = new StepClause();
					this.next = stpc;
					stpc.previous = this;

					stpc.matchNode = this.matchNode;
					stpc.endNodeLabel = this.endNodeLabel;
					stpc.originalEndNodeName = this.originalEndNodeName;
					stpc.traversalExpression = this.traversalExpression;
					stpc.step = nextStep;
					stpc.stepClauses = this.stepClauses;
					stpc.stepIndex = nextStepIndex;
					stpc.build(tmpNodeIdx, types, isList);
				}
				
				private void buildNextClone(int tmpNodeIdx, List<Class<?>> types, boolean isList,
						CloneInfo cloneInfo) {
					cloneInfo.toClone = cloneInfo.toClone.next;
					StepClause stpc = new StepClause();
					this.next = stpc;
					stpc.previous = this;

					stpc.matchNode = this.matchNode;
					stpc.endNodeLabel = this.endNodeLabel;
					stpc.originalEndNodeName = this.originalEndNodeName;
					stpc.traversalExpression = this.traversalExpression;
					stpc.stepClauses = this.stepClauses;
					stpc.stepIndex = cloneInfo.toClone.stepIndex;
					stpc.step = cloneInfo.toClone.step;
					stpc.fieldMappings = cloneInfo.toClone.fieldMappings;
					stpc.fmIndex = cloneInfo.toClone.fmIndex;
					stpc.buildClone(tmpNodeIdx, cloneInfo, isList);
				}
				
				private void build(int tmpNodeIdx, List<Class<?>> types, boolean isList) {
					List<FieldMapping> fms = null;
					for (Class<?> t : types) {
						if (this.step.getDirection() == 0) { // forward
							FieldMapping fm = getMappingInfo().getFieldMapping(this.step.getAttributeName(),
									t);
							if (fm != null) {
								if (fms == null)
									fms = new ArrayList<FieldMapping>();
								fms.add(fm);
							}
						} else { // backward
							if (fms == null)
								fms = new ArrayList<FieldMapping>();
							List<FieldMapping> fms_t = getMappingInfo().getBackwardFieldMappings(this.step.getAttributeName(),
									t);
							for (FieldMapping f : fms_t) {
								if (!fms.contains(f))
									fms.add(f);
							}
						}
					}
					
					boolean doBuild = false;
					if (fms != null && !fms.isEmpty()) {
						this.fieldMappings = fms;
						this.fmIndex = 0;
						doBuild = true;
					} else {
						// navigation has no result
						this.matchNode = null;
					}
					
					if (doBuild) {
						if (this.step.getDirection() == 0) // forward
							this.buildStep(tmpNodeIdx, isList, null, true);
						else
							this.buildStep(tmpNodeIdx, isList, null, false);
					}
					
					// test for additional paths
					if (this.fieldMappings != null && this.fmIndex < this.fieldMappings.size() - 1) {
						this.cloneTraversal(this, tmpNodeIdx + 1);
					}
				}
				
				private void buildStep(int tmpNodeIdx, boolean listOrArray, CloneInfo cloneInfo,
						boolean forward) {
					FieldMapping fm = this.fieldMappings.get(this.fmIndex);
					
					Relation matchRel = matchNode.relation().type(fm.getPropertyOrRelationName());
					if (forward)
						matchRel = matchRel.out();
					else
						matchRel = matchRel.in();
					
					if (this.step.getMinDistance() != 1)
						matchRel = matchRel.minHops(this.step.getMinDistance());
					if (this.step.getMaxDistance() != 1) {
						if (this.step.getMaxDistance() == -1)
							matchRel = matchRel.maxHopsUnbound();
						else
							matchRel = matchRel.maxHops(this.step.getMaxDistance());
					}
					
					Class<?> typ;
					CompoundObjectType cType = null;
					if (forward) {
						if (listOrArray) {
							cType = getMappingInfo().getInternalDomainAccess()
									.getFieldComponentType(fm.getClassFieldName());
						} else {
							cType = getMappingInfo().getInternalDomainAccess()
									.getConcreteFieldType(fm.getClassFieldName());
						}
						typ = cType.getType();
					} else { // backward
						typ = fm.getField().getDeclaringClass();
					}
					
					boolean isList = typ.equals(Collection.class) ||
							typ.equals(Array.class); // TODO alternative check for surrogate
					Step nextStep = null;
					int nextStepIndex = this.stepIndex;
					List<Class<?>> types;
					if (isList) { // need to advance one step
						// surrogates have one (non-transient) field
						if (forward) {
							nextStep = this.step.createStep(this.step.getDirection(),
									getMappingInfo().getObjectMappingFor(typ)
										.fieldMappingsIterator().next().getPropertyOrRelationName());
						} else { // backward
							nextStep = this.step.createStep(this.step.getDirection(),
									this.step.getAttributeName());
						}
						types = new ArrayList<Class<?>>();
						types.add(typ);
					} else {
						nextStepIndex++;
						if (nextStepIndex <= this.traversalExpression.getSteps().size() - 1)
							nextStep = this.traversalExpression.getSteps().get(nextStepIndex);
						if (forward)
							types = cType.getTypes(true);
						else
							types = getMappingInfo().getCompoundTypesFor(typ);
					}
					
					if (nextStep == null) { // we have reached the end
						if (isValidEndNodeType(types)) {
							this.endNode = new JcNode(this.originalEndNodeName.concat(tmpNodePostPrefix)
									.concat(String.valueOf(tmpNodeIdx)));
							StepClause first = this.getFirst();
							if (first.jcPath != null) {
								String npm = ValueAccess.getName(first.jcPath).concat(tmpNodePostPrefix)
										.concat(String.valueOf(tmpNodeIdx));
								ValueAccess.setName(npm, first.jcPath);
							}
							this.matchNode = matchRel.node(this.endNode).label(this.endNodeLabel);
						} else
							this.matchNode = null;
					} else {
						this.matchNode = matchRel.node();
						if (cloneInfo != null)
							this.buildNextClone(tmpNodeIdx, types, isList, cloneInfo);
						else
							this.buildNext(tmpNodeIdx, types, isList, nextStep, nextStepIndex);
					}
				}
				
				private boolean isValidEndNodeType(List<Class<?>> types) {
					for (Class<?> clazz : types) {
						if (this.endNodeLabel.equals(getMappingInfo().getLabelForClass(clazz)))
							return true;
					}
					return false;
				}

				private void cloneTraversal(StepClause cloneTo, int tmpNodeIdx) {
					StepClause first = this.getFirst();
					StepClause newFirst = new StepClause();
					newFirst.stepClauses = first.stepClauses;
					CloneInfo cloneInfo = new CloneInfo();
					cloneInfo.toClone = first;
					cloneInfo.cloneTo = cloneTo;
					newFirst.cloneFirst(cloneInfo, tmpNodeIdx);
				}
				
				private void cloneFirst(CloneInfo cloneInfo, int tmpNodeIdx) {
					this.stepClauses.add(this);
					this.startNode = cloneInfo.toClone.startNode;
					this.jcPath = cloneInfo.toClone.jcPath;
					this.originalEndNodeName = cloneInfo.toClone.originalEndNodeName;
					this.endNodeLabel = cloneInfo.toClone.endNodeLabel;
					this.traversalExpression = cloneInfo.toClone.traversalExpression;
					if (this.jcPath != null)
						this.matchNode = OPTIONAL_MATCH.path(this.jcPath).node(this.startNode);
					else
						this.matchNode = OPTIONAL_MATCH.node(this.startNode);
					Class<?> typ = APIAccess.getTypeForNodeName(this.traversalExpression.getStart(),
							ValueAccess.getName(this.startNode));
					boolean isList = typ.equals(Collection.class) ||
							typ.equals(Array.class); // TODO what about other surrogates
					this.stepIndex = cloneInfo.toClone.stepIndex;
					this.step = cloneInfo.toClone.step;
					this.fieldMappings = cloneInfo.toClone.fieldMappings;
					this.fmIndex = cloneInfo.toClone.fmIndex;
					this.buildClone(tmpNodeIdx, cloneInfo, isList);
				}
				
				private void buildClone(int tmpNodeIdx, CloneInfo cloneInfo, boolean isList) {
					CloneInfo cli = cloneInfo;
					if (cli.stopCloning()) {
						this.fmIndex++;
						cli = null;
					}
					
					if (this.step.getDirection() == 0) // forward
						this.buildStep(tmpNodeIdx, isList, cli, true);
					else
						this.buildStep(tmpNodeIdx, isList, cli, false);
					
					// test for additional paths
					if (this.fieldMappings != null && this.fmIndex < this.fieldMappings.size() - 1) {
						this.cloneTraversal(this, tmpNodeIdx + 1);
					}
				}

				private JcNode getEndNode() {
					return this.getLast().endNode;
				}
				
				private Node getTotalMatchNode() {
					return this.getLast().matchNode;
				}
				
				private StepClause getFirst() {
					StepClause first = this;
					while(first.previous != null) {
						first = first.previous;
					}
					return first;
				}
				
				private StepClause getLast() {
					StepClause last = this;
					while(last.next != null) {
						last = last.next;
					}
					return last;
				}
				
				private boolean isSameAs(StepClause toCompare) {
					if (toCompare == null)
						return false;
					
					if (!this.originalEndNodeName.equals(toCompare.originalEndNodeName))
						return false;
					
					if (this.startNode != null) {
						if (toCompare.startNode == null)
							return false;
						if (!ValueAccess.getName(this.startNode).equals(
								ValueAccess.getName(toCompare.startNode)))
							return false;
					} else if (toCompare.startNode != null)
						return false;
					
					String relName = getFieldMapping().getPropertyOrRelationName();
					String oRelName = toCompare.getFieldMapping().getPropertyOrRelationName();
					if (!relName.equals(oRelName))
						return false;
					
					if (this.next != null)
						return this.next.isSameAs(toCompare.next);
					else if (toCompare.next != null)
						return false;
					return true;
				}

				private FieldMapping getFieldMapping() {
					if (this.fieldMappings != null)
						return this.fieldMappings.get(this.fmIndex);
					return null;
				}
			}
			
			/***********************************/
			private class CloneInfo {
				private StepClause toClone;
				private StepClause cloneTo;
				
				private boolean stopCloning() {
					return this.toClone == this.cloneTo;
				}
			}
		}
		
		/*************************************/
		private class ClauseBuilderContext {
			private int stepClausesCount = 0;
			private List<String> validNodes;
			
			// The DomainObjectMatch as Map-Key is owner of the collection expression;
			// the one which is produced by the collection expression.
			// The Class which is key in the inner map is one type of the DomainObjectMatch which is owner
			// of the tarversal expression (produced by the traversal expression)
			// collection owner -> traversal owner -> TraversalResultsPerType
			private Map<DomainObjectMatch<?>,
				Map<DomainObjectMatch<?>, TraversalResultPerTypes>> travClausesPerCollectXpr;
			
			private ClauseBuilderContext(List<String> validNodes) {
				super();
				this.validNodes = validNodes;
			}
			
			private void addCountFor(DomainObjectMatch<?> collOwner, DomainObjectMatch<?> travOwner,
					String startNodeName, JcNode endNd) {
				TraversalResultPerTypes travResPerTypes = this.getTraversalResultPerTypes(collOwner, travOwner, true);
				travResPerTypes.addCount(startNodeName, endNd);
			}
			
			private List<JcNode> getCountsFor(DomainObjectMatch<?> collOwner, DomainObjectMatch<?> travOwner,
					String startNodeName) {
				UnionExpression ue;
				TraversalResultPerTypes travResPerTypes;
				if ((ue = APIAccess.getUnionExpression(travOwner)) != null) {
					List<JcNode> ret = new ArrayList<JcNode>();
					for (DomainObjectMatch<?> dom : ue.getSources()) {
						travResPerTypes = this.getTraversalResultPerTypes(collOwner, dom, false);
						if (travResPerTypes != null)
							ret.addAll(travResPerTypes.getCountsFor(startNodeName));
					}
					return ret;
				}
				travResPerTypes = this.getTraversalResultPerTypes(collOwner, travOwner, false);
				if (travResPerTypes != null)
					return travResPerTypes.getCountsFor(startNodeName);
				return null;
			}

			private void addTravClausesPerColl(DomainObjectMatch<?> collOwner,
					DomainObjectMatch<?> travOwner, Class<?> travOwnerType, ClauseBuilder.TraversalResult travResult) {
				TraversalResultPerTypes travResPerTypes = this.getTraversalResultPerTypes(collOwner, travOwner, true);
				travResPerTypes.addTravResult(travOwnerType, travResult);
			}
			
			private TraversalResultPerTypes getTraversalResultPerTypes(DomainObjectMatch<?> collOwner,
					DomainObjectMatch<?> travOwner, boolean doCreate) {
				if (this.travClausesPerCollectXpr == null) {
					if (!doCreate)
						return null;
					this.travClausesPerCollectXpr = new HashMap<DomainObjectMatch<?>,
							Map<DomainObjectMatch<?>, TraversalResultPerTypes>>();
				}
				Map<DomainObjectMatch<?>, TraversalResultPerTypes> travResOwnerMap = this.travClausesPerCollectXpr.get(collOwner);
				if (travResOwnerMap == null) {
					if (!doCreate)
						return null;
					travResOwnerMap = new HashMap<DomainObjectMatch<?>, TraversalResultPerTypes>();
					this.travClausesPerCollectXpr.put(collOwner, travResOwnerMap);
				}
				TraversalResultPerTypes travResPerTypes = travResOwnerMap.get(travOwner);
				if (travResPerTypes == null) {
					if (!doCreate)
						return null;
					travResPerTypes = new TraversalResultPerTypes();
					travResOwnerMap.put(travOwner, travResPerTypes);
				}
				return travResPerTypes;
			}
			
			private ClauseBuilder.TraversalResult getTravResult(DomainObjectMatch<?> collOwner,
					DomainObjectMatch<?> travOwner, Class<?> travOwnerType) {
				TraversalResultPerTypes travResPerTypes = this.getTraversalResultPerTypes(collOwner, travOwner, false);
				if (travResPerTypes != null)
					return travResPerTypes.getTravResult(travOwnerType);
				return null;
			}
			
			private boolean isNodeValid(JcNode nd) {
				String ndName = ValueAccess.getName(nd);
				for (String n : this.validNodes) {
					if (n.equals(ndName))
						return true;
				}
				return false;
			}
			
			private List<JcNode> filterValidNodes(List<JcNode> nds) {
				List<JcNode> ret = new ArrayList<JcNode>();
				for (JcNode n : nds) {
					if (this.isNodeValid(n))
						ret.add(n);
				}
				return ret;
			}
			
			/*************************************/
			private class TraversalResultPerTypes {
				
				private Map<Class<?>, ClauseBuilder.TraversalResult> travResultsPerType;
				// traversal start node name -> list of traversal end nodes
				private Map<String, List<JcNode>> startNode2CountMap;
				
				private void addCount(String startNodeName, JcNode endNode) {
					if (this.startNode2CountMap == null)
						this.startNode2CountMap = new HashMap<String, List<JcNode>>();
					List<JcNode> endNodes = this.startNode2CountMap.get(startNodeName);
					if (endNodes == null) {
						endNodes = new ArrayList<JcNode>();
						this.startNode2CountMap.put(startNodeName, endNodes);
					}
					endNodes.add(endNode);
				}
				
				private List<JcNode> getCountsFor(String startNodeName) {
					if (this.startNode2CountMap != null)
						return this.startNode2CountMap.get(startNodeName);
					return null;
				}
				
				private void addTravResult(Class<?> travOwnerType, ClauseBuilder.TraversalResult traversalresult) {
					if (this.travResultsPerType == null)
						this.travResultsPerType = new HashMap<Class<?>, ClauseBuilder.TraversalResult>();
					this.travResultsPerType.put(travOwnerType, traversalresult);
				}
				
				private ClauseBuilder.TraversalResult getTravResult(Class<?> travOwnerType) {
					if (this.travResultsPerType != null)
						return this.travResultsPerType.get(travOwnerType);
					return null;
				}
			}
		}
		
		/*************************************/
		private class StateContext {
			private State state;
			private int blockCount;
			private String baseNodeName;
			private List<IASTObject> candidates;
			private List<String> dependencies;
			
			// -1 .. init,
			// 0 .. foreign predicate expression,
			private int orRemoveState;
			private IASTObject orToAdd;
			
			private StateContext(String bndNm) {
				super();
				this.state = State.INIT;
				this.blockCount = 0;
				this.candidates = new ArrayList<IASTObject>();
				this.orRemoveState = -1;
				this.baseNodeName = bndNm;
			}
			
			private void addDependency(String nodeName) {
				if (nodeName != null) {
					String nn = nodeName;
					if (this.dependencies == null)
						this.dependencies = new ArrayList<String>();
					int idx = nodeName.indexOf(separator);
					idx = nodeName.indexOf(separator, idx + 1);
					// extract base-node-name if needed
					if (idx >= 0)
						nn = nodeName.substring(0, idx);
					if (!nn.equals(this.baseNodeName) && !this.dependencies.contains(nn))
						this.dependencies.add(nn);
				}
			}
		}
		
		/*************************************/
		private class ClausesPerType {
			private DomainObjectMatch<?> domainObjectMatch;
			private JcNode node;
			private Class<?> domainObjectType;
			private boolean valid;
			private boolean oneValid;
			private boolean previousOr;
			private boolean testForNextIsOr;
			private int pageOffset;
			private int pageLength;
			private List<IClause> clauses;
			private ExpressionsPerDOM expressionsPerDOM;
			private Concat concat = null;
			private iot.jcypher.query.api.predicate.Concatenator concatenator = null;
			private List<IClause> traversalClauses;
			private List<IClause> collectionClauses;
			private boolean lastIsWith;
			private boolean isCollectExpression;
			private boolean startCountSelectXpr;
			private List<Integer> withClausesAddIdxs;
			private boolean closeBracket;
			
			private ClausesPerType(JcNode node, DomainObjectMatch<?> dom, Class<?> type) {
				super();
				this.node = node;
				this.domainObjectMatch = dom;
				this.domainObjectType = type;
				this.valid = true;
				this.oneValid = false;
				this.previousOr = false;
				this.testForNextIsOr = false;
				this.closeBracket = false;
				this.startCountSelectXpr = false;
				this.isCollectExpression = false;
				this.lastIsWith = false;
			}
			
			private boolean needDistinctQuery() {
				return this.pageOffset > 0 || this.pageLength >= 0;
			}
			
			private JcNumber getJcNumber(String prefix) {
				return QueryBuilder.this.getJcNumber(prefix, this.node);
			}
			
			/**
			 * may return null in case it is not valid
			 * @return
			 */
			private List<IClause> getClauses(List<ClausesPerType> added) {
				if (this.clauses == null) {
					if (this.valid) {
						this.clauses = new ArrayList<IClause>();
						if (this.traversalClauses != null) {
							if (this.concatenator != null) {
								IClause cl = this.traversalClauses.get(
										this.traversalClauses.size() - 1);
								if (cl instanceof iot.jcypher.query.api.predicate.Concatenator)
									this.traversalClauses.remove(this.traversalClauses.size() - 1);
							}
							this.clauses.addAll(this.traversalClauses);
							this.traversalClauses = null;
						} else if (this.collectionClauses != null) {
							int offset = this.clauses.size();
							this.clauses.addAll(this.collectionClauses);
							if (this.withClausesAddIdxs != null) {
								// add missing with clauses
								for (int idx : this.withClausesAddIdxs) {
									for (ClausesPerType cpt : added) {
										this.clauses.add(idx + offset, WITH.value(cpt.node));
									}
								}
							}
						} else {
							String nodeLabel = getMappingInfo().getLabelForClass(this.domainObjectType);
							this.clauses.add(
								OPTIONAL_MATCH.node(this.node).label(nodeLabel)
							);
						}
						if (this.concatenator != null)
							this.clauses.add(this.closeBracket ? this.concatenator.BR_CLOSE() :
								this.concatenator);
						else
							this.clauses.add(SEPARATE.nextClause());
					}
				}
				return this.clauses;
			}
			
			private void addClausesTo(List<IClause> clauses, List<ClausesPerType> added) {
				List<IClause> mcs = this.getClauses(added);
				if (mcs != null)
					clauses.addAll(mcs);
			}
			
			private void addDependencyClauses(List<IClause> clauses, List<ClausesPerType> added,
					OrderClausesHolder withClausesHolder, ClauseBuilderContext cbContext,
					boolean isCountQuery) {
				if (this.expressionsPerDOM.flattenedDependencies != null) {
					for (ExpressionsPerDOM xpd : this.expressionsPerDOM.flattenedDependencies) {
						// for a traversal expression for this select expression we do not need to
						// add the original traversal expression, because the select expression
						// will work on the clones.
						List<DomainObjectMatch<?>> collOwner = APIAccess.getCollectExpressionOwner(xpd.domainObjectMatch);
						boolean addExpressions = collOwner == null || !collOwner.contains(this.domainObjectMatch);
						for (ClausesPerType cpt : xpd.clausesPerTypes) {
							if (cbContext.isNodeValid(cpt.node)) {
								if (!added.contains(cpt)) {
									if (addExpressions) {
										if (!isCountQuery)
											withClausesHolder.checkForOrdeClause(cpt);
										cpt.addClausesTo(clauses, added);
										added.add(cpt);
									}
//									added.add(cpt);
								}
							}
						}
					}
				}
			}

			private boolean isValidDependency(String vn,
					List<String> depBases) {
				for (String bnm : depBases) {
					if (vn.startsWith(bnm))
						return true;
				}
				return false;
			}
		}
		
		/*************************************/
		private class ExpressionsPerDOM {
			private DomainObjectMatch<?> domainObjectMatch;
			private List<IASTObject> xPressions;
			private List<DomainObjectMatch<?>> dependencies;
			private List<ExpressionsPerDOM> flattenedDependencies;
			private List<ClausesPerType> clausesPerTypes;
			
			private ExpressionsPerDOM(DomainObjectMatch<?> domainObjectMatch,
					List<IASTObject> xPressions, List<String> deps) {
				super();
				this.domainObjectMatch = domainObjectMatch;
				this.xPressions = xPressions;
				if (deps != null) {
					this.dependencies = new ArrayList<DomainObjectMatch<?>>(deps.size());
					for (String nn : deps) {
						for (DomainObjectMatch<?> dom : domainObjectMatches) {
							if (APIAccess.getBaseNodeName(dom).equals(nn)) {
								this.dependencies.add(dom);
								break;
							}
						}
					}
				}
			}
			
			private void addDependencies(List<ExpressionsPerDOM> xpds) {
				if (xpds != null) {
					if (this.flattenedDependencies == null) {
						this.flattenedDependencies = new ArrayList<ExpressionsPerDOM>();
						this.flattenedDependencies.addAll(xpds);
					} else {
						for (ExpressionsPerDOM xpd : xpds) {
							if (!this.flattenedDependencies.contains(xpd))
								this.flattenedDependencies.add(xpd);
						}
					}
				}
			}
			
			private void addDependency(ExpressionsPerDOM xpd) {
				if (this.flattenedDependencies == null)
					this.flattenedDependencies = new ArrayList<ExpressionsPerDOM>();
				this.flattenedDependencies.add(xpd);
			}
		}
	}
	
	/************************************/
	private class QueryContext {
		private List<ResultsPerType> resultsPerType;
		private Map<DomainObjectMatch<?>, List<ResultsPerType>> resultsMap;
		private boolean execCount;

		private QueryContext(boolean execCount) {
			super();
			this.resultsPerType = new ArrayList<ResultsPerType>();
			this.resultsMap = new HashMap<DomainObjectMatch<?>, List<ResultsPerType>>();
			this.execCount = execCount;
		}
		
		private ResultsPerType addFor(QueryBuilder.ClausesPerType cpt, boolean isCountQuery) {
			DomainObjectMatch<?> dom = cpt.domainObjectMatch;
			Class<?> type = cpt.domainObjectType;
			List<ResultsPerType> resPerTypeList = this.resultsMap.get(dom);
			if (resPerTypeList == null) {
				resPerTypeList = new ArrayList<ResultsPerType>();
				this.resultsMap.put(dom, resPerTypeList);
			}
			ResultsPerType resPerType = null;
			for (ResultsPerType rpt : resPerTypeList) {
				if (rpt.type.equals(type)) {
					resPerType = rpt;
					break;
				}
			}
			if (resPerType == null) {
				String pref = isCountQuery ? countPrefix : idPrefix;
				resPerType = new ResultsPerType(type, ValueAccess.getName(cpt.node), cpt.getJcNumber(pref));
				resPerTypeList.add(resPerType);
			}
			return resPerType;
		}
		
		private void addEmptyFor(QueryBuilder.ClausesPerType cpt) {
			DomainObjectMatch<?> dom = cpt.domainObjectMatch;
			List<ResultsPerType> resPerTypeList = this.resultsMap.get(dom);
			if (resPerTypeList == null) {
				resPerTypeList = new ArrayList<ResultsPerType>();
				this.resultsMap.put(dom, resPerTypeList);
			}
		}
		
		private void queryExecuted() {
			Iterator<Entry<DomainObjectMatch<?>, List<ResultsPerType>>> it = this.resultsMap.entrySet().iterator();
			while(it.hasNext()) {
				Entry<DomainObjectMatch<?>, List<ResultsPerType>> entry = it.next();
				APIAccess.setPageChanged(entry.getKey(), false);
			}
		}
		
		/************************************/
		private class ResultsPerType {
			private Class<?> type;
			private JcPrimitive jcPrimitive;
			private JcNumber jcNumber;
			private List<Long> ids;
			private List<Object> simpleValues;
			private long count;
			private int queryIndex;
			
			private ResultsPerType(Class<?> type, String retName, JcNumber num) {
				super();
				this.type = type;
				this.jcNumber = num;
				this.count = 0;
				this.queryIndex = 0;
				this.jcPrimitive = MappingUtil.fromType(type, retName);
			}
		}
	}
	
	/*************************************/
	private enum State {
		INIT, HAS_XPRESSION
	}
	
	/************************************/
	public class MappingInfo {

		private MappingInfo() {
			super();
		}

		public ObjectMapping getObjectMappingFor(Class<?> domainObjectType) {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess()
					.getObjectMappingFor(domainObjectType);
		}
		
		/**
		 * may return null
		 * @param attribName
		 * @param domainObjectType
		 * @return
		 */
		public FieldMapping getFieldMapping(String attribName, Class<?> domainObjectType) {
			return getObjectMappingFor(domainObjectType).getFieldMappingForField(attribName);
		}
		
		public List<FieldMapping> getBackwardFieldMappings(String attribName, Class<?> domainObjectType) {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess()
					.getBackwardFieldMappings(attribName, domainObjectType);
		}
		
		public List<Class<?>> getCompoundTypesFor(Class<?> domainObjectType) {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess()
					.getCompoundTypesFor(domainObjectType);
		}
		
		public String getLabelForClass(Class<?> clazz) {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess()
					.getLabelForClass(clazz);
		}
		
		public Class<?> getClassForLabel(String label) {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess()
					.getClassForLabel(label);
		}
		
		public InternalDomainAccess getInternalDomainAccess() {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess();
		}
	}
}
