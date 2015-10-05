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

package iot.jcypher.domain.genericmodel.internal;

import iot.jcypher.domain.genericmodel.DOField;
import iot.jcypher.domain.genericmodel.DomainObject;
import iot.jcypher.domain.genericmodel.internal.DOWalker.Field;

import java.util.List;

public interface IDOVisitor {

	public void startVisitDomainObjects(List<DomainObject> domainObjects);
	public void endVisitDomainObjects(List<DomainObject> domainObjects);
	
	public void startVisitDomainObject(DomainObject domainObject, Field field, int depth);
	public void endVisitDomainObject(DomainObject domainObject, Field field, int depth);
	
	public void startVisitField(DOField field, Object fieldValue, int depth);
	public void endVisitField(DOField field, Object fieldValue, int depth);
}
