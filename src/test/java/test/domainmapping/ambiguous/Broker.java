package test.domainmapping.ambiguous;

import test.domainmapping.Address;

public class Broker {

	private IPerson worksWith;
	private Address address;

	public IPerson getWorksWith() {
		return worksWith;
	}

	public void setWorksWith(IPerson worksWith) {
		this.worksWith = worksWith;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
	}

	@Override
	public String toString() {
		return "Broker [worksWith=" + worksWith + "]";
	}
	
}
