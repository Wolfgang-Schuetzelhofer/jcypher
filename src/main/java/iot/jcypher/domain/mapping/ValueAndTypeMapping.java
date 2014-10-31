package iot.jcypher.domain.mapping;

import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;

public class ValueAndTypeMapping extends FieldMapping {

	private static final String TypePostfix = "Type";
	
	public ValueAndTypeMapping(IField field) {
		super(field);
	}

	public ValueAndTypeMapping(IField field, String propertyName) {
		super(field, propertyName);
	}

	@Override
	public void mapPropertyFromField(Object domainObject, GrNode rNode) {
		Object mapped = super.intMapPropertyFromField(domainObject, rNode);
		String propName = getPropertyOrRelationName().concat(TypePostfix);
		GrProperty prop = rNode.getProperty(propName);
		if (mapped != null) {
			String value = mapped.getClass().getName();
			if (prop != null) {
				Object propValue = prop.getValue(); // String need not be converted
				if (!value.equals(propValue)) {
					prop.setValue(value);
				}
			} else
				rNode.addProperty(propName, value);
		} else { // remove if needed
			if (prop != null)
				prop.setValue(null);
		}
	}

	@Override
	protected Class<?> getFieldTypeInt(GrNode rNode) {
		String propName = getPropertyOrRelationName().concat(TypePostfix);
		Class<?> clazz = getTypeFromProperty(rNode, propName);
		if (clazz == null)
			clazz = super.getFieldTypeInt(rNode);
		return clazz;
	}

}
