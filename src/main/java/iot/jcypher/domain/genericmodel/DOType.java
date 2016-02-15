/************************************************************************
 * Copyright (c) 2015 IoT-Solutions e.U.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ************************************************************************/

package iot.jcypher.domain.genericmodel;

import iot.jcypher.domain.genericmodel.internal.DomainModel;
import iot.jcypher.domain.mapping.MappingUtil;

import java.util.ArrayList;
import java.util.List;

public class DOType {

	private long nodeId;
	
	private String name;
	private Kind kind;
	private DOType superType;
	private List<DOType> interfaces;
	private List<DOField> declaredFields;
	private List<String> declaredFieldNames;
	private boolean buildIn;
	private DomainModel domainModel;

	DOClassBuilder createClassBuilder() {
		DOClassBuilder ret = new DOClassBuilder();
		ret.setKind(Kind.CLASS);
		return ret;
	}
	
	DOInterfaceBuilder createInterfaceBuilder() {
		DOInterfaceBuilder ret = new DOInterfaceBuilder();
		ret.setKind(Kind.INTERFACE);
		return ret;
	}
	
	DOEnumBuilder createEnumBuilder() {
		DOEnumBuilder ret = new DOEnumBuilder();
		ret.setKind(Kind.ENUM);
		return ret;
	}
	
	DOType(String typeName, DomainModel domainModel) {
		super();
		this.name = typeName;
		this.declaredFields = new ArrayList<DOField>();
		this.interfaces = new ArrayList<DOType>();
		this.nodeId = -1;
		this.buildIn = DomainModel.isBuildIn(typeName);
		this.domainModel = domainModel;
	}
	
	/**
	 * Answer the fully qualified name of this type e.g. <b>iot.jcypher.samples.domain.people.model.Person</b>
	 * @return the fully qualified name of this type
	 */
	public String getName() {
		return name;
	}

	/**
	 * Answer the field- (attribute) definitions declared by this type.
	 * @return a list of DOField
	 */
	public List<DOField> getDeclaredFields() {
		return declaredFields;
	}
	
	/**
	 * Answer the field- (attribute) definitions declared by this type and all it's super types.
	 * @return a list of DOField
	 */
	public List<DOField> getFields() {
	List<DOField> ret = new ArrayList<DOField>();
		DOType typ = this;
		while(typ != null) {
			ret.addAll(typ.getDeclaredFields());
			typ = typ.getSuperType();
		}
		return ret;
	}
	
	/**
	 * Answer a list of all field names declared by this type.
	 * @return
	 */
	public List<String> getDeclaredFieldNames() {
		if (this.declaredFieldNames == null) {
			this.declaredFieldNames = new ArrayList<String>(this.declaredFields.size());
			for (DOField f : this.declaredFields) {
				this.declaredFieldNames.add(f.getName());
			}
		}
		return this.declaredFieldNames;
	}
	
	/**
	 * Answer a list of all field names declared by this type and all it's super types.
	 * @return
	 */
	public List<String> getFieldNames() {
		List<String> ret = new ArrayList<String>();
		DOType typ = this;
		while(typ != null) {
			ret.addAll(typ.getDeclaredFieldNames());
			typ = typ.getSuperType();
		}
		return ret;
	}
	
	/**
	 * Answer the field at the given index in the list of all fields.
	 * @param index
	 * @return
	 */
	public DOField getFieldByIndex(int index) {
		return this.getFields().get(index);
	}
	
	/**
	 * Answer the index of the field with the given name within the list of all fields.
	 * <br/>Answer -1 if a field with the given name does not exist.
	 * @param fieldName
	 * @return
	 */
	public int getIndexOfField(String fieldName) {
		List<String> fnms = this.getFieldNames();
		for (int i = 0; i < fnms.size(); i++) {
			if (fnms.get(i).equals(fieldName))
				return i;
		}
		return -1;
	}
	
