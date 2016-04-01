package test.domcollections;

import java.util.Set;

public class AddressNode implements Cloneable { 
    private String GUID; 
    private Set<AddressNode> preAddressNodes;
    private Set<String> preAddressNodeGUIDs;
    
	public String getGUID() {
		return GUID;
	}
	public void setGUID(String gUID) {
		GUID = gUID;
	}
	public Set<AddressNode> getPreAddressNodes() {
		return preAddressNodes;
	}
	public void setPreAddressNodes(Set<AddressNode> preAddressNodes) {
		this.preAddressNodes = preAddressNodes;
	}
	public Set<String> getPreAddressNodeGUIDs() {
		return preAddressNodeGUIDs;
	}
	public void setPreAddressNodeGUIDs(Set<String> preAddressNodeGUIDs) {
		this.preAddressNodeGUIDs = preAddressNodeGUIDs;
	}
    
}
