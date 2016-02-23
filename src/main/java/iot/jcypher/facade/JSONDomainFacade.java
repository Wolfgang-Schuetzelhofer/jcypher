/************************************************************************
 * Copyright (c) 2016 IoT-Solutions e.U.
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

package iot.jcypher.facade;

import java.io.StringWriter;
import java.util.List;

import javax.json.Json;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;

import iot.jcypher.domain.IDomainAccess;
import iot.jcypher.domain.IGenericDomainAccess;
import iot.jcypher.domain.genericmodel.DOField;
import iot.jcypher.domain.genericmodel.DOType;
import iot.jcypher.domain.genericmodel.DOType.Kind;
import iot.jcypher.domain.genericmodel.internal.DomainModel;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.query.writer.Format;
import iot.jcypher.query.writer.JSONWriter;

public class JSONDomainFacade {
	
	private IDomainAccess domainAccess;
	private Format prettyFormat;

	public JSONDomainFacade(IDomainAccess domainAccess) {
		this.domainAccess = domainAccess;
		this.prettyFormat = Format.NONE;
	}
	
	public JSONDomainFacade(IGenericDomainAccess genericDomainAccess) {
		this.domainAccess = genericDomainAccess.getDomainAccess();
		this.prettyFormat = Format.NONE;
	}

	/**
	 * Answer a JSON representation of the domain model
	 * @return
	 */
	public String getDomainModel() {
		DomainModel model = ((IIntDomainAccess)this.domainAccess).getInternalDomainAccess().getDomainModel();
		StringWriter sw = new StringWriter();
		JsonGenerator generator;
		if (this.prettyFormat != Format.NONE) {
			JsonGeneratorFactory gf = JSONWriter.getPrettyGeneratorFactory();
			generator = gf.createGenerator(sw);
		} else
			generator = Json.createGenerator(sw);
		
		generator.writeStartObject();
		generator.write("domainName", model.getDomainName());
		writeModel(model, generator);
		generator.writeEnd();

		generator.flush();
		return sw.toString();
	}
	
	/**
	 * Answer a JSON object containing the domain name
	 * @return
	 */
	public String getDomainName() {
		DomainModel model = ((IIntDomainAccess)this.domainAccess).getInternalDomainAccess().getDomainModel();
		StringWriter sw = new StringWriter();
		JsonGenerator generator;
		if (this.prettyFormat != Format.NONE) {
			JsonGeneratorFactory gf = JSONWriter.getPrettyGeneratorFactory();
			generator = gf.createGenerator(sw);
		} else
			generator = Json.createGenerator(sw);
		
		generator.writeStartObject();
		generator.write("domainName", model.getDomainName());
		generator.writeEnd();

		generator.flush();
		return sw.toString();
	}
	
	/**
	 * Set the format for creating JSON representations (i.e use of indentation and new lines),
	 * <br/>the default is 'no pretty printing'.
	 * @param prettyFormat
	 * @return
	 */
	public JSONDomainFacade setPrettyFormat(Format prettyFormat) {
		this.prettyFormat = prettyFormat;
		return this;
	}

	private void writeModel(DomainModel model, JsonGenerator generator) {
		List<DOType> doTypes = model.getDOTypes();
		generator.writeStartArray("types");
		for (DOType typ : doTypes) {
			writeType(typ, generator);
		}
		generator.writeEnd();
	}

	private void writeType(DOType typ, JsonGenerator generator) {
		generator.writeStartObject();
		generator.write("name", typ.getName());
		if (typ.getKind() != null)
			generator.write("kind", typ.getKind().name());
		if (typ.getSuperType() != null) {
			generator.writeStartArray("extends");
			generator.write(typ.getSuperType().getName());
			generator.writeEnd();
		}
		List<DOType> ifs = typ.getInterfaces();
		if (!ifs.isEmpty()) {
			if (typ.getKind() == Kind.INTERFACE)
				generator.writeStartArray("extends");
			else
				generator.writeStartArray("implements");
			for (DOType intf : ifs) {
				generator.write(intf.getName());
			}
			generator.writeEnd();
		}
		List<DOField> flds = typ.getDeclaredFields();
		if (!flds.isEmpty()) {
			generator.writeStartArray("fields");
			for (DOField f : flds) {
				writeField(f, generator);
			}
			generator.writeEnd();
		}
		generator.writeEnd();
	}

	private void writeField(DOField f, JsonGenerator generator) {
		generator.writeStartObject();
		generator.write("name", f.getName());
		generator.write("type", f.getTypeName());
		String ct = f.getComponentTypeName();
		if (ct != null) {
			generator.write("componentType", ct);
			generator.write("buildIn", DomainModel.isBuildIn(ct));
		} else
			generator.write("buildIn", f.isBuidInType());
		generator.writeEnd();
	}
}
