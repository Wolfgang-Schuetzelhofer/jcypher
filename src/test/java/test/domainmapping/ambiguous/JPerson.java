package test.domainmapping.ambiguous;

import test.domainmapping.Address;

public class JPerson extends AbstractPerson {

	private int companyNumber;
	private Address companyAddress;
	
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
	@Override
	public String toString() {
		return "JPerson [companyNumber=" + companyNumber + ", companyAddress="
				+ companyAddress + ", getNamePart1()=" + getNamePart1()
				+ ", getNamePart2()=" + getNamePart2() + "]";
	}
	
}
