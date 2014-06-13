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

package iot.jcypher.query.factories.xpression;

import iot.jcypher.query.api.collection.CFactory;
import iot.jcypher.query.api.collection.CWhere;
import iot.jcypher.query.api.collection.InCollection;
import iot.jcypher.query.values.JcValue;

/**
 * <div color='red' style="font-size:24px;color:red"><b><i>JCYPHER FACTORY</i></b></div>
 */
public class I {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>all elements of the collection must hold true for a predicate expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...holdsTrue(I.<b>forAll(n)</b>.IN_nodes(p).WHERE()...)</i></div>
	 * <br/>
	 */
	public static InCollection<CWhere> forAll(JcValue jcValue) {
		return CFactory.forAll(jcValue);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>at least one element of the collection must hold true for a predicate expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...holdsTrue(I.<b>forAny(n)</b>.IN_nodes(p).WHERE()...)</i></div>
	 * <br/>
	 */
	public static InCollection<CWhere> forAny(JcValue jcValue) {
		return CFactory.forAny(jcValue);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>exactly one element of the collection must hold true for a predicate expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...holdsTrue(I.<b>forSingle(n)</b>.IN_nodes(p).WHERE()...)</i></div>
	 * <br/>
	 */
	public static InCollection<CWhere> forSingle(JcValue jcValue) {
		return CFactory.forSingle(jcValue);
	}
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>no element of the collection may hold true for a predicate expression</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...holdsTrue(I.<b>forNone(n)</b>.IN_nodes(p).WHERE()...)</i></div>
	 * <br/>
	 */
	public static InCollection<CWhere> forNone(JcValue jcValue) {
		return CFactory.forNone(jcValue);
	}
}
