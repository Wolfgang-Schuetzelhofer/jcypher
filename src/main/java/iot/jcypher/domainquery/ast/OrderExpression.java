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

package iot.jcypher.domainquery.ast;

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.domainquery.api.DomainObjectMatch;

public class OrderExpression implements IASTObject {

	private DomainObjectMatch<?> objectMatch;
	private List<OrderBy> orderCriterias;

	public OrderExpression(DomainObjectMatch<?> objectMatch) {
		super();
		this.objectMatch = objectMatch;
	}

	public DomainObjectMatch<?> getObjectMatch() {
		return objectMatch;
	}
	
	public List<OrderBy> getOrderCriterias() {
		if (this.orderCriterias == null)
			this.orderCriterias = new ArrayList<OrderBy>();
		return this.orderCriterias;
	}
	
	public OrderBy getCreateOrderCriteriaFor(String attributeName) {
		List<OrderBy> ocs = getOrderCriterias();
		OrderBy ret = null;
		for (OrderBy ob : ocs) {
			if (ob.getAttributeName().equals(attributeName)) {
				ret = ob;
				break;
			}
		}
		if (ret == null) {
			ret = new OrderBy(attributeName);
			ocs.add(ret);
		}
		return ret;
	}

	/*********************************/
	public static class OrderBy {
		private String attributeName;
		
		// 0 .. ascending, 1 .. descending
		private int direction;

		public OrderBy(String attributeName) {
			super();
			this.attributeName = attributeName;
			this.direction = 0;
		}

		public int getDirection() {
			return direction;
		}

		public void setDirection(int direction) {
			this.direction = direction;
		}

		public String getAttributeName() {
			return attributeName;
		}
		
	}
}
