/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
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

package iot.jcypher.graph.internal;

import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.WITH;
import iot.jcypher.query.result.util.ResultHandler;
import iot.jcypher.query.values.JcElement;
import iot.jcypher.query.values.JcNumber;

import java.util.ArrayList;
import java.util.List;

public class LockUtil {

	public static void calcRemoves(Removes removes, JcElement element, int version) {
		if (removes.withClauses == null)
			removes.withClauses = new ArrayList<IClause>();
		removes.withClauses.add(WITH.value(element));
		if (removes.sum == null)
			removes.sum = element.numberProperty(ResultHandler.lockVersionProperty);
		else
			removes.sum = removes.sum.plus(element.numberProperty(ResultHandler.lockVersionProperty));
		if (version >= 0) {
			if (removes.versionSum < 0)
				removes.versionSum = 0;
			removes.versionSum = removes.versionSum + version;
		}
	}
	
	/***************************/
	public static class Removes {

		private List<IClause> withClauses;
		private int versionSum = -1;
		private JcNumber sum;
		
		public List<IClause> getWithClauses() {
			return withClauses;
		}

		public JcNumber getSum() {
			return sum;
		}

		public int getVersionSum() {
			return versionSum;
		}
	}
}
