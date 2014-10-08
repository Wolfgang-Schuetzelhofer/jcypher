package test.domainmapping.maps;

import java.util.Map;

import test.domainmapping.Address;

public class MapContainer {

	// simple, simple
	private Map<String, Integer> string2IntegerMap;
	
	// simple, complex
	private Map<String, Address> string2AddressMap;
	
	// complex, simple
	private Map<Address, String> address2StringMap;
	
	// complex, complex
	private Map<Address, Address> address2AddressMap;
	
	// any, any
	private Map<?, ?> any2AnyMap;

	public Map<String, Address> getString2AddressMap() {
		return string2AddressMap;
	}

	public void setString2AddressMap(Map<String, Address> string2AddressMap) {
		this.string2AddressMap = string2AddressMap;
	}

	public Map<Address, String> getAddress2StringMap() {
		return address2StringMap;
	}

	public void setAddress2StringMap(Map<Address, String> address2StringMap) {
		this.address2StringMap = address2StringMap;
	}

	public Map<Address, Address> getAddress2AddressMap() {
		return address2AddressMap;
	}

	public void setAddress2AddressMap(Map<Address, Address> address2AddressMap) {
		this.address2AddressMap = address2AddressMap;
	}

	public Map<String, Integer> getString2IntegerMap() {
		return string2IntegerMap;
	}

	public void setString2IntegerMap(Map<String, Integer> string2IntegerMap) {
		this.string2IntegerMap = string2IntegerMap;
	}

	public Map<?, ?> getAny2AnyMap() {
		return any2AnyMap;
	}

	public void setAny2AnyMap(Map<?, ?> any2AnyMap) {
		this.any2AnyMap = any2AnyMap;
	}

}
