package iot.jcypher.result.model;

import iot.jcypher.result.util.ResultHandler;

public class JcrRelation extends JcrElement<JcrRelation> {

	private long startNodeId;
	private long endNodeId;
	
	JcrRelation(ResultHandler resultHandler, long id, String name,
			long startNodeId, long endNodeId) {
		super(resultHandler, id, name);
		this.startNodeId = startNodeId;
		this.endNodeId = endNodeId;
	}

}
