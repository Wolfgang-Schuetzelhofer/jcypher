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

package iot.jcypher.domainquery.api;

import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.internal.DomainAccess.InternalDomainAccess;
import iot.jcypher.domainquery.ast.TraversalExpression;
import iot.jcypher.domainquery.ast.TraversalExpression.Step;
import iot.jcypher.domainquery.internal.QueryRecorder;

public class TraversalStep extends APIObject {

	TraversalStep(TraversalExpression te) {
		this.astObject = te;
	}

	/**
	 * Traverse forward via an attribute
	 * @param attributeName
	 * @return
	 */
	public TraversalStep FORTH(String attributeName) {
		TraversalExpression te = (TraversalExpression)this.astObject;
		te.step(attributeName, 0);
		TraversalStep ret = new TraversalStep(te);
		QueryRecorder.recordInvocation(this, "FORTH", ret, QueryRecorder.literal(attributeName));
		return ret;
	}
	
	/**
	 * Traverse backward via an attribute
	 * @param attributeName
	 * @return
	 */
	public TraversalStep BACK(String attributeName) {
		TraversalExpression te = (TraversalExpression)this.astObject;
		te.step(attributeName, 1);
		TraversalStep ret = new TraversalStep(te);
		QueryRecorder.recordInvocation(this, "BACK", ret, QueryRecorder.literal(attributeName));
		return ret;
	}
	
	/**
	 * Define the distance in terms of how many hops to take when navigating the domain graph along a given attribute.
	 * The default is one hop (minDistance = maxDistance = 1),
	 * maxDistance -1 means hop as far as you will get (either to a leaf in the graph or to a detected loop).
	 * @param minDistance the minimum number of hops to navigate
	 * @param maxDistance the maximum number of hops to navigate
	 * @return
	 */
	public TraversalStep DISTANCE(int minDistance, int maxDistance) {
		TraversalExpression te = (TraversalExpression)this.astObject;
		Step step = te.getSteps().get(te.getSteps().size() - 1);
		step.setMinDistance(minDistance);
		step.setMaxDistance(maxDistance);
		QueryRecorder.recordInvocation(this, "DISTANCE", this, QueryRecorder.literal(minDistance),
				QueryRecorder.literal(maxDistance));
		return this;
	}
	
	/**
	 * End the traversal of the domain object graph matching a specific type of domain objects
	 * @param domainObjectType
	 * @return a DomainObjectMatch
	 */
	public <T> DomainObjectMatch<T> TO(Class<T> domainObjectType) {
		TraversalExpression te = (TraversalExpression)this.astObject;
		DomainObjectMatch<T> ret =APIAccess.createDomainObjectMatch(domainObjectType,
				te.getQueryExecutor().getDomainObjectMatches().size(),
				te.getQueryExecutor().getMappingInfo());
		te.getQueryExecutor().getDomainObjectMatches().add(ret);
		te.setEnd(ret);
		QueryRecorder.recordAssignment(this, "TO", ret, QueryRecorder.literal(domainObjectType.getName()));
		return ret;
	}
	
	/**
	 * End the traversal of the domain object graph matching a specific type of domain objects.
	 * <b>TO_GENERIC</b> is used when navigating a generic domain model.
	 * @param domainObjectTypeName
	 * @return a DomainObjectMatch
	 */
	public DomainObjectMatch<DomainObject> TO_GENERIC(String domainObjectTypeName) {
		Boolean br_old = null;
		try {
			TraversalExpression te = (TraversalExpression)this.astObject;
			InternalDomainAccess iAccess = te.getQueryExecutor().getMappingInfo().getInternalDomainAccess();
			iAccess.loadDomainInfoIfNeeded();
			Class<?> clazz = iAccess.getClassForName(domainObjectTypeName);
			br_old = QueryRecorder.blockRecording.get();
			QueryRecorder.blockRecording.set(Boolean.TRUE);
			DomainObjectMatch<?> delegate = TO(clazz);
			QueryRecorder.blockRecording.set(br_old);
			DomainObjectMatch<DomainObject> ret = APIAccess.createDomainObjectMatch(DomainObject.class, delegate);
			QueryRecorder.recordAssignment(this, "TO_GENERIC", delegate, QueryRecorder.literal(domainObjectTypeName));
			return ret;
		} catch (Throwable e) {
			if (e instanceof RuntimeException)
				throw (RuntimeException)e;
			else
				throw new RuntimeException(e);
		} finally {
			if (br_old != null)
				QueryRecorder.blockRecording.set(br_old);
		}
	}
}
