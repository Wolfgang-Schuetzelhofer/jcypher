package test.domainmapping.ambiguous;

import test.domainmapping.Address;

public class NPerson extends AbstractPerson {
	private String socialSecurityNumber;
	private Address homeAddress;
	private Address workAddress;
	
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
	public Address getWorkAddress() {
		return workAddress;
	}
	public void setWorkAddress(Address workAddress) {
		this.workAddress = workAddress;
	}
	@Override
	public String toString() {
		return "NPerson [socialSecurityNumber=" + socialSecurityNumber
				+ ", homeAddress=" + homeAddress + ", getNamePart1()="
				+ getNamePart1() + ", getNamePart2()=" + getNamePart2() + "]";
	}
	
}
