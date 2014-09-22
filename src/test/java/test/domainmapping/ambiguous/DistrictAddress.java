package test.domainmapping.ambiguous;

import test.domainmapping.Address;

public class DistrictAddress extends Address {

	private District district;
	private District subDistrict;

	public District getDistrict() {
		return district;
	}

	public void setDistrict(District district) {
		this.district = district;
	}

	public District getSubDistrict() {
		return subDistrict;
	}

	public void setSubDistrict(District subDistrict) {
		this.subDistrict = subDistrict;
	}
	
}
