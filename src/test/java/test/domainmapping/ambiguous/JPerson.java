package test.domainmapping.ambiguous;

import test.domainmapping.Address;

public class JPerson extends AbstractPerson {

	private int companyNumber;
	private Address companyAddress;
	private Address contactAddress;
	private Address postalAddress;
	
	public int getCompanyNumber() {
		return companyNumber;
	}
	public void setCompanyNumber(int companyNumber) {
		this.companyNumber = companyNumber;
	}
	public Address getCompanyAddress() {
		return companyAddress;
	}
	public void setCompanyAddress(Address companyAddress) {
		this.companyAddress = companyAddress;
	}
	public Address getContactAddress() {
		return contactAddress;
	}
	public void setContactAddress(Address contactAddress) {
		this.contactAddress = contactAddress;
	}
	public Address getPostalAddress() {
		return postalAddress;
	}
	public void setPostalAddress(Address postalAddress) {
		this.postalAddress = postalAddress;
	}
	@Override
	public String toString() {
		return "JPerson [companyNumber=" + companyNumber + ", companyAddress="
				+ companyAddress + ", getNamePart1()=" + getNamePart1()
				+ ", getNamePart2()=" + getNamePart2() + "]";
	}
	
}
