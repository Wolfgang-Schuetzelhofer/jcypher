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

package iot.jcypher.api.collection;

import iot.jcypher.values.JcCollection;
import iot.jcypher.values.JcNode;
import iot.jcypher.values.JcPath;

public interface ICollection<T> {

	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify a collection in order to iterate over its elements or to test for containment of elements</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>Use Factory Class <b>C</b> to create expressions which construct a collection</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>IN(C</b>.FILTER()...)</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or ...<b>IN(C</b>.EXTRACT()...)</i></div>
	 * <br/>
	 */
	public T IN(ICollectExpression C);
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify a collection in order to iterate over its elements or to test for containment of elements</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>IN(p</b>.nodes())</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>or ...<b>IN(n</b>.labels())</i></div>
	 * <br/>
	 */
	public T IN(JcCollection collection);
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify a collection in order to iterate over its elements or to test for containment of elements</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i>e.g. ...<b>IN_list("a", "b", "c")</b></i></div>
	 * <br/>
	 */
	@SuppressWarnings("unchecked")
	public <E> T IN_list(E... value);
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify a collection in order to iterate over its elements or to test for containment of elements</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>IN_nodes(path)</b> is a convenient shortcut for</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>IN(path.nodes())</b></i></div>
	 * <br/>
	 */
	public T IN_nodes(JcPath path);
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify a collection in order to iterate over its elements or to test for containment of elements</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>IN_relations(path)</b> is a convenient shortcut for</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>IN(path.relations())</b></i></div>
	 * <br/>
	 */
	public T IN_relations(JcPath path);
	
	/**
	 * <div color='red' style="font-size:24px;color:red"><b><i><u>JCYPHER</u></i></b></div>
	 * <div color='red' style="font-size:18px;color:red"><i>specify a collection in order to iterate over its elements or to test for containment of elements</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>IN_labels(node)</b> is a convenient shortcut for</i></div>
	 * <div color='red' style="font-size:18px;color:red"><i><b>IN(node.labels())</b></i></div>
	 * <br/>
	 */
	public T IN_labels(JcNode node);
}
