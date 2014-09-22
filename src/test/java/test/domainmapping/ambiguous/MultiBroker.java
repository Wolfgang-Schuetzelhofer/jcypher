package test.domainmapping.ambiguous;

import java.util.List;

public class MultiBroker {

	private List<IPerson> canBroker;

	public List<IPerson> getCanBroker() {
		return canBroker;
	}

	public void setCanBroker(List<IPerson> canBroker) {
		this.canBroker = canBroker;
	}
}
