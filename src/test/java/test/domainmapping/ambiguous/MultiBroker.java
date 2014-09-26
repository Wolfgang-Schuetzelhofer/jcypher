package test.domainmapping.ambiguous;

import java.util.List;

import test.domainmapping.Address;

public class MultiBroker {

	private List<IPerson> canBroker;
	private String name;
	private Address address;

	public List<IPerson> getCanBroker() {
		return canBroker;
	}

	public void setCanBroker(List<IPerson> canBroker) {
		this.canBroker = canBroker;
	}

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
}
