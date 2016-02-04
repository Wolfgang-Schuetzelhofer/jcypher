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

import iot.jcypher.database.IDBAccess;
import iot.jcypher.domain.DomainInformation;
import iot.jcypher.domain.genericmodel.internal.DomainModel;
import iot.jcypher.domain.internal.IIntDomainAccess;
import iot.jcypher.query.writer.Format;
import iot.jcypher.query.writer.JSONWriter;

public class JSONDBFacade {

	private IDBAccess dbAccess;
	private Format prettyFormat;
	
	public JSONDBFacade(IDBAccess dbAccess) {
		super();
		this.dbAccess = dbAccess;
		this.prettyFormat = Format.NONE;
	}
	
	/**
	 * Answer a JSON representation of the available domains in the database
	 * @return
	 */
	public String getDomains() {
		StringWriter sw = new StringWriter();
		JsonGenerator generator;
		if (this.prettyFormat != Format.NONE) {
			JsonGeneratorFactory gf = JSONWriter.getPrettyGeneratorFactory();
			generator = gf.createGenerator(sw);
		} else
			generator = Json.createGenerator(sw);
		
		generator.writeStartArray();
		List<String> doms = DomainInformation.availableDomains(dbAccess);
		for (String dom : doms) {
			generator.write(dom);
		}
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
	public JSONDBFacade setPrettyFormat(Format prettyFormat) {
		this.prettyFormat = prettyFormat;
		return this;
	}
}
