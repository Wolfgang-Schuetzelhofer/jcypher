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

package iot.jcypher.api.returns;

import iot.jcypher.ast.returns.ReturnAggregate;
import iot.jcypher.ast.returns.ReturnExpression;

public class AggregateDistinct extends Aggregate {

	AggregateDistinct(ReturnExpression rx) {
		super(rx);
	}

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>return unique results</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().<b>DISTINCT().sum(n.property("eyeColor"))</b></i></div>
	 * <br/>
	 */
	public Aggregate DISTINCT() {
		ReturnExpression rx = (ReturnExpression)this.astNode;
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setDistinct();
		Aggregate ret = new Aggregate(rx);
		return ret;
	}
	
}
