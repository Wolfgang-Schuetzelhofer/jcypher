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

package iot.jcypher.query.result.util;

import java.util.ArrayList;
import java.util.List;

public class LocalIdBuilder {

	private long nextId = 0l;
	private List<Long> freeIds;
	
	public void releaseId(long id) {
		if (this.freeIds == null)
			this.freeIds = new ArrayList<Long>();
		Long lid = new Long(id);
		if (!this.freeIds.contains(lid))
			this.freeIds.add(lid);
	}
	
	public long getId() {
		if (this.freeIds != null && this.freeIds.size() > 0) {
			return this.freeIds.remove(this.freeIds.size() - 1).longValue();
		} else {
			long ret = this.nextId;
			this.nextId++;
			return ret;
		}
	}
}
