package iot.jcypher.result.model;

import java.util.ArrayList;
import java.util.List;

import iot.jcypher.result.util.ResultHandler;

public abstract class JcrElement<T extends JcrElement<?>> implements IListElement<T> {

	protected ResultHandler resultHandler;
	private long id;
	private String name;

	JcrElement(ResultHandler resultHandler, long id, String name) {
		super();
		this.resultHandler = resultHandler;
		this.id = id;
		this.name = name;
	}

	public long getId() {
		return id;
	}

	@Override
	public List<T> allResults() {
		List<T> ret = new ArrayList<T>();
		this.resultHandler.fillInWholeColumn(this.name, ret);
		return ret;
	}
	
}
