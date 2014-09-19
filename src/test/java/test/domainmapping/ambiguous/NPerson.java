package test.domainmapping.ambiguous;

import test.domainmapping.Address;

public class NPerson extends AbstractPerson {
	private String socialSecurityNumber;
	private Address homeAddress;
	
	public String getSocialSecurityNumber() {
		return socialSecurityNumber;
	}
	public void setSocialSecurityNumber(String socialSecurityNumber) {
		this.socialSecurityNumber = socialSecurityNumber;
	}
	public Address getHomeAddress() {
		return homeAddress;
	}
	public void setHomeAddress(Address homeAddress) {
		this.homeAddress = homeAddress;
	}
	@Override
	public String toString() {
		return "NPerson [socialSecurityNumber=" + socialSecurityNumber
				+ ", homeAddress=" + homeAddress + ", getNamePart1()="
				+ getNamePart1() + ", getNamePart2()=" + getNamePart2() + "]";
	}
	
}
