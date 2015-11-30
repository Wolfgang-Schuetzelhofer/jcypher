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

import iot.jcypher.domainquery.ast.TraversalExpression;
import iot.jcypher.domainquery.internal.QueryRecorder;

public class Traverse extends APIObject {

	Traverse(TraversalExpression te) {
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

}
