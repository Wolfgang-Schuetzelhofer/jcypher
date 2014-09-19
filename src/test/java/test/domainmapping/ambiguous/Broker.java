package test.domainmapping.ambiguous;

public class Broker {

	private IPerson worksWith;

	public IPerson getWorksWith() {
		return worksWith;
	}

	public void setWorksWith(IPerson worksWith) {
		this.worksWith = worksWith;
	}

	@Override
	public String toString() {
		return "Broker [worksWith=" + worksWith + "]";
	}
	
}
