package iot.jcypher.domain.mapping.surrogate;

import java.util.Collection;
import java.util.Map;

public abstract class AbstractSurrogate {

	@SuppressWarnings("unchecked")
	public static AbstractSurrogate createSurrogate(Object original) {
		if (original instanceof Map<?, ?>)
			return new iot.jcypher.domain.mapping.surrogate.Map((Map<Object, Object>) original);
		else if (original instanceof Collection<?>)
			return new iot.jcypher.domain.mapping.surrogate.Collection((Collection<Object>) original);
		return null;
	}
	
	public abstract Object getContent();
}
