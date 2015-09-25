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

import java.util.ArrayList;
import java.util.List;

public class DOType {

	private long nodeId;
	
	private String name;
	private Kind kind;
	private DOType superType;
	private List<DOType> interfaces;
	private List<DOField> fields;
	private boolean buildIn;

	/**
	 * Create a ClassBuilder which allows to build a generic domain object type representing a <b>Class</b>.
	 * @param typeName fully qualified name e.g. <b>iot.jcypher.samples.domain.people.model.Person</b>
	 * @return a DOClassBuilder
	 */
	public static DOClassBuilder createClassBuilder(String typeName) {
		DOType doType = new DOType(typeName);
		doType.kind = Kind.CLASS;
		return doType.createClassBuilder();
	}
	
	/**
	 * Create a ClassBuilder which allows to build a generic domain object type representing an <b>Interface</b>.
	 * @param typeName fully qualified name e.g. <b>iot.jcypher.samples.domain.people.model.PointOfContact</b>
	 * @return a DOInterfaceBuilder
	 */
	public static DOInterfaceBuilder createInterfaceBuilder(String typeName) {
		DOType doType = new DOType(typeName);
		doType.kind = Kind.INTERFACE;
		return doType.createInterfaceBuilder();
	}
	
	/**
	 * Create a ClassBuilder which allows to build a generic domain object type representing an <b>Enum</b>.
	 * @param typeName fully qualified name e.g. <b>iot.jcypher.samples.domain.people.model.Gender</b>
	 * @return a DOEnumBuilder
	 */
	public static DOEnumBuilder createEnumBuilder(String typeName) {
		DOType doType = new DOType(typeName);
		doType.kind = Kind.ENUM;
		return doType.createEnumBuilder();
	}
	
	DOClassBuilder createClassBuilder() {
		return new DOClassBuilder();
	}
	
	private DOInterfaceBuilder createInterfaceBuilder() {
		return new DOInterfaceBuilder();
	}
	
	private DOEnumBuilder createEnumBuilder() {
		return new DOEnumBuilder();
	}
	
	DOType(String typeName) {
		super();
		this.name = typeName;
		this.fields = new ArrayList<DOField>();
		this.interfaces = new ArrayList<DOType>();
		this.nodeId = -1;
		this.buildIn = DomainModel.isBuildIn(typeName);
	}
	
	/**
	 * Answer the fully qualified name of this type e.g. <b>iot.jcypher.samples.domain.people.model.Person</b>
	 * @return the fully qualified name of this type
	 */
	public String getName() {
		return name;
	}

	public List<DOField> getFields() {
		return fields;
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

	long getNodeId() {
		return nodeId;
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
		for (DOField f : this.fields) {
			sb.append('\n');
			sb.append(f.asString(indent2));
		}
		sb.append(indent);
		sb.append('}');
		return sb.toString();
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
			kind = k;
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
			if (superType.getKind() != Kind.CLASS && superType.getKind() != Kind.ABSTRACT_CLASS)
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
			if (superType.getKind() != Kind.INTERFACE)
				throw new RuntimeException("must be a kind of Interface");
			getInterfaces().add(anInterface);
		}
		
		/**
		 * Set a builder for the interface to be added to the constructed domain object Class.
		 * <br/>This is an alternative to addInterface() if building the interface is not finished yet.
		 * @param builder
		 */
		public void addInterfaceBuilder(DOInterfaceBuilder builder) {
			getInterfaces().add(builder.build());
		}
		
		/**
		 * Add a field i.e. an attribute definition to the domain object type.
		 * @param name field- (attribute) name
		 * @param typeName qualified type name
		 */
		public void addField(String name, String typeName) {
			getFields().add(new DOField(name, typeName));
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
			if (superType.getKind() != Kind.INTERFACE)
				throw new RuntimeException("must be a kind of Interface");
			getInterfaces().add(anInterface);
		}
		
		/**
		 * Set a builder for the interface to be added to this interface.
		 * <br/>This is an alternative to addInterface() if building the interface is not finished yet.
		 * @param builder
		 */
		public void addInterfaceBuilder(DOInterfaceBuilder builder) {
			getInterfaces().add(builder.build());
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
	}
	
}
