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

import iot.jcypher.query.api.APIObject;
import iot.jcypher.query.ast.returns.ReturnAggregate;
import iot.jcypher.query.ast.returns.ReturnExpression;
import iot.jcypher.query.values.JcProperty;

public class Percentile extends APIObject {

	Percentile(ReturnExpression rx) {
		super();
		this.astNode = rx;
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify the properties over which to perform the percentileDisc or PercentileCont calculation</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().percentileDisc(0.5).<b>over(n.property("age"))</b>...</i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> over(JcProperty property) {
		ReturnExpression rx = (ReturnExpression)this.astNode;
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setArgument(property);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
}
