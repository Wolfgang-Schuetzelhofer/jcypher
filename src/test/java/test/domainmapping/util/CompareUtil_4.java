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

import java.util.ArrayList;
import java.util.List;

import test.domainmapping.resolutiondepth.LinkedElement;

public class CompareUtil_4 {

	public static boolean equalsLinkedElements(LinkedElement elem_1, LinkedElement elem_2, int toDepth) {
		return equalsLinkedElements(elem_1, elem_2, toDepth, 0, new ArrayList<AlreadyCompared>());
	}
	
	private static boolean equalsLinkedElements(LinkedElement elem_1, LinkedElement elem_2,
			int toDepth, int actDepth, List<AlreadyCompared> alreadyCompareds) {
		
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(elem_1, elem_2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(elem_1, elem_2);
		acs.add(ac);
		ac.setResult(true);
		
		if (elem_1 == elem_2)
			return true;
		if (elem_1 != null && elem_2 == null)
			return ac.setResult(false);
		if (elem_2 != null && elem_1 == null)
			return ac.setResult(false);
		if (elem_1.getClass() != elem_2.getClass())
			return ac.setResult(false);
		if (elem_1.getDescription() == null) {
			if (elem_2.getDescription() != null)
				return ac.setResult(false);
		} else if (!elem_1.getDescription().equals(elem_2.getDescription()))
			return ac.setResult(false);
		
		if (toDepth >= 0 && toDepth == actDepth)
			return ac.getResult();
		
		if (elem_1.getNext() == null) {
			if (elem_2.getNext() != null)
				return ac.setResult(false);
		} else if (!equalsLinkedElements(elem_1.getNext(), elem_2.getNext(),
				toDepth, actDepth + 1, alreadyCompareds))
			return ac.setResult(false);
		return true;
	}
	
	public static int getDepth(LinkedElement elem) {
		return getDepth(elem, 0, new ArrayList<LinkedElement>());
	}
	
	private static int getDepth(LinkedElement elem, int actDepth,  List<LinkedElement> alreadyVisited) {
		if (alreadyVisited.contains(elem)) {
			// loop detected
			return -1;
		}
		alreadyVisited.add(elem);
		LinkedElement nextElem = elem.getNext();
		if (nextElem == null)
			return actDepth;
		return getDepth(nextElem, actDepth + 1, alreadyVisited);
	}
}
