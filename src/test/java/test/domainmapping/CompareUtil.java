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

package test.domainmapping;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import test.domainmapping.util.AlreadyCompared;

public class CompareUtil {

	public static boolean equalsPerson(Person person1, Person person2) {
		return equalsPerson(person1, person2, new ArrayList<AlreadyCompared>());
	}
			
	private static boolean equalsPerson(Person person1, Person person2,
			List<AlreadyCompared> alreadyCompareds) {
		
		if (person1 == null || person2 == null)
			return false;
		
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(person1, person2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(person1, person2);
		acs.add(ac);

		if (person1.getMainAddress() == null) {
			if (person2.getMainAddress() != null)
				return ac.setResult(false);
		} else if (!equalsAddress(person1.getMainAddress(), person2.getMainAddress()))
			return ac.setResult(false);
		if (person1.getBirthDate() == null) {
			if (person2.getBirthDate() != null)
				return ac.setResult(false);
		} else if (!person1.getBirthDate().equals(person2.getBirthDate()))
			return ac.setResult(false);
		if (person1.getContact() == null) {
			if (person2.getContact() != null)
				return ac.setResult(false);
		} else if (!equalsContact(person1.getContact(), person2.getContact()))
			return ac.setResult(false);
		if (person1.getFirstName() == null) {
			if (person2.getFirstName() != null)
				return ac.setResult(false);
		} else if (!person1.getFirstName().equals(person2.getFirstName()))
			return ac.setResult(false);
		if (person1.getLastName() == null) {
			if (person2.getLastName() != null)
				return ac.setResult(false);
		} else if (!person1.getLastName().equals(person2.getLastName()))
			return ac.setResult(false);
		if (person1.getLuckyNumbers() == null) {
			if (person2.getLuckyNumbers() != null)
				return ac.setResult(false);
		} else if (!person1.getLuckyNumbers().equals(person2.getLuckyNumbers()))
			return ac.setResult(false);
		if (person1.getAddresses() == null) {
			if (person2.getAddresses() != null)
				return ac.setResult(false);
		} else if (!equalsAddresses(person1.getAddresses(), person2.getAddresses()))
			return false;
		
		ac.setResult(true); // equal so far
		if (person1.getBestFriend() == null) {
			if (person2.getBestFriend() != null)
				return ac.setResult(false);
		} else if (!equalsPerson(person1.getBestFriend(), person2.getBestFriend(), acs))
			return ac.setResult(false);
		return true;
	}
	
	public static boolean equalsAddress(Address address1, Address address2) {
		if (address1 == null || address2 == null)
			return false;
		
		if(!address1.getClass().equals(address2.getClass()))
			return false;
		
		if (address1.getCity() == null) {
			if (address2.getCity() != null)
				return false;
		} else if (!address1.getCity().equals(address2.getCity()))
			return false;
		if (address1.getNumber() != address2.getNumber())
			return false;
		if (address1.getStreet() == null) {
			if (address2.getStreet() != null)
				return false;
		} else if (!address1.getStreet().equals(address2.getStreet()))
			return false;
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean equalsAddresses(List addresses1, List addresses2) {
		 if (addresses1 == addresses2)
	            return true;

	        ListIterator e1 = addresses1.listIterator();
	        ListIterator e2 = addresses2.listIterator();
	        while (e1.hasNext() && e2.hasNext()) {
	        	Object o1 = e1.next();
	        	Object o2 = e2.next();
	            if (!(o1==null ? o2==null : equalsAddress((Address)o1, (Address)o2)))
	                return false;
	        }
	        return !(e1.hasNext() || e2.hasNext());
	}
	
	public static boolean equalsContact(Contact contact1, Contact contact2) {
		if (contact1 == null || contact2 == null)
			return false;
		
		if (contact1.getNummer() == null) {
			if (contact2.getNummer() != null)
				return false;
		} else if (!contact1.getNummer().equals(contact2.getNummer()))
			return false;
		if (contact1.getType() != contact2.getType())
			return false;
		return true;
	}
	
	public static boolean equalsCompany(Company company_1, Company company_2) {
		if (company_1 == null || company_2 == null)
			return false;
		
		if (company_1.getName() == null) {
			if (company_2.getName() != null)
				return false;
		} else if (!company_1.getName().equals(company_2.getName()))
			return false;
		if (company_1.getAddress() == null) {
			if (company_2.getAddress() != null)
				return false;
		} else if (!equalsAddress(company_1.getAddress(), company_2.getAddress()))
			return false;
		if (company_1.getAreaCodes() == null) {
			if (company_2.getAreaCodes() != null)
				return false;
		} else if (!company_1.getAreaCodes().equals(company_2.getAreaCodes()))
			return false;
		return true;
	}
}
