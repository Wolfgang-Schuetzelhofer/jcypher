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

package iot.jcypher.domainquery.internal;

import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.internal.DomainAccess.InternalDomainAccess;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.domain.mapping.FieldMapping;
import iot.jcypher.domain.mapping.ObjectMapping;
import iot.jcypher.domainquery.api.APIAccess;
import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.ast.ConcatenateExpression;
import iot.jcypher.domainquery.ast.ConcatenateExpression.Concatenator;
import iot.jcypher.domainquery.ast.IASTObject;
import iot.jcypher.domainquery.ast.Parameter;
import iot.jcypher.domainquery.ast.PredicateExpression;
import iot.jcypher.domainquery.ast.PredicateExpression.Operator;
import iot.jcypher.query.JcQuery;
import iot.jcypher.query.JcQueryResult;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.api.predicate.BooleanOperation;
import iot.jcypher.query.api.predicate.Concat;
import iot.jcypher.query.api.predicate.IBeforePredicate;
import iot.jcypher.query.factories.clause.OPTIONAL_MATCH;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.WHERE;
import iot.jcypher.query.result.JcError;
import iot.jcypher.query.result.JcResultException;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;
import iot.jcypher.query.values.ValueAccess;
import iot.jcypher.query.values.ValueElement;
import iot.jcypher.query.writer.Format;
import iot.jcypher.util.Util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class QueryExecutor {

	private static final String idPrefix = "id_";
	private static final char separator = '_';
	
	private IDomainAccess domainAccess;
	private List<IASTObject> astObjects;
	private List<DomainObjectMatch<?>> domainObjectMatches;
	private Map<String, Parameter> parameters;
	private MappingInfo mappingInfo;
	private QueryContext result;
	
	public QueryExecutor(IDomainAccess domainAccess) {
		super();
		this.domainAccess = domainAccess;
		this.astObjects = new ArrayList<IASTObject>();
		this.domainObjectMatches = new ArrayList<DomainObjectMatch<?>>();
		this.parameters = new HashMap<String, Parameter>();
	}

	public List<IASTObject> getAstObjects() {
		return astObjects;
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
	 * @return a DomainQueryResult
	 */
	public void execute() {
		QueryContext context = new QueryContext();
		QueryBuilder qb = new QueryBuilder();
		JcQuery query = qb.buildQuery(context);
		Util.printQuery(query, "DOM QUERY", Format.PRETTY_1);
		JcQueryResult result = ((IIntDomainAccess)domainAccess).getInternalDomainAccess().
														execute(query);
		List<JcError> errors = Util.collectErrors(result);
		if (errors.size() > 0) {
			throw new JcResultException(errors);
		}
		Util.printResult(result, "DOM QUERY", Format.PRETTY_1);
		qb.extractUniqueIds(result, context.idNumbers);
		this.result = context;
		return;
	}
	
	public MappingInfo getMappingInfo() {
		if (this.mappingInfo == null)
			this.mappingInfo = new MappingInfo();
		return this.mappingInfo;
	}
	
	public <T> List<T> loadResult(DomainObjectMatch<T> match) {
		if (this.result == null)
			throw new RuntimeException("query was not executed");
		List<QueryContext.IdsPerType> idsPerTypeList = this.result.idsMap.get(match);
		if (idsPerTypeList == null)
			throw new RuntimeException("DomainObjectMatch was not defined in this query");
		Map<Class<?>, List<Long>> type2IdsMap = new HashMap<Class<?>, List<Long>>();
		List<Long> allIds = new ArrayList<Long>();
		for (QueryContext.IdsPerType idsPerType : idsPerTypeList) {
			List<Long> ids = type2IdsMap.get(idsPerType.type);
			boolean addList = false;
			if (ids == null) {
				ids = new ArrayList<Long>();
				addList = true;
			}
			ids.addAll(idsPerType.ids);
			allIds.addAll(idsPerType.ids);
			if (ids.isEmpty())
				addList = false;
			if (addList)
				type2IdsMap.put(idsPerType.type, ids);
		}
		long[] idsArray = new long[allIds.size()];
		for (int i = 0; i < allIds.size(); i++) {
			idsArray[i] = allIds.get(i).longValue();
		}
		List<T> ret = ((IIntDomainAccess)domainAccess).getInternalDomainAccess().
			loadByIds(APIAccess.getDomainObjectType(match), type2IdsMap, -1, idsArray);
		return ret;
	}
	
	/************************************/
	private class QueryBuilder {
		
		JcQuery buildQuery(QueryContext context) {
			List<ClausesPerType> clausesPerType = new ArrayList<ClausesPerType>();
			List<ExpressionsPerDOM> xpressionsPerDom = new ArrayList<ExpressionsPerDOM>();
			for (DomainObjectMatch<?> dom : domainObjectMatches) {
				StateContext stateContext = findXpressionsFor(dom);
				if (stateContext.state == State.HAS_XPRESSION) {
					xpressionsPerDom.add(new ExpressionsPerDOM(dom, stateContext.candidates,
							stateContext.dependencies));
				}
			}
			
			xpressionsPerDom = orderByDependencies(xpressionsPerDom);
			
			for (ExpressionsPerDOM xpd : xpressionsPerDom) {
				clausesPerType.addAll(buildClausesFor(xpd.domainObjectMatch, xpd.xPressions));
			}
			
			List<IClause> clauses = new ArrayList<IClause>();
			for (ClausesPerType cpt : clausesPerType) {
				if (cpt.valid) {
					String nodeLabel = getMappingInfo().getLabelForClass(cpt.domainObjectType);
					clauses.add(
						OPTIONAL_MATCH.node(cpt.node).label(nodeLabel)
					);
					if (cpt.concatenator != null)
						clauses.add(cpt.concatenator);
				}
			}
			
			int idx = 0;
			for (ClausesPerType cpt : clausesPerType) {
				if (cpt.valid) {
					JcNumber num = new JcNumber(idPrefix.concat(String.valueOf(idx)));
					QueryContext.IdsPerType idsPerType = context.addFor(cpt.domainObjectMatch, cpt.domainObjectType, num);
					context.idNumbers.add(idsPerType);
					if (idx == 0)
						clauses.add(
								RETURN.DISTINCT().value(cpt.node.id()).AS(num)
						);
					else
						clauses.add(
								RETURN.value(cpt.node.id()).AS(num)
						);
					idx++;
				}
			}
			
			IClause[] clausesArray = clauses.toArray(new IClause[clauses.size()]);
			JcQuery query = new JcQuery();
			query.setClauses(clausesArray);
			
			return query;
		}
		
		void extractUniqueIds(JcQueryResult result, List<QueryContext.IdsPerType> idsPerTypeList) {
			Set<Long> uniqueIds = new LinkedHashSet<Long>();
			for (QueryContext.IdsPerType idsPerType : idsPerTypeList) {
				uniqueIds.clear();
				List<BigDecimal> idList = result.resultOf(idsPerType.jcNumber);
				for (BigDecimal bd : idList) {
					if (bd != null) {
						uniqueIds.add(bd.longValue());
					}
				}
				idsPerType.ids = new ArrayList<Long>();
				idsPerType.ids.addAll(uniqueIds);
			}
			return;
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
				throw new RuntimeException("cyclic dependencies in WHERE clauses");
			if (add.dependencies == null || add.dependencies.isEmpty()) {
				if (!ordered.contains(add))
					ordered.add(add);
			} else {
				tryingToAdd.add(add);
				for (DomainObjectMatch<?> dom : add.dependencies) {
					ExpressionsPerDOM xpd = getXprPerDom(dom, xpressionsPerDom);
					addRecursive(xpd, xpressionsPerDom, ordered, tryingToAdd);
				}
				tryingToAdd.remove(add);
				ordered.add(add);
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

		private List<ClausesPerType> buildClausesFor(DomainObjectMatch<?> dom,
				List<IASTObject> xpressionsForDom) {
			List<ClausesPerType> clausesPerType = new ArrayList<ClausesPerType>();
			List<JcNode> nodes = APIAccess.getNodes(dom);
			List<Class<?>> typeList = APIAccess.getTypeList(dom);
			int idx = 0;
			for (JcNode n : nodes) {
				clausesPerType.add(new ClausesPerType(n, dom, typeList.get(idx)));
				idx++;
			}
			
			for (IASTObject astObj : xpressionsForDom) {
				for (ClausesPerType clausePerType : clausesPerType) {
					if (clausePerType.valid) {
						if (astObj instanceof ConcatenateExpression) {
							ConcatenateExpression conc = (ConcatenateExpression)astObj;
							if (conc.getConcatenator() == Concatenator.BR_OPEN) {
								if (clausePerType.concatenator == null) {
									if (clausePerType.concat == null) {
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
								continue;
							}
							PredicateExpression pred = (PredicateExpression)astObj;
							Concat concat_1 = clausePerType.concat;
							if (clausePerType.concatenator != null) {
								concat_1 = clausePerType.concatenator.AND();
								clausePerType.previousOr = false;
							}
							clausePerType.concatenator = buildPredicateExpression(pred, concat_1, clausePerType);
						}
					}
				}
			}
			return clausesPerType;
		}

		private iot.jcypher.query.api.predicate.Concatenator buildPredicateExpression(PredicateExpression pred,
				Concat concat, ClausesPerType clausesPerType) {
			iot.jcypher.query.api.predicate.Concatenator ret = null;
			// handle negations
			int neg = pred.getNegationCount();
			IBeforePredicate beforePred = null;
			BooleanOperation booleanOp;
			for (int i = neg; neg > 0; neg--) {
				if (i == neg) {
					if (concat == null)
						beforePred = WHERE.NOT();
					else
						beforePred = concat.NOT();
				} else
					beforePred = beforePred.NOT();
			}
			
			ValueElement val_1 = testAndCloneIfNeeded(pred.getValue_1(), clausesPerType);
			if (val_1 != null) { // if either really null or invalid
				if (beforePred != null)
					booleanOp = beforePred.valueOf(val_1);
				else { // no negation(s)
					if (concat == null)
						booleanOp = WHERE.valueOf(val_1);
					else
						booleanOp = concat.valueOf(val_1);
				}
				
				Object val_2 = pred.getValue_2();
				if (val_2 instanceof Parameter)
					val_2 = ((Parameter)val_2).getValue();
				Operator op = pred.getOperator();
				if (op == Operator.EQUALS)
					ret = booleanOp.EQUALS(val_2);
				else if (op == Operator.GT)
					ret = booleanOp.GT(val_2);
				else if (op == Operator.GTE)
					ret = booleanOp.GTE(val_2);
				else if (op == Operator.IN) {
					if (val_2 instanceof DomainObjectMatch<?>) {
						//TODO
					} else if (val_2.getClass().isArray())
						ret = booleanOp.IN_list(val_2);
				} else if (op == Operator.IS_NULL)
					ret = booleanOp.IS_NULL();
				else if (op == Operator.LIKE)
					ret = booleanOp.REGEX(val_2.toString());
				else if (op == Operator.LT)
					ret = booleanOp.LT(val_2);
				else if (op == Operator.LTE)
					ret = booleanOp.LTE(val_2);
			} else
				clausesPerType.valid = false;
			
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
								ValueAccess.setPredecessor(nextCloned, clausesPerType.node);
							else // the value element must have been the node 
								ret = clausesPerType.node;
						}
					} else
						ret = null;
				}
			}
			return ret;
		}

		@SuppressWarnings("unchecked")
		private void testValidForType(JcNode first,
				ValueElement ve, ClausesPerType clausesPerType) {
			if (ve == first) {
				return;
			}
			if (clausesPerType.previousOr && !Settings.strict) { // build the clause even if the expression will return no result
				return;									     // because it is concatenated by OR
			}
			Object hint = ValueAccess.getAnyHint(ve);
			if (hint instanceof List<?>) {
				List<JcNode> validFor = (List<JcNode>) hint; 
				String nm = ValueAccess.getName(clausesPerType.node);
				for(JcNode n : validFor) {
					if (nm.equals(ValueAccess.getName(n))) {
						return;
					}
				}
			}
			if (Settings.strict)
				clausesPerType.valid = false;
			else
				clausesPerType.testForNextIsOr = true;
			return;
		}

		private StateContext findXpressionsFor(DomainObjectMatch<?> dom) {
			String baseNodeName = APIAccess.getBaseNodeName(dom);
			StateContext context = new StateContext();
			for (int i = 0; i < astObjects.size(); i++) {
				IASTObject astObj = astObjects.get(i);
				testXpressionFor(baseNodeName, astObj, context);
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
			List<Integer> candidatesIndices = new ArrayList<Integer>();
			for (int i = 0; i < candidates.size(); i++) {
				IASTObject astObj = candidates.get(i);
				if (astObj instanceof ConcatenateExpression) {
					Concatenator concat = ((ConcatenateExpression)astObj).getConcatenator();
					if (concat == Concatenator.BR_OPEN)
						candidatesIndices.add(i);
					else if (concat == Concatenator.BR_CLOSE) {
						// close follows immediately after open
						if (candidatesIndices.size() > 0) {
							toRemove.add(i);
							Integer idx = candidatesIndices.remove(candidatesIndices.size() - 1);
							toRemove.add(0, idx);
						}
					}
				} else { // not an empty block, contains at least one valid predicate expression
					candidatesIndices.clear();
				}
			}
			
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
				}
				// toggle between close and open index (they are always pairwise)
				prevCloseIndex = prevCloseIndex == -1 ? idx : -1;
			}
		}

		private void testXpressionFor(String baseNodeName,
				IASTObject astObj, StateContext context) {
			if (astObj instanceof PredicateExpression) {
				PredicateExpression pred = (PredicateExpression)astObj;
				boolean isXpr = false;
				String nodeName_1 = getNodeName(pred.getValue_1());
				if (nodeName_1 != null) {
					if (nodeName_1.indexOf(baseNodeName) == 0) {
						context.candidates.add(astObj);
						isXpr = true;
						context.state = State.HAS_XPRESSION;
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
			} else if (astObj instanceof ConcatenateExpression) {
				ConcatenateExpression conc = (ConcatenateExpression)astObj;
				if (conc.getConcatenator() == Concatenator.BR_OPEN) {
					context.blockCount++;
				} else if (conc.getConcatenator() == Concatenator.BR_CLOSE) {
					if (context.blockCount <= 0)
						throw new RuntimeException("bracket close mismatch");
					context.blockCount--;
				}
				context.candidates.add(astObj);
			}
		}

		private String getNodeName(Object value) {
			if (value instanceof ValueElement) {
				ValueElement ve = (ValueElement)value;
				ValueElement first = ValueAccess.findFirst(ve);
				if (first instanceof JcNode)
					return ValueAccess.getName((JcNode)first);
			}
			return null;
		}
		
		/*************************************/
		private class StateContext {
			private State state;
			private int blockCount;
			private List<IASTObject> candidates;
			private List<String> dependencies;
			
			private StateContext() {
				super();
				this.state = State.INIT;
				this.blockCount = 0;
				this.candidates = new ArrayList<IASTObject>();
			}
			
			private void addDependency(String nodeName) {
				String nn = nodeName;
				if (this.dependencies == null)
					this.dependencies = new ArrayList<String>();
				int idx = nodeName.indexOf(separator);
				idx = nodeName.indexOf(separator, idx + 1);
				if (idx >= 0)
					nn = nodeName.substring(0, idx);
				if (!this.dependencies.contains(nn))
					this.dependencies.add(nn);
			}
		}
		
		/*************************************/
		private class ClausesPerType {
			private DomainObjectMatch<?> domainObjectMatch;
			private List<IClause> clauses;
			private JcNode node;
			private Class<?> domainObjectType;
			private boolean valid;
			private boolean previousOr;
			private boolean testForNextIsOr;
			Concat concat = null;
			iot.jcypher.query.api.predicate.Concatenator concatenator = null;
			
			private ClausesPerType(JcNode node, DomainObjectMatch<?> dom, Class<?> type) {
				super();
				this.node = node;
				this.domainObjectMatch = dom;
				this.domainObjectType = type;
				this.valid = true;
				this.previousOr = false;
				this.testForNextIsOr = false;
			}
		}
		
		/*************************************/
		private class ExpressionsPerDOM {
			private DomainObjectMatch<?> domainObjectMatch;
			private List<IASTObject> xPressions;
			private List<DomainObjectMatch<?>> dependencies;
			
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
		}
	}
	
	/************************************/
	private class QueryContext {
		private List<IdsPerType> idNumbers;
		private List<Long> ids;
		private Map<DomainObjectMatch<?>, List<IdsPerType>> idsMap;

		private QueryContext() {
			super();
			this.idNumbers = new ArrayList<IdsPerType>();
			this.idsMap = new HashMap<DomainObjectMatch<?>, List<IdsPerType>>();
		}
		
		private IdsPerType addFor(DomainObjectMatch<?> dom, Class<?> type, JcNumber num) {
			List<IdsPerType> idsPerTypeList = this.idsMap.get(dom);
			if (idsPerTypeList == null) {
				idsPerTypeList = new ArrayList<IdsPerType>();
				this.idsMap.put(dom, idsPerTypeList);
			}
			IdsPerType idsPerType = null;
			for (IdsPerType idp : idsPerTypeList) {
				if (idp.type.equals(type)) {
					idsPerType = idp;
					break;
				}
			}
			if (idsPerType == null) {
				idsPerType = new IdsPerType(dom, type, num);
				idsPerTypeList.add(idsPerType);
			}
			return idsPerType;
		}
		
		/************************************/
		private class IdsPerType {
			private DomainObjectMatch<?> domainObjectMatch;
			private Class<?> type;
			private JcNumber jcNumber;
			private List<Long> ids;
			
			private IdsPerType(DomainObjectMatch<?> dom, Class<?> type, JcNumber num) {
				super();
				this.domainObjectMatch = dom;
				this.type = type;
				this.jcNumber = num;
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
		
		public List<Class<?>> getCompoundTypesFor(Class<?> domainObjectType) {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess()
					.getCompoundTypesFor(domainObjectType);
		}
		
		public String getLabelForClass(Class<?> clazz) {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess()
					.getLabelForClass(clazz);
		}
		
		public InternalDomainAccess getInternalDomainAccess() {
			return ((IIntDomainAccess)domainAccess).getInternalDomainAccess();
		}
	}
}
