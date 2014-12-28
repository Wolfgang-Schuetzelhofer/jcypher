/************************************************************************
 * Copyright (c) 2014 IoT-Solutions e.U.
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

package iot.jcypher.domain.mapping.surrogate;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import iot.jcypher.domain.internal.DomainAccess.IRecursionExit;
import iot.jcypher.domain.internal.DomainAccess.InternalDomainAccess;
import iot.jcypher.domain.mapping.FieldMapping;
import iot.jcypher.domain.mapping.DomainState.Relation.RelationUpdate;
import iot.jcypher.graph.GrNode;
import iot.jcypher.graph.GrProperty;

public class InnerClassSurrogate {

	private transient Object realObject;
	private transient Constructor<?> constructor;
	private transient List<InnerClassSurrogate> toConstruct;
	private transient List<DeferredFieldMapping> parents;
	private transient List<DeferredFieldMapping> children;
	private transient List<DeferredPropertyMapping> propertyChildren;
	private transient IRecursionExit recursionExit;
	private transient int actResolutionDepth;
	private transient InternalDomainAccess id2ObjectMapper;
	private transient long nodeId;
	private transient List<RelationUpdate> relationUpdates;
	
	public InnerClassSurrogate(Constructor<?> constructor) {
		super();
		this.constructor = constructor;
	}
	
	private void addToConstruct(InnerClassSurrogate ics) {
		if (this.realObject == null) {
			if (this.toConstruct == null)
				this.toConstruct = new ArrayList<InnerClassSurrogate>();
			this.toConstruct.add(ics);
		} else {
			ics.constructAndFill(this.realObject);
		}
	}
	
	private void constructAndFill(Object obj) {
		try {
			this.realObject = this.constructor.newInstance(obj);
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		fill();
	}

	public void addChild(FieldMapping fm, Object obj) {
		if (fm.isInnerClassRefField()) {
			if (obj instanceof InnerClassSurrogate) {
				((InnerClassSurrogate)obj).addToConstruct(this);
			} else {
				constructAndFill(obj);
			}
		} else {
			if (this.realObject == null) {
				if (this.children == null)
					this.children = new ArrayList<DeferredFieldMapping>();
				this.children.add(new DeferredFieldMapping(fm, obj));
			} else {
				fm.setFieldValue(this.realObject, obj);
			}
		}
	}
	
	private void fill() {
		if (this.children != null) {
			for (DeferredFieldMapping dfm : this.children) {
				dfm.fieldMapping.setFieldValue(this.realObject, dfm.object);
			}
		}
		if (this.propertyChildren != null) {
			for (DeferredPropertyMapping dpm : this.propertyChildren) {
				dpm.fieldMapping.mapPropertyToField(this.realObject, dpm.grNode);
			}
		}
		if (this.parents != null) {
			for (DeferredFieldMapping dfm : this.parents) {
				dfm.fieldMapping.setFieldValue(dfm.object, this.realObject);
			}
		}
		this.id2ObjectMapper.replace_Id2Object(this, this.realObject, this.nodeId);
		if (this.recursionExit != null)
			this.recursionExit.addRecursionExitObject(this.realObject, this.actResolutionDepth);
		if (this.toConstruct != null) {
			for (InnerClassSurrogate ics : this.toConstruct) {
				ics.constructAndFill(this.realObject);
			}
		}
		if (this.relationUpdates != null) {
			for (RelationUpdate ru : this.relationUpdates) {
				ru.updateWith(this.realObject);
			}
		}
	}
	
	/**
	 * @param fm
	 * @param obj
	 * @param node
	 * @return true if the property exists in the node
	 */
	public boolean addPropertyChild(FieldMapping fm, Object obj, GrNode node) {
		if (!fm.isInnerClassRefField()) {
			if (this.realObject == null) {
				if (this.propertyChildren == null)
					this.propertyChildren = new ArrayList<DeferredPropertyMapping>();
				this.propertyChildren.add(new DeferredPropertyMapping(fm, obj, node));
				GrProperty prop = node.getProperty(fm.getPropertyOrRelationName());
				return prop != null;
			} else {
				return fm.mapPropertyToField(this.realObject, node);
			}
		}
		return false;
	}
	
	public void addParent(FieldMapping fm, Object obj) {
		if (this.realObject == null) {
			if (this.parents == null)
				this.parents = new ArrayList<DeferredFieldMapping>();
			this.parents.add(new DeferredFieldMapping(fm, obj));
		} else {
			fm.setFieldValue(obj, this.realObject);
		}
	}
	
	public Class<?> getRealClass() {
		return this.constructor.getDeclaringClass();
	}
	
	public Object getRealObject() {
		return realObject;
	}

	public void setRecursionExit(IRecursionExit recursionExit) {
		this.recursionExit = recursionExit;
	}

	public void setActResolutionDepth(int actResolutionDepth) {
		this.actResolutionDepth = actResolutionDepth;
	}

	public void setId2ObjectMapper(InternalDomainAccess id2ObjectMapper) {
		this.id2ObjectMapper = id2ObjectMapper;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	public void addRelationUpdate(RelationUpdate relationUpdate) {
		if (this.realObject == null) {
			if (this.relationUpdates == null)
				this.relationUpdates = new ArrayList<RelationUpdate>();
			this.relationUpdates.add(relationUpdate);
		} else
			relationUpdate.updateWith(this.realObject);
	}

	/*****************************/
	private class DeferredFieldMapping {
		protected FieldMapping fieldMapping;
		protected Object object;
		
		private DeferredFieldMapping(FieldMapping fieldMapping, Object object) {
			super();
			this.fieldMapping = fieldMapping;
			this.object = object;
		}
		
	}
	
	/*****************************/
	private class DeferredPropertyMapping extends DeferredFieldMapping {
		private GrNode grNode;
		
		private DeferredPropertyMapping(FieldMapping fieldMapping, Object object, GrNode n) {
			super(fieldMapping, object);
			this.grNode = n;
		}
	}
}
