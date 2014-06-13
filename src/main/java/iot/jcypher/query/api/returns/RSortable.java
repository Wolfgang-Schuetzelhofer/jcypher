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

package iot.jcypher.query.api.returns;

import iot.jcypher.query.ast.returns.Order;
import iot.jcypher.query.ast.returns.ReturnExpression;
import iot.jcypher.query.factories.clause.RETURN;

public class RSortable extends RElement<RSortable> {

	RSortable(ReturnExpression rx) {
		super(rx);
		setConnector(this);
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>sort the output in ascending order</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. RETURN.element(n).<b>ORDER_BY("age")</b></i></div>
	 * <br/>
	 */
	public RSortable ORDER_BY(String propertyName) {
		Order o = new Order();
		o.setPropertyName(propertyName);
		getReturnExpression().addOrder(o);
		return this;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>sort the output in descending order</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. RETURN.element(n).<b>ORDER_BY_DESC("age")</b></i></div>
	 * <br/>
	 */
	public RSortable ORDER_BY_DESC(String propertyName) {
		Order o = new Order();
		o.setPropertyName(propertyName);
		o.setDescending(true);
		getReturnExpression().addOrder(o);
		return this;
	}
}
