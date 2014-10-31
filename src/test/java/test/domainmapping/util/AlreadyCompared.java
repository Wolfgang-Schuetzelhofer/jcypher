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

package test.domainmapping.util;

import java.util.List;

public class AlreadyCompared {
	private Object object1;
	private Object object2;
	private boolean result;
	
	public AlreadyCompared(Object object1, Object object2) {
		super();
		this.object1 = object1;
		this.object2 = object2;
	}
	
	public boolean setResult(boolean b) {
		this.result = b;
		return b;
	}
	
	public boolean getResult() {
		return result;
	}

	public static AlreadyCompared alreadyCompared(Object obj1, Object obj2,
			List<AlreadyCompared> alreadyCompareds) {
		for (AlreadyCompared ac : alreadyCompareds) {
			if ((ac.object1 == obj1 && ac.object2 == obj2) ||
					(ac.object1 == obj2 && ac.object2 == obj2))
				return ac;
		}
		return null;
	}
}
