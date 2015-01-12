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

package iot.jcypher.domainquery.ast;

import iot.jcypher.domainquery.api.DomainObjectMatch;
import iot.jcypher.domainquery.internal.QueryExecutor;

import java.util.ArrayList;
import java.util.List;

public class TraversalExpression implements IASTObject {

	private QueryExecutor queryExecutor;
	private DomainObjectMatch<?> start;
	private DomainObjectMatch<?> end;
	private List<Step> steps;

	public TraversalExpression(DomainObjectMatch<?> start, QueryExecutor queryExecutor) {
		super();
		this.start = start;
		this.steps = new ArrayList<Step>();
		this.queryExecutor = queryExecutor;
	}
	
	/**
	 * @param attributeName
	 * @param direction 0 .. forward, 1 .. backward
	 */
	public void step(String attributeName, int direction) {
		this.steps.add(new Step(direction, attributeName));
	}
	
	public List<Step> getSteps() {
		return steps;
	}

	public void setEnd(DomainObjectMatch<?> end) {
		this.end = end;
	}

	public DomainObjectMatch<?> getEnd() {
		return end;
	}

	public DomainObjectMatch<?> getStart() {
		return start;
	}

	public QueryExecutor getQueryExecutor() {
		return queryExecutor;
	}
	
	/***************************/
	public class Step {
		// 0 .. forward, 1 .. backward
		private int direction;
		private String attributeName;
		private boolean isCollection = false;
		private int minDistance;
		private int maxDistance;
		
		private Step(int direction, String attributeName) {
			super();
			this.direction = direction;
			this.attributeName = attributeName;
			this.minDistance = 1;
			this.maxDistance = 1;
		}
		
		public Step createStep(int direction, String attributeName) {
			return new Step(direction, attributeName);
		}

		public boolean isCollection() {
			return isCollection;
		}

		public void setCollection(boolean isCollection) {
			this.isCollection = isCollection;
		}

		public int getDirection() {
			return direction;
		}

		public String getAttributeName() {
			return attributeName;
		}

		public int getMinDistance() {
			return minDistance;
		}

		public void setMinDistance(int minDistance) {
			this.minDistance = minDistance;
		}

		public int getMaxDistance() {
			return maxDistance;
		}

		public void setMaxDistance(int maxDistance) {
			this.maxDistance = maxDistance;
		}
		
	}
}
