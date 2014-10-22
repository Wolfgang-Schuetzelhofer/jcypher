package iot.jcypher.domain.mapping.surrogate;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDeferred implements IDeferred{

	private IDeferred nextUpInTree;
	private List<IDeferred> downInTree;
	
	public AbstractDeferred() {
		super();
		this.downInTree = new ArrayList<IDeferred>();
	}

	@Override
	public boolean isLeaf() {
		return this.downInTree.isEmpty();
	}

	@Override
	public IDeferred nextUp() {
		return this.nextUpInTree;
	}
	
	@Override
	public void setNextUpInTree(IDeferred deferred) {
		this.nextUpInTree = deferred;
		((AbstractDeferred)deferred).addDownInTree(this);
	}
	
	public void addDownInTree(IDeferred dit) {
		this.downInTree.add(dit);
	}
	
	@Override
	public void modifiedBy(IDeferred changer) {
		this.downInTree.remove(changer);
	}
	
	protected void modifyNextUp() {
		if (this.nextUpInTree != null)
			this.nextUpInTree.modifiedBy(this);
	}
}