	/**
	 * Answer the field with the given name.
	 * <br/>Answer null if a field with the given name does not exist.
	 * @param fieldName
	 * @return
	 */
	public DOField getFieldByName(String fieldName) {
		int idx = this.getIndexOfField(fieldName);
		if (idx != -1)
			return this.getFieldByIndex(idx);
		return null;
	}

	public DOType getSuperType() {
		return superType;
	}

	public List<DOType> getInterfaces() {
		return interfaces;
	}

	/**
	 * Answer the kind of this type which can be <b>CLASS | ABSTRACT_CLASS | INTERFACE | ENUM</b>
	 * @return
	 */
	public Kind getKind() {
		return kind;
	}
	
	/**
	 * Answer an enum value with the given name.
	 * @param name
	 * @return
	 */
	public Object getEnumValue(String name) {
		if (this.kind != Kind.ENUM)
			throw new RuntimeException("getEnumValue(..) can only be called on an enum");
		Object[] vals = getEnumValues();
		if (vals != null) {
			for (Object val : vals) {
				if (((Enum<?>)val).name().equals(name))
					return val;
			}
		}
		return null;
	}
	
	/**
	 * Answer the list of enum values of this enum.
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public Object[] getEnumValues() {
		if (this.kind != Kind.ENUM)
			throw new RuntimeException("getEnumValues() can only be called on an enum");
		try {
			return MappingUtil.getEnumValues((Class<? extends Enum<?>>) getRawType());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
	}

	long getNodeId() {
		return nodeId;
	}
	
	void addDeclaredFieldUnique(DOField field) {
		if (!containsDeclaredField(field.getName()))
			getDeclaredFields().add(field);
	}
	
	private boolean containsDeclaredField(String fieldName) {
		List<DOField> flds = this.getDeclaredFields();
		for (DOField f : flds) {
			if (f.getName().equals(fieldName))
				return true;
		}
		return false;
	}
	
	void addInterfaceUnique(DOType intf) {
		if (!containsInterface(intf.getName()))
			getInterfaces().add(intf);
	}
	
	private boolean containsInterface(String fName) {
		List<DOType> ifs = this.getInterfaces();
		for (DOType intf : ifs) {
			if (intf.getName().equals(fName))
				return true;
		}
		return false;
	}

	void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}
	
	public boolean isBuildIn() {
		return buildIn;
	}
	
	public String asString(String indent) {
		String indent2 = "".concat(indent).concat(indent);
		StringBuilder sb = new StringBuilder();
		sb.append(indent);
		if (this.kind != null) {
			sb.append(this.kind);
			sb.append(' ');
		}
		sb.append(this.name);
		if (this.superType != null) {
			sb.append(" extends ");
			sb.append(this.superType.getName());
		}
		if (this.interfaces.size() > 0) {
			sb.append(" implements");
			for (DOType t : this.interfaces) {
				sb.append(' ');
				sb.append(t.getName());
			}
		}
		sb.append(" {");
		for (DOField f : this.declaredFields) {
			sb.append('\n');
			sb.append(f.asString(indent2));
		}
		sb.append(indent);
		sb.append('}');
		return sb.toString();
	}
	
	DomainModel getDomainModel() {
		return this.domainModel;
	}
	
	Class<?> getRawType() throws ClassNotFoundException {
		return this.domainModel.getClassForName(this.name);
	}

	/******************************/
	public enum Kind {
		CLASS, ABSTRACT_CLASS, INTERFACE, ENUM
	}
	
	/******************************/
	public abstract class Builder {
		
		private Builder() {
			super();
		}
		
		void setKind(Kind k) {
			DOType.this.kind = k;
		}
		
		void setSuperTypeInternal(DOType superType) {
			DOType.this.superType = superType;
		}
		
		public DOType build() {
			return DOType.this;
		}
	}
	
	/******************************/
	public class DOClassBuilder extends Builder {

		private DOClassBuilder() {
			super();
		}
		
		/**
		 * Define the constructed domain object type to represent an <b>Abstract Class<b/>
		 */
		public void setAbstract() {
			kind = Kind.ABSTRACT_CLASS;
		}
		
