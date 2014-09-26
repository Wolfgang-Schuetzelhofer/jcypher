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

package test.domainmapping.ambiguous;

import java.util.List;
import java.util.ListIterator;

import test.domainmapping.Address;
import test.domainmapping.CompareUtil;


public class CompareUtil_2 {
	
	public static boolean equalsMultiBroker(MultiBroker broker_1, MultiBroker broker_2) {
		if (broker_1 == broker_2)
			return true;
		if (broker_1 != null && broker_2 == null)
			return false;
		if (broker_2 != null && broker_1 == null)
			return false;
		if (broker_1.getClass() != broker_2.getClass())
			return false;
		
		if (broker_1.getName() == null) {
			if (broker_2.getName() != null)
				return false;
		} else if (!broker_1.getName().equals(broker_2.getName()))
			return false;
		if (broker_1.getAddress() == null) {
			if (broker_2.getAddress() != null)
				return false;
		} else if (!CompareUtil_2.equalsAddress(broker_1.getAddress(), broker_2.getAddress()))
			return false;
		
		if (broker_1.getCanBroker() == null) {
			if (broker_2.getCanBroker() != null)
				return false;
		} else if (!equalsIPersons(broker_1.getCanBroker(), broker_2.getCanBroker()))
			return false;
		return true;
	}

	public static boolean equalsBroker(Broker broker_1, Broker broker_2) {
		if (broker_1 == broker_2)
			return true;
		if (broker_1 != null && broker_2 == null)
			return false;
		if (broker_2 != null && broker_1 == null)
			return false;
		if (broker_1.getClass() != broker_2.getClass())
			return false;
		if (broker_1.getWorksWith() == null) {
			if (broker_2.getWorksWith() != null)
				return false;
		} else if (!equalsIPerson(broker_1.getWorksWith(), broker_2.getWorksWith()))
			return false;
		if (broker_1.getAddress() == null) {
			if (broker_2.getAddress() != null)
				return false;
		} else if (!CompareUtil_2.equalsAddress(broker_1.getAddress(), broker_2.getAddress()))
			return false;
		return true;
	}
	
