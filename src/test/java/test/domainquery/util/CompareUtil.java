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

package test.domainquery.util;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import test.domainmapping.util.AlreadyCompared;
import test.domainquery.model.Address;
import test.domainquery.model.Area;
import test.domainquery.model.Company;
import test.domainquery.model.Person;
import test.domainquery.model.Subject;

public class CompareUtil {

	public static boolean equalsObjects(Object o_1, Object o_2) {
		return equalsObjects(o_1, o_2, null);
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean equalsUnorderedList(List list_1, List list_2) {
		ArrayList<AlreadyCompared> acs = new ArrayList<AlreadyCompared>();
		if (list_1.size() != list_2.size())
        	return false;
		ListIterator<?> e1 = list_1.listIterator();
        while (e1.hasNext()) {
        	boolean found = false;
            Object o1 = e1.next();
            ListIterator<?> e2 = list_2.listIterator();
            while (e2.hasNext()) {
	            Object o2 = e2.next();
	            if (o1==null ? o2==null : equalsObjects(o1, o2, acs)) {
	            	found = true;
	            	break;
	            }
            }
            if (!found)
            	return false;
        }
        return true;
	}
	
	private static boolean equalsObjects(Object o_1, Object o_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		
		if (o_1 == o_2)
			return true;
		if (o_1 == null) {
			if (o_2 != null)
				return false;
		}
		if (!o_1.getClass().equals(o_2.getClass()))
			return false;
		if (o_1 instanceof Person)
			return CompareUtil.equalsPerson((Person)o_1, (Person)o_2, acs);
		else if (o_1 instanceof Company)
			return CompareUtil.equalsCompany((Company)o_1, (Company)o_2, acs);
		else if (o_1 instanceof Address)
			return CompareUtil.equalsAddress((Address)o_1, (Address)o_2, acs);
		else if (o_1 instanceof Area)
			return CompareUtil.equalsArea((Area)o_1, (Area)o_2, acs);
		else if (o_1 instanceof List<?>)
			return CompareUtil.equalsList((List<?>)o_1, (List<?>)o_2, acs);
		else
			return o_1.equals(o_2);
	}
	
	private static boolean equalsCompany(Company o_1, Company o_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(o_1, o_2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(o_1, o_2);
		acs.add(ac);
		
		ac.setResult(true);
		
		if (o_1 == o_2)
			return true;
		if (o_1 == null) {
			if (o_2 != null)
				return ac.setResult(false);
		}
		if (o_1.getClass() != o_2.getClass())
			return ac.setResult(false);
		if (!equalsSubject(o_1, o_2, acs))
			return ac.setResult(false);
		if (o_1.getName() == null) {
			if (o_2.getName() != null)
				return ac.setResult(false);
		} else if (!o_1.getName().equals(o_2.getName()))
			return ac.setResult(false);
		return true;
	}
	
	private static boolean equalsPerson(Person o_1, Person o_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(o_1, o_2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(o_1, o_2);
		acs.add(ac);
		
		ac.setResult(true);
		
		if (o_1 == o_2)
			return true;
		if (o_1 == null) {
			if (o_2 != null)
				return ac.setResult(false);
		}
		if (o_1.getClass() != o_2.getClass())
			return ac.setResult(false);
		if (!equalsSubject(o_1, o_2, acs))
			return ac.setResult(false);
		if (o_1.getFirstName() == null) {
			if (o_2.getFirstName() != null)
				return ac.setResult(false);
		} else if (!o_1.getFirstName().equals(o_2.getFirstName()))
			return ac.setResult(false);
		if (o_1.getGender() != o_2.getGender())
			return ac.setResult(false);
		if (o_1.getLastName() == null) {
			if (o_2.getLastName() != null)
				return ac.setResult(false);
		} else if (!o_1.getLastName().equals(o_2.getLastName()))
			return ac.setResult(false);
		if (o_1.getMother() == null) {
			if (o_2.getMother() != null)
				return ac.setResult(false);
		} else if (!equalsPerson(o_1.getMother(), o_2.getMother(), alreadyCompareds))
			return ac.setResult(false);
		if (o_1.getFather() == null) {
			if (o_2.getFather() != null)
				return ac.setResult(false);
		} else if (!equalsPerson(o_1.getFather(), o_2.getFather(), alreadyCompareds))
			return ac.setResult(false);
		return true;
	}
	
	private static boolean equalsSubject(Subject o_1, Subject o_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		if (o_1 == o_2)
			return true;
		if (o_1.getClass() != o_2.getClass())
			return false;
		if (o_1.getMatchString() == null) {
			if (o_2.getMatchString() != null)
				return false;
		} else if (!o_1.getMatchString().equals(o_2.getMatchString()))
			return false;
		if (o_1.getPointsOfContact() == null) {
			if (o_2.getPointsOfContact() != null)
				return false;
		} else if (!equalsList(o_1.getPointsOfContact(), o_2.getPointsOfContact(), acs))
			return false;
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	private static boolean equalsList(List list_1, List list_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(list_1, list_2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(list_1, list_2);
		acs.add(ac);
		
		ac.setResult(true);
		
		 if (list_1 == list_2)
            return true;
        if (list_1.size() != list_2.size())
        	return ac.setResult(false);

        ListIterator<?> e1 = list_1.listIterator();
        ListIterator<?> e2 = list_2.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : equalsObjects(o1, o2, acs)))
            	return ac.setResult(false);
        }
        return ac.setResult(!(e1.hasNext() || e2.hasNext()));
	}
	
	private static boolean equalsAddress(Address o_1, Address o_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(o_1, o_2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(o_1, o_2);
		acs.add(ac);
		
		ac.setResult(true);
		
		if (o_1 == o_2)
			return true;
		if (o_1 == null) {
			if (o_2 != null)
				return ac.setResult(false);
		}
		if (o_1.getClass() != o_2.getClass())
			return ac.setResult(false);
		if (o_1.getArea() == null) {
			if (o_2.getArea() != null)
				return ac.setResult(false);
		} else if (!equalsArea(o_1.getArea(), o_2.getArea(), acs))
			return ac.setResult(false);
		if (o_1.getNumber() != o_2.getNumber())
			return ac.setResult(false);
		if (o_1.getStreet() == null) {
			if (o_2.getStreet() != null)
				return ac.setResult(false);
		} else if (!o_1.getStreet().equals(o_2.getStreet()))
			return ac.setResult(false);
		return true;
	}
	
	private static boolean equalsArea(Area o_1, Area o_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(o_1, o_2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(o_1, o_2);
		acs.add(ac);
		
		ac.setResult(true);
		
		if (o_1 == o_2)
			return true;
		if (o_1 == null) {
			if (o_2 != null)
				return ac.setResult(false);
		}
		if (o_1.getClass() != o_2.getClass())
			return ac.setResult(false);
		if (o_1.getAreaCode() == null) {
			if (o_2.getAreaCode() != null)
				return ac.setResult(false);
		} else if (!o_1.getAreaCode().equals(o_2.getAreaCode()))
			return ac.setResult(false);
		if (o_1.getAreaType() != o_2.getAreaType())
			return ac.setResult(false);
		if (o_1.getName() == null) {
			if (o_2.getName() != null)
				return ac.setResult(false);
		} else if (!o_1.getName().equals(o_2.getName()))
			return ac.setResult(false);
		if (o_1.getPartOf() == null) {
			if (o_2.getPartOf() != null)
				return ac.setResult(false);
		} else if (!equalsArea(o_1.getPartOf(), o_2.getPartOf(), acs))
			return ac.setResult(false);
		return true;
	}
}
