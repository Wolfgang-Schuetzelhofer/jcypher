package iot.jcypher.result.model;

import iot.jcypher.result.util.ResultHandler;

public class JcrAccess {

	public static JcrNode createNode(ResultHandler rh, long id, String name) {
		return new JcrNode(rh, id, name);
	}
	
	public static JcrRelation createRelation(ResultHandler rh, long id, String name,
			long startNodeId, long endNodeId) {
		return new JcrRelation(rh, id, name, startNodeId, endNodeId);
	}
}