	public static boolean equalsIPersons(List<IPerson> iPersons_1, List<IPerson> iPersons_2) {
		if (iPersons_1 == iPersons_2)
            return true;

        ListIterator<IPerson> e1 = iPersons_1.listIterator();
        ListIterator<IPerson> e2 = iPersons_2.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
        	IPerson o1 = e1.next();
        	IPerson o2 = e2.next();
            if (!(o1==null ? o2==null : equalsIPerson(o1, o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
	}
	
	public static boolean equalsIPerson(IPerson iPerson_1, IPerson iPerson_2) {
		if (iPerson_1 instanceof NPerson && iPerson_2 instanceof NPerson)
			return equalsNPerson((NPerson)iPerson_1, (NPerson)iPerson_2);
		else if (iPerson_1 instanceof JPerson && iPerson_2 instanceof JPerson)
			return equalsJPerson((JPerson)iPerson_1, (JPerson)iPerson_2);
		else if (iPerson_1 == null && iPerson_2 == null)
			return true;
		return false;
	}
	
	public static boolean equalsNPerson(NPerson nPerson_1, NPerson nPerson_2) {
		if (nPerson_1 == nPerson_2)
			return true;
		if (!equalsAbstractPerson(nPerson_1, nPerson_2))
			return false;
		if (nPerson_1.getHomeAddress() == null) {
			if (nPerson_2.getHomeAddress() != null)
				return false;
		} else if (!CompareUtil_2.equalsAddress(nPerson_1.getHomeAddress(), nPerson_2.getHomeAddress()))
			return false;
		if (nPerson_1.getWorkAddress() == null) {
			if (nPerson_2.getWorkAddress() != null)
				return false;
		} else if (!CompareUtil_2.equalsAddress(nPerson_1.getWorkAddress(), nPerson_2.getWorkAddress()))
			return false;
		if (nPerson_1.getSocialSecurityNumber() == null) {
			if (nPerson_2.getSocialSecurityNumber() != null)
				return false;
		} else if (!nPerson_1.getSocialSecurityNumber().equals(nPerson_2.getSocialSecurityNumber()))
			return false;
		return true;
	}
	
	public static boolean equalsJPerson(JPerson jPerson_1, JPerson jPerson_2) {
		if (jPerson_1 == jPerson_2)
			return true;
		if (!equalsAbstractPerson(jPerson_1, jPerson_2))
			return false;
		if (jPerson_1.getCompanyAddress() == null) {
			if (jPerson_2.getCompanyAddress() != null)
				return false;
		} else if (!CompareUtil_2.equalsAddress(jPerson_1.getCompanyAddress(), jPerson_2.getCompanyAddress()))
			return false;
		if (jPerson_1.getContactAddress() == null) {
			if (jPerson_2.getContactAddress() != null)
				return false;
		} else if (!CompareUtil_2.equalsAddress(jPerson_1.getContactAddress(), jPerson_2.getContactAddress()))
			return false;
		if (jPerson_1.getPostalAddress() == null) {
			if (jPerson_2.getPostalAddress() != null)
				return false;
		} else if (!CompareUtil_2.equalsAddress(jPerson_1.getPostalAddress(), jPerson_2.getPostalAddress()))
			return false;
		if (jPerson_1.getCompanyNumber() != jPerson_2.getCompanyNumber())
			return false;
		return true;
	}
	
	public static boolean equalsAbstractPerson(AbstractPerson aPerson_1, AbstractPerson aPerson_2) {
		if (aPerson_1 == aPerson_2)
			return true;
		if (aPerson_1 != null && aPerson_2 == null)
			return false;
		if (aPerson_2 != null && aPerson_1 == null)
			return false;
		if (aPerson_1.getClass() != aPerson_2.getClass())
			return false;
		if (aPerson_1.getNamePart1() == null) {
			if (aPerson_2.getNamePart1() != null)
				return false;
		} else if (!aPerson_1.getNamePart1().equals(aPerson_2.getNamePart1()))
			return false;
		if (aPerson_1.getNamePart2() == null) {
			if (aPerson_2.getNamePart2() != null)
				return false;
		} else if (!aPerson_1.getNamePart2().equals(aPerson_2.getNamePart2()))
			return false;
		return true;
	}
	
	public static boolean equalsAddress(Address address1, Address address2) {
		if (address1 instanceof DistrictAddress && address2 instanceof DistrictAddress)
			return equalsDistrictAddress((DistrictAddress)address1, (DistrictAddress)address2);
		if (address1 instanceof Address && address2 instanceof Address)
			return CompareUtil.equalsAddress(address1, address2);
		if (address1 == null && address2 == null)
			return true;
		return false;
	}
	
	public static boolean equalsDistrictAddress(DistrictAddress address1, DistrictAddress address2) {
		if (address1 == address2)
			return true;
		if (!CompareUtil.equalsAddress(address1, address2))
			return false;
		if (address1.getDistrict() == null) {
			if (address2.getDistrict() != null)
				return false;
		} else if (!equalsDistrict(address1.getDistrict(), address2.getDistrict()))
			return false;
		if (address1.getSubDistrict() == null) {
			if (address2.getSubDistrict() != null)
				return false;
		} else if (!equalsDistrict(address1.getSubDistrict(), address2.getSubDistrict()))
			return false;
		return true;
	}

	public static boolean equalsDistrict(District district1, District district2) {
		if (district1 == district2)
			return true;
		if (district1.getClass() != district2.getClass())
			return false;
		if (district1.getName() == null) {
			if (district2.getName() != null)
				return false;
		} else if (!district1.getName().equals(district2.getName()))
			return false;
		return true;
	}
}
