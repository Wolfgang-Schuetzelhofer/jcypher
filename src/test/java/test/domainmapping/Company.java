package test.domainmapping;

import java.util.List;

public class Company {

	private String name;
	private Address address;
	@SuppressWarnings("rawtypes")
	private List areaCodes;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Address getAddress() {
		return address;
	}
	public void setAddress(Address address) {
		this.address = address;
	}
	
	@SuppressWarnings("rawtypes")
	public List getAreaCodes() {
		return areaCodes;
	}
	@SuppressWarnings("rawtypes")
	public void setAreaCodes(List areaCodes) {
		this.areaCodes = areaCodes;
	}
	@Override
	public String toString() {
		return "Company [name=" + name + ", address=" + address + "]";
	}
	
}
