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

package iot.jcypher.ast.returns;

import iot.jcypher.ast.ASTNode;
import iot.jcypher.values.JcValue;

import java.util.ArrayList;
import java.util.List;

public class ReturnExpression extends ASTNode {

	private ReturnValue returnValue;
	
	private JcValue alias;
	private boolean distinct = false;
	private boolean count = false;
	// filter expressions
	private List<Order> orders;
	private int limit = -1;
	private int skip = -1;

	public ReturnValue getReturnValue() {
		return returnValue;
	}

	public void setReturnValue(ReturnValue returnVal) {
		this.returnValue = returnVal;
	}

	public JcValue getAlias() {
		return alias;
	}

	public void setAlias(JcValue alias) {
		this.alias = alias;
	}

	public boolean isDistinct() {
		return distinct;
	}

	public void setDistinct() {
		this.distinct = true;
	}

	public boolean isCount() {
		return count;
	}

	public void setCount() {
		this.count = true;
	}

	public List<Order> getOrders() {
		return orders;
	}

	public void addOrder(Order order) {
		if (this.orders == null)
			this.orders = new ArrayList<Order>();
		this.orders.add(order);
	}

	public int getLimit() {
		return limit;
	}

	public void setLimit(int limit) {
		this.limit = limit;
	}

	public int getSkip() {
		return skip;
	}

	public void setSkip(int skip) {
		this.skip = skip;
	}
}
