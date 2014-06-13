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
import iot.jcypher.query.ast.returns.ReturnAggregate.AggregateFunctionType;
import iot.jcypher.query.values.JcProperty;

public class Aggregate extends APIObject {

	Aggregate(ReturnExpression rx) {
		super();
		this.astNode = rx;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>sum all the numeric values which are encountered. NULLs are silently dropped</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().<b>sum(n.property("amount"))</b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> sum(JcProperty property) {
		ReturnExpression rx = getReturnExpression();
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setType(AggregateFunctionType.SUM);
		ra.setArgument(property);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i> calculate the average of numeric values</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().<b>avg(n.property("amount"))</b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> avg(JcProperty property) {
		ReturnExpression rx = getReturnExpression();
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setType(AggregateFunctionType.AVG);
		ra.setArgument(property);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i> calculate the standard deviation for a given value over a group;
	 * <br/>uses a standard two-pass method, with N - 1 as the denominator, 
	 * and should be used when taking a sample of the population for an unbiased estimate.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().<b>stdev(n.property("age"))</b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> stdev(JcProperty property) {
		ReturnExpression rx = getReturnExpression();
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setType(AggregateFunctionType.STDEV);
		ra.setArgument(property);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i> calculate the standard deviation for a given value over a group;
	 * <br/>uses a standard two-pass method, with N - 1 as the denominator, 
	 * and should be used when taking a sample of an entire population.</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().<b>stdevp(n.property("age"))</b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> stdevp(JcProperty property) {
		ReturnExpression rx = getReturnExpression();
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setType(AggregateFunctionType.STDEVP);
		ra.setArgument(property);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>find the largest value in a collection of numeric values</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().<b>max(n.property("amount"))</b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> max(JcProperty property) {
		ReturnExpression rx = getReturnExpression();
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setType(AggregateFunctionType.MAX);
		ra.setArgument(property);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>find the smallest value in a collection of numeric values</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().<b>min(n.property("amount"))</b></i></div>
	 * <br/>
	 */
	public RElement<RElement<?>> min(JcProperty property) {
		ReturnExpression rx = getReturnExpression();
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setType(AggregateFunctionType.MIN);
		ra.setArgument(property);
		RElement<RElement<?>> ret = new RElement<RElement<?>>(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>calculate: Given a series of values, which value is located at (or nearest to) a certain percentile (percent rank);</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>takes the percentile (between 0.0 and 1.0)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().<b>percentileDisc(0.5)</b>...</i></div>
	 * <br/>
	 */
	public Percentile percentileDisc(Number percentile) {
		ReturnExpression rx = getReturnExpression();
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setType(AggregateFunctionType.PERCENTILE_DISC);
		ra.setPercentile(percentile);
		Percentile ret = new Percentile(rx);
		return ret;
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>calculate: Given a series of values, which value is located at a certain percentile (percent rank);</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>uses a linear interpolation method, calculating a weighted average between two values,<br/>if the desired percentile lies between them.;</b></i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>takes the percentile (between 0.0 and 1.0)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...aggregate().<b>percentileCont(0.5)</b>...</i></div>
	 * <br/>
	 */
	public Percentile percentileCont(Number percentile) {
		ReturnExpression rx = getReturnExpression();
		ReturnAggregate ra = (ReturnAggregate) rx.getReturnValue();
		ra.setType(AggregateFunctionType.PERCENTILE_CONT);
		ra.setPercentile(percentile);
		Percentile ret = new Percentile(rx);
		return ret;
	}
	
	private ReturnExpression getReturnExpression() {
		return (ReturnExpression)this.astNode;
	}
}
