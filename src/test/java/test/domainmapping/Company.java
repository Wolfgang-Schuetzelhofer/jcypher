package test.domainmapping;

public class Company {

	private String name;
	private Address address;
	
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
	
	@Override
	public String toString() {
		return "Company [name=" + name + ", address=" + address + "]";
	}
	
}
