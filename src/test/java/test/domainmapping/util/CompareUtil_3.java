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
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;

import test.domainmapping.Address;
import test.domainmapping.maps.MapContainer;
import test.domainmapping.maps.Mark;
import test.domainmapping.maps.MultiDimMapsLists;

public class CompareUtil_3 {
	
	public static boolean equalsMultiDimMapsLists(MultiDimMapsLists m_1,
			MultiDimMapsLists m_2) {
		return equalsMultiDimMapsLists(m_1, m_2, new ArrayList<AlreadyCompared>());
	}

	private static boolean equalsMultiDimMapsLists(MultiDimMapsLists m_1,
			MultiDimMapsLists m_2, List<AlreadyCompared> alreadyCompareds) {
		
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(m_1, m_2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(m_1, m_2);
		acs.add(ac);
		
		ac.setResult(true);
		if (m_1 == m_2)
			return true;
		if (m_1 != null && m_2 == null)
			return ac.setResult(false);
		if (m_2 != null && m_1 == null)
			return ac.setResult(false);
		if (m_1.getMultiDimMap() == null) {
			if (m_2.getMultiDimMap() != null)
				return ac.setResult(false);
		} else if (!equalsMap(m_1.getMultiDimMap(),
				m_2.getMultiDimMap(), acs))
			return ac.setResult(false);
		if (m_1.getMultiDimList() == null) {
			if (m_2.getMultiDimList() != null)
				return ac.setResult(false);
		} else if (!equalsList(m_1.getMultiDimList(),
				m_2.getMultiDimList(), acs))
			return ac.setResult(false);
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
				container_2.getAddress2AddressMap(), null))
			return false;
		if (container_1.getAddress2StringMap() == null) {
			if (container_2.getAddress2StringMap() != null)
				return false;
		} else if (!equalsMap(container_1.getAddress2StringMap(),
				container_2.getAddress2StringMap(), null))
			return false;
		if (container_1.getAny2AnyMap() == null) {
			if (container_2.getAny2AnyMap() != null)
				return false;
		} else if (!equalsMap(container_1.getAny2AnyMap(),
				container_2.getAny2AnyMap(), null))
			return false;
		if (container_1.getString2AddressMap() == null) {
			if (container_2.getString2AddressMap() != null)
				return false;
		} else if (!equalsMap(container_1.getString2AddressMap(),
				container_2.getString2AddressMap(), null))
			return false;
		if (container_1.getString2IntegerMap() == null) {
			if (container_2.getString2IntegerMap() != null)
				return false;
		} else if (!equalsMap(container_1.getString2IntegerMap(),
				container_2.getString2IntegerMap(), null))
			return false;
		return true;
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static boolean equalsMap(Map map_1, Map map_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(map_1, map_2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(map_1, map_2);
		acs.add(ac);
		
		ac.setResult(true);
		
		if (map_1 == map_2)
			return true;
		if (map_2 == null)
			return ac.setResult(false);
		if (map_1.size() != map_2.size())
			return ac.setResult(false);

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
					if (equalsObjects(key_1, key_2, acs) && equalsObjects(value_1, value_2, acs)) {
						found = true;
						break;
					}
				}
				if (!found)
					return ac.setResult(false);
			}
		} catch (ClassCastException unused) {
			return ac.setResult(false);
		} catch (NullPointerException unused) {
			return ac.setResult(false);
		}

		return true;
	}
	
	@SuppressWarnings("rawtypes")
	private static boolean equalsObjects(Object o_1, Object o_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		
		if (o_1 == o_2)
			return true;
		if (o_1 instanceof Address && o_2 instanceof Address)
			return CompareUtil_2.equalsAddress((Address)o_1, (Address)o_2);
		if (o_1 == null) {
			if (o_2 != null)
				return false;
		} else if (o_1 instanceof Map && o_2 instanceof Map) {
			return CompareUtil_3.equalsMap((Map)o_1, (Map)o_2, acs);
		} else if (o_1 instanceof List && o_2 instanceof List) {
			return CompareUtil_3.equalsList((List)o_1, (List)o_2, acs);
		} else if (o_1 instanceof Mark && o_2 instanceof Mark) {
			return CompareUtil_3.equalsMark((Mark)o_1, (Mark)o_2, acs);
		} else if (!o_1.equals(o_2))
			return false; 
		
		return true;
	}
	
	private static boolean equalsMark(Mark m_1, Mark m_2, List<AlreadyCompared> alreadyCompareds) {
		List<AlreadyCompared> acs = alreadyCompareds;
		if (acs == null)
			acs = new ArrayList<AlreadyCompared>();
		AlreadyCompared ac = AlreadyCompared.alreadyCompared(m_1, m_2, acs);
		if (ac != null) // avoid infinite loops
			return ac.getResult();
		
		ac = new AlreadyCompared(m_1, m_2);
		acs.add(ac);
		
		ac.setResult(true);
		if (m_1 == m_2)
			return true;
		if (m_1 != null && m_2 == null)
			return ac.setResult(false);
		if (m_2 != null && m_1 == null)
			return ac.setResult(false);
		if (m_1.getClass() != m_2.getClass())
			return ac.setResult(false);
		if (m_1.getName() == null) {
			if (m_2.getName() != null)
				return ac.setResult(false);
		} else if (!m_1.getName().equals(m_2.getName()))
			return ac.setResult(false);
		return true;
	}
}
