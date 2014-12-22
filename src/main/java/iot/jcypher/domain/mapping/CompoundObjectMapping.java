package iot.jcypher.domain.mapping;

import iot.jcypher.domain.mapping.surrogate.InnerClassSurrogate;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

public class CompoundObjectMapping extends ObjectMapping {

	private CompoundObjectType compoundObjectType;
	private Map<Class<?>, ObjectMapping> typeMappings;
	private HashSet<FieldMapping> fieldsToAccept;
	
	public CompoundObjectMapping(CompoundObjectType compoundObjectType,
			Map<Class<?>, ObjectMapping> typeMappings, Object toAccept) {
		super();
		this.compoundObjectType = compoundObjectType;
		this.typeMappings = typeMappings;
		if (toAccept != null) {
			Class<?> toAcc = toAccept.getClass();
			if (toAccept instanceof InnerClassSurrogate)
				toAcc = ((InnerClassSurrogate)toAccept).getRealClass();
			this.fieldsToAccept = new HashSet<FieldMapping>();
			Iterator<FieldMapping> it = typeMappings.get(toAcc).fieldMappingsIterator();
			while(it.hasNext()) {
				this.fieldsToAccept.add(it.next());
			}
		} else
			this.fieldsToAccept = null;
	}

	@Override
	public Iterator<FieldMapping> fieldMappingsIterator() {
		return new FieldMappingIterator();
	}

	@Override
	public boolean shouldPerformFieldMapping(FieldMapping fieldMapping) {
		return this.fieldsToAccept == null || this.fieldsToAccept.contains(fieldMapping);
	}

	@Override
	public FieldMapping getFieldMappingForField(String fieldName) {
		throw new RuntimeException("not suppported");
	}

	/****************************************/
	public class FieldMappingIterator implements Iterator<FieldMapping> {

		private HashSet<FieldMapping> fieldMappingsSet = new HashSet<FieldMapping>();
		private Iterator<CompoundObjectType> typeIterator = compoundObjectType.typeIterator();
		private Iterator<FieldMapping> currentTypeFieldIterator;
		private FieldMapping nextFieldMapping;
		
		@Override
		public boolean hasNext() {
			this.nextFieldMapping = null;
			if (this.currentTypeFieldIterator == null)
				switchToNextType();
			if (this.currentTypeFieldIterator != null) { // else the end is reached
				while (this.currentTypeFieldIterator.hasNext()) {
					FieldMapping next = this.currentTypeFieldIterator.next();
					if (this.fieldMappingsSet.add(next)) { // this fieldMapping was not returned until now
						this.nextFieldMapping = next;
						return true;
					}
				}
				// reached the end of the currentTypeFieldIterator
				// recursively continue with the next one
				this.currentTypeFieldIterator = null;
				return hasNext();
			}
			return false;
		}

		@Override
		public FieldMapping next() {
			return this.nextFieldMapping;
		}

		@Override
		public void remove() {
			throw new RuntimeException("operation not supported");
		}
		
		private void switchToNextType() {
			while (this.typeIterator.hasNext()) {
				CompoundObjectType cType = this.typeIterator.next();
				Class<?> typ = cType.getType();
				ObjectMapping om = typeMappings.get(typ);
				if (om != null) { // may be null if the type is a supertype or interface, which itself has never
										   // been stored to the graph
					this.currentTypeFieldIterator = om.fieldMappingsIterator();
					return;
				}
			}
			
			// we hav reached the end
			this.currentTypeFieldIterator = null;
		}
	}
}
