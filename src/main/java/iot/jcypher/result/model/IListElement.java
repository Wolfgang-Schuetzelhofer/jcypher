package iot.jcypher.result.model;

import java.util.List;

public interface IListElement<T extends JcrElement<?>> {

	public List<T> allResults();
}
