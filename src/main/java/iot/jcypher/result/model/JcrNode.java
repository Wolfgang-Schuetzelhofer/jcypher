package iot.jcypher.result.model;

import iot.jcypher.result.util.ResultHandler;

public class JcrNode extends JcrElement<JcrNode> {

	JcrNode(ResultHandler resultHandler, long id, String name) {
		super(resultHandler, id, name);
	}

}
