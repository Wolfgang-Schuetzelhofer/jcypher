package test.domainmapping.maps;

import java.util.Map;

import test.domainmapping.Address;

public class AddressMap {

	private Map<String, Address> addresses;

	public Map<String, Address> getAddresses() {
		return addresses;
	}

	public void setAddresses(Map<String, Address> addresses) {
		this.addresses = addresses;
	}
}