		/**
		 * Set the super type of the constructed domain object Class.
		 * <br/>If it is not set or set to null, 'java.lang.Object' will implicitly be used as super type
		 * @param superType
		 */
		public void setSuperType(DOType superType) {
			if (superType.getKind() != Kind.CLASS && superType.getKind() != Kind.ABSTRACT_CLASS &&
					!superType.getName().equals("java.lang.Object"))
				throw new RuntimeException("super type must be a Class or an Abstract Class");
			super.setSuperTypeInternal(superType);
		}
		
		/**
		 * Set a builder for the super type of the constructed domain object Class.
		 * <br/>This is an alternative to setSuperType() if building the super type is not finished yet.
		 * @param superTypeBuilder
		 */
		public void setSuperTypeBuilder(DOClassBuilder superTypeBuilder) {
			super.setSuperTypeInternal(superTypeBuilder.build());
		}
		
		/**
		 * Add an interface definition to the constructed domain object Class.
		 * @param anInterface
		 */
		public void addInterface(DOType anInterface) {
			if (anInterface.getKind() != Kind.INTERFACE)
				throw new RuntimeException("must be a kind of Interface");
			addInterfaceUnique(anInterface);
		}
		
		/**
		 * Set a builder for the interface to be added to the constructed domain object Class.
		 * <br/>This is an alternative to addInterface() if building the interface is not finished yet.
		 * @param builder
		 */
		public void addInterfaceBuilder(DOInterfaceBuilder builder) {
			addInterfaceUnique(builder.build());
		}
		
		/**
		 * Add a field i.e. an attribute definition to the domain object type.
		 * @param name field- (attribute) name
		 * @param typeName qualified type name
		 */
		public void addField(String name, String typeName) {
			addDeclaredFieldUnique(new DOField(name, typeName, false, DOType.this));
		}
		
		/**
		 * Add a list field i.e. a list or array attribute defined in a domain object type.
		 * @param name of the field (attribute)
		 * @param componentTypeName if null, java.lang.Object will be taken as component type
		 */
		public void addListField(String name, String componentTypeName) {
			addDeclaredFieldUnique(new DOField(name, componentTypeName, true, DOType.this));
		}
	}
	
	/******************************/
	public class DOInterfaceBuilder extends Builder {

		private DOInterfaceBuilder() {
			super();
		}
		
		/**
		 * Add an interface definition to to be extended by this interface.
		 * @param anInterface
		 */
		public void addInterface(DOType anInterface) {
			if (anInterface.getKind() != Kind.INTERFACE)
				throw new RuntimeException("must be a kind of Interface");
			addInterfaceUnique(anInterface);
		}
		
		/**
		 * Set a builder for the interface to be added to this interface.
		 * <br/>This is an alternative to addInterface() if building the interface is not finished yet.
		 * @param builder
		 */
		public void addInterfaceBuilder(DOInterfaceBuilder builder) {
			addInterfaceUnique(builder.build());
		}
	}
	
	/******************************/
	public class DOEnumBuilder extends Builder {

		private DOEnumBuilder() {
			super();
		}
		
		/**
		 * Set the super type of the constructed Enum.
		 * <br/>If it is not set or set to null, 'java.lang.Enum' will implicitly be used as super type
		 * @param superType
		 */
		public void setSuperType(DOType superType) {
			if (superType.getKind() != Kind.ENUM && !superType.getName().equals("java.lang.Enum"))
				throw new RuntimeException("super type must be an Enum");
			super.setSuperTypeInternal(superType);
		}
		
		/**
		 * Set a builder for the super type of the constructed Enum.
		 * <br/>This is an alternative to setSuperType() if building the super type is not finished yet.
		 * @param superTypeBuilder
		 */
		public void setSuperTypeBuilder(DOEnumBuilder superTypeBuilder) {
			super.setSuperTypeInternal(superTypeBuilder.build());
		}
		
		/**
		 * Add an enum value to the enum to be constructed.
		 * @param name
		 */
		public void addEnumValue(String name) {
			addDeclaredFieldUnique(new DOField(name, DOType.this.name, false, DOType.this));
		}
	}
	
}
