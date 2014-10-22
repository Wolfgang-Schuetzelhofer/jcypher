package iot.jcypher.domain.mapping;

import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;

import java.lang.reflect.Field;

public class ValueAndTypeMapping extends FieldMapping {

	private static final String TypePostfix = "Type";
	
	public ValueAndTypeMapping(Field field) {
		super(field);
	}

	public ValueAndTypeMapping(Field field, String propertyName) {
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
		GrProperty typeProp = rNode.getProperty(propName);
		Class<?> clazz;
		if (typeProp != null) {
			try {
				clazz = Class.forName(typeProp.getValue().toString());
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
		} else
			clazz = super.getFieldTypeInt(rNode);
		return clazz;
	}

}
