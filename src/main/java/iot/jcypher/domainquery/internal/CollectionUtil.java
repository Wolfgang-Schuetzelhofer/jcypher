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

package iot.jcypher.domainquery.internal;

import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.ValueAccess;

import java.util.List;

public class CollectionUtil {

	public static void addUnique(List<JcNode> nodes, JcNode node) {
		if (!containsNode(nodes, node))
			nodes.add(node);
	}
	
	public static void addAllUnique(List<JcNode> nodes, List<JcNode> toAdd) {
		for (JcNode n : toAdd) {
			addUnique(nodes, n);
		}
	}
	
	private static boolean containsNode(List<JcNode> nodes, JcNode node) {
		String nm = ValueAccess.getName(node);
		for (JcNode n : nodes) {
			if (ValueAccess.getName(n).equals(nm))
				return true;
		}
		return false;
	}
}
