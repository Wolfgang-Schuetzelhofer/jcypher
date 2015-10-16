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

package test.genericmodel;

import iot.jcypher.domain.genericmodel.DomainObject;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Util {

	public static List<DomainObject> sortPersons(List<DomainObject> toSort) {
		Collections.sort(toSort, new Comparator<DomainObject>() {

			@Override
			public int compare(DomainObject o1, DomainObject o2) {
				String nm1 = o1.getFieldValue("lastName").toString();
				String nm2 = o2.getFieldValue("lastName").toString();
				int res = nm1.compareTo(nm2);
				if (res == 0) {
					nm1 = o1.getFieldValue("firstName").toString();
					nm2 = o2.getFieldValue("firstName").toString();
					res = nm1.compareTo(nm2);
				}
				return res;
			}
		});
		return toSort;
	}
	
	public static List<DomainObject> sortAddresses(List<DomainObject> toSort) {
		Collections.sort(toSort, new Comparator<DomainObject>() {

			@Override
			public int compare(DomainObject o1, DomainObject o2) {
				String nm1 = o1.getFieldValue("street").toString();
				String nm2 = o2.getFieldValue("street").toString();
				return nm1.compareTo(nm2);
			}
		});
		return toSort;
	}
}
