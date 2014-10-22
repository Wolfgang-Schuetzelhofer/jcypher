package iot.jcypher.domain.mapping.surrogate;

import java.util.Map;

import iot.jcypher.domain.mapping.FieldMapping;

public class Map2DO extends AbstractDeferred {

	private FieldMapping fieldMapping;
	private Map<Object, Object> map;
	private Object domainObject;
	
	public Map2DO(FieldMapping fieldMapping, Map<Object, Object> map,
			Object domainObject) {
		super();
		this.fieldMapping = fieldMapping;
		this.map = map;
		this.domainObject = domainObject;
	}

	@Override
	public void performUpdate() {
		if (!this.map.isEmpty()) // empty maps have been mapped to aproperty
			this.fieldMapping.setFieldValue(this.domainObject, this.map);
		modifyNextUp();
	}
	
	public Map<Object, Object> getMap() {
		return map;
	}
}
