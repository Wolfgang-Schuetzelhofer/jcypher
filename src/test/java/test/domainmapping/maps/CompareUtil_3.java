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

package test.domainmapping.maps;

import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import test.domainmapping.Address;
import test.domainmapping.ambiguous.CompareUtil_2;

public class CompareUtil_3 {

	public static boolean equalsMultiDimMapsLists(MultiDimMapsLists m_1,
			MultiDimMapsLists m_2) {
		if (m_1 == m_2)
			return true;
		if (m_1 != null && m_2 == null)
			return false;
		if (m_2 != null && m_1 == null)
			return false;
		if (m_1.getMultiDimMap() == null) {
			if (m_2.getMultiDimMap() != null)
				return false;
		} else if (!equalsMap(m_1.getMultiDimMap(),
				m_2.getMultiDimMap()))
			return false;
		if (m_1.getMultiDimList() == null) {
			if (m_2.getMultiDimList() != null)
				return false;
		} else if (!equalsList(m_1.getMultiDimList(),
				m_2.getMultiDimList()))
			return false;
		return true;
	}
	
	public static boolean equalsMapContainer(MapContainer container_1,
			MapContainer container_2) {
		if (container_1 == container_2)
			return true;
		if (container_1 != null && container_2 == null)
			return false;
		if (container_2 != null && container_1 == null)
			return false;
		if (container_1.getAddress2AddressMap() == null) {
			if (container_2.getAddress2AddressMap() != null)
				return false;
		} else if (!equalsMap(container_1.getAddress2AddressMap(),
				container_2.getAddress2AddressMap()))
			return false;
		if (container_1.getAddress2StringMap() == null) {
			if (container_2.getAddress2StringMap() != null)
				return false;
		} else if (!equalsMap(container_1.getAddress2StringMap(),
				container_2.getAddress2StringMap()))
			return false;
		if (container_1.getAny2AnyMap() == null) {
			if (container_2.getAny2AnyMap() != null)
				return false;
		} else if (!equalsMap(container_1.getAny2AnyMap(),
				container_2.getAny2AnyMap()))
			return false;
		if (container_1.getString2AddressMap() == null) {
			if (container_2.getString2AddressMap() != null)
				return false;
		} else if (!equalsMap(container_1.getString2AddressMap(),
				container_2.getString2AddressMap()))
			return false;
		if (container_1.getString2IntegerMap() == null) {
			if (container_2.getString2IntegerMap() != null)
				return false;
		} else if (!equalsMap(container_1.getString2IntegerMap(),
				container_2.getString2IntegerMap()))
			return false;
		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean equalsList(List list_1, List list_2) {
		 if (list_1 == list_2)
            return true;
        if (list_1.size() != list_2.size())
			return false;

        ListIterator<?> e1 = list_1.listIterator();
        ListIterator<?> e2 = list_2.listIterator();
        while (e1.hasNext() && e2.hasNext()) {
            Object o1 = e1.next();
            Object o2 = e2.next();
            if (!(o1==null ? o2==null : equalsObjects(o1, o2)))
                return false;
        }
        return !(e1.hasNext() || e2.hasNext());
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static boolean equalsMap(Map map_1, Map map_2) {
		if (map_1 == map_2)
			return true;
		if (map_2 == null)
			return false;
		if (map_1.size() != map_2.size())
			return false;

		try {
			Iterator<Entry<Object, Object>> it_1 = map_1.entrySet().iterator();
			while (it_1.hasNext()) {
				Entry<Object, Object> e_1 = it_1.next();
				Object key_1 = e_1.getKey();
				Object value_1 = e_1.getValue();
				Iterator<Entry<Object, Object>> it_2 = map_2.entrySet().iterator(); 
				boolean found = false;
				while (it_2.hasNext()) {
					Entry<Object, Object> e_2 = it_2.next();
					Object key_2 = e_2.getKey();
					Object value_2 = e_2.getValue();
					if (equalsObjects(key_1, key_2) && equalsObjects(value_1, value_2)) {
						found = true;
						break;
					}
				}
				if (!found)
					return false;
			}
		} catch (ClassCastException unused) {
			return false;
		} catch (NullPointerException unused) {
			return false;
		}

		return true;
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean equalsObjects(Object o_1, Object o_2) {
		if (o_1 == o_2)
			return true;
		if (o_1 instanceof Address && o_2 instanceof Address)
			return CompareUtil_2.equalsAddress((Address)o_1, (Address)o_2);
		if (o_1 == null) {
			if (o_2 != null)
				return false;
		} else if (o_1 instanceof Map && o_2 instanceof Map) {
			return CompareUtil_3.equalsMap((Map)o_1, (Map)o_2);
		} else if (!o_1.equals(o_2))
			return false; 
		
		return true;
	}
}
