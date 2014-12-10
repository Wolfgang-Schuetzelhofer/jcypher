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

package iot.jcypher.domain.internal;

import java.util.ArrayList;
import java.util.List;

public class SkipLimitCalc {

	public static SkipsLimits calcSkipsLimits(List<Integer> counts, int offset, int count) {
		SkipsLimits ret = new SkipsLimits(counts.size());
		int nextOffset = offset;
		int remainLen = count;
		for (int nums : counts) {
			int reduceLen;
			// calc offset
			if (nextOffset > 0) {
					nextOffset = nextOffset - nums;
				if (nextOffset <= 0) {
					ret.offsets.add(nums + nextOffset);
					reduceLen = -nextOffset;
				} else {
					ret.offsets.add(nums); // skip elements of this type
					reduceLen = 0;
				}
			} else {
				ret.offsets.add(0);
				reduceLen = nums;
			}
			
			// calc count
			if (count >= 0) { // number of objects to read is limited 
				if (remainLen > 0) {
					remainLen = remainLen - reduceLen;
					ret.lengths.add(reduceLen + (remainLen < 0 ? remainLen : 0));
				} else
					ret.lengths.add(0); // we are past the maximum number to read
			} else
				ret.lengths.add(reduceLen);
		}
		return ret;
	}
	
	/****************************/
	public static class SkipsLimits {
		private List<Integer> offsets;
		private List<Integer> lengths;
		
		private SkipsLimits(int size) {
			super();
			this.offsets = new ArrayList<Integer>(size);
			this.lengths = new ArrayList<Integer>(size);
		}

		public List<Integer> getOffsets() {
			return offsets;
		}

		public List<Integer> getLengths() {
			return lengths;
		}
		
	}
}
