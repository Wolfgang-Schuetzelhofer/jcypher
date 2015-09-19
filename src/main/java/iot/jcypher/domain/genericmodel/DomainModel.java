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

import iot.jcypher.domain.genericmodel.DOType.Kind;
import iot.jcypher.domain.mapping.surrogate.AbstractSurrogate;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;
import iot.jcypher.query.api.IClause;
import iot.jcypher.query.factories.clause.CREATE;
import iot.jcypher.query.factories.clause.RETURN;
import iot.jcypher.query.factories.clause.SEPARATE;
import iot.jcypher.query.values.JcNode;
import iot.jcypher.query.values.JcNumber;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DomainModel {
	
	private static final String JavaPkg = "java.";
	private static final String EnumVals = "ENUM$VALUES";
	private static final String TypeNodePostfix = "_mdl";
	private static final String Colon = ":";
	private static final String propTypeName = "typeName";
	private static final String propSuperTypeName = "superTypeName";
	private static final String propInterfaceNames = "interfaceNames";
	private static final String propFields = "fields";
	private static final String propKind = "kind";
	
	private String domainName;
	private String typeNodeName;
	private Map<String, DOType> doTypes;
	private List<DOType> unsaved;

	public DomainModel(String domainName) {
		super();
		this.domainName = domainName;
		this.typeNodeName = domainName.concat(TypeNodePostfix);
		this.doTypes = new HashMap<String, DOType>();
		this.unsaved = new ArrayList<DOType>();
	}

	public DOType addType(Class<?> clazz) {
		if (!AbstractSurrogate.class.isAssignableFrom(clazz)) {
			String name = clazz.getName();
			boolean buildIn = isBuildIn(name);
			DOType doType;
			if ((doType = this.doTypes.get(name)) == null) {
				doType = new DOType(name, buildIn);
				this.doTypes.put(name, doType);
				if (!buildIn) {
					Kind kind = clazz.isInterface() ? Kind.INTERFACE : 
						clazz.isEnum() ? Kind.ENUM :
						Modifier.isAbstract(clazz.getModifiers()) ? Kind.ABSTRACT_CLASS : Kind.CLASS;
					doType.setKind(kind);
					this.unsaved.add(doType);
					addFields(doType, clazz);
					Class<?> sClass = clazz.getSuperclass();
					DOType superType = null;
					if (sClass != null)
						superType = addType(sClass);
					if (superType != null)
						doType.setSuperType(superType);
					Class<?>[] ifss = clazz.getInterfaces();
					if (ifss != null) {
						for (Class<?> ifs : ifss) {
							DOType interf = addType(ifs);
							if (interf != null)
								doType.getInterfaces().add(interf);
						}
					}
				}
			}
			return doType;
		}
		return null;
	}
	
	private void addFields(DOType doType, Class<?> clazz) {
		Field[] fields = clazz.getDeclaredFields();
		for (int i = 0;i < fields.length; i++) {
			if (!Modifier.isTransient(fields[i].getModifiers())) {
				if (doType.getKind() == Kind.ENUM && fields[i].getName().equals(EnumVals))
					continue;
				Class<?> fTyp = fields[i].getType();
				DOField fld = new DOField(fields[i].getName(), fTyp.getName());
				doType.getFields().add(fld);
				if (fTyp.isEnum())
					addType(fTyp);
			}
		}
	}

	public DOType getDOType(String typeName) {
		return this.doTypes.get(typeName);
	}
	
	public String getDomainName() {
		return domainName;
	}

	public String getTypeNodeName() {
		return this.typeNodeName;
	}
	
	public void loadFrom(List<GrNode> mdlInfos) {
		for(GrNode nd : mdlInfos) {
			if (nd != null) {
				GrProperty propTyp = nd.getProperty(propTypeName);
				GrProperty propSuperTyp = nd.getProperty(propSuperTypeName);
				GrProperty propFlds = nd.getProperty(propFields);
				GrProperty propKnd = nd.getProperty(propKind);
				GrProperty propIfss = nd.getProperty(propInterfaceNames);
				
				String typNm = propTyp.getValue().toString();
				
				DOType doType = addType(typNm);
				doType.setNodeId(nd.getId());
				
				String sTypNm = propSuperTyp.getValue().toString();
				if (!sTypNm.isEmpty())
					doType.setSuperType(addType(sTypNm));
				
				Object flds = propFlds.getValue();
				if (flds instanceof List<?>) {
					for (Object obj : (List<?>)flds) {
						String[] fld = obj.toString().split(":");
						DOField doField = new DOField(fld[0], fld[1]);
						doType.getFields().add(doField);
					}
				}
				
				Object ifss = propIfss.getValue();
				if (ifss instanceof List<?>) {
					for (Object obj : (List<?>)ifss) {
						doType.getInterfaces().add(addType(obj.toString()));
					}
				}
				
				Kind knd = Kind.valueOf(propKnd.getValue().toString());
				doType.setKind(knd);
			}
		}
	}
	
	private DOType addType(String typeName) {
		DOType typ = this.doTypes.get(typeName);
		if (typ == null) {
			boolean buildIn = isBuildIn(typeName);
			typ = new DOType(typeName, buildIn);
			this.doTypes.put(typeName, typ);
		}
		return typ;
	}
	
	public boolean hasChanged() {
		return this.unsaved.size() > 0;
	}
	
	@SuppressWarnings("unchecked")
	public List<IClause>[] getChangeClauses() {
		List<IClause> clauses = null;
		List<IClause> returnClauses = null;
		if (hasChanged()) {
			clauses = new ArrayList<IClause>();
			returnClauses = new ArrayList<IClause>();
			int idx = 0;
			for (DOType t : this.unsaved) {
				List<String> flds = new ArrayList<String>();
				for (DOField f : t.getFields()) {
					String fd = f.getName().concat(Colon).concat(f.getTypeName());
					flds.add(fd);
				}
				List<String> ifss = new ArrayList<String>();
				for (DOType ifs : t.getInterfaces()) {
					String ifName = ifs.getName();
					ifss.add(ifName);
				}
				String sTypeName = t.getSuperType() != null ? t.getSuperType().getName() : "";
				String strIdx = String.valueOf(idx);
				JcNode n = new JcNode("n_".concat(strIdx));
				JcNumber nid = new JcNumber("nid_".concat(strIdx));
				IClause clause = CREATE.node(n).label(getTypeNodeName())
						.property(propTypeName).value(t.getName())
						.property(propKind).value(t.getKind())
						.property(propSuperTypeName).value(sTypeName)
						.property(propInterfaceNames).value(ifss)
						.property(propFields).value(flds);
				clauses.add(SEPARATE.nextClause());
				clauses.add(clause);
				returnClauses.add(RETURN.value(n.id()).AS(nid));
				idx++;
			}
			return new List[] {clauses, returnClauses};
		}
		return null;
	}
	
	public boolean isBuildIn(String typeName) {
		return typeName.startsWith(JavaPkg);
	}
	
	public void updatedToGraph() {
		this.unsaved.clear();
	}
}
