/************************************************************************
 * Copyright (c) 2014-2016 IoT-Solutions e.U.
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

package iot.jcypher.query.writer;

import java.util.List;

public class ContextAccess {

	public static boolean useTransationalEndpoint(WriterContext context) {
		return context.useTransactionalEndpoint;
	}
	
	public static void setUseTransactionalEndpoint(boolean b, WriterContext context) {
		context.useTransactionalEndpoint = b;
	}
	
	public static List<String> getResultDataContents(WriterContext context) {
		return context.getResultDataContents();
	}
	
	public static void setResultDataContents(WriterContext context, List<String> rdc) {
		context.setResultDataContents(rdc);
	}
	
	public static WriterContext cloneContext(WriterContext ctxt) {
		WriterContext ret = new WriterContext();
		ret.cypherFormat = ctxt.cypherFormat;
		ret.extractParams = ctxt.extractParams;
		ret.useTransactionalEndpoint = ctxt.useTransactionalEndpoint;
		for (String contentType : ctxt.getResultDataContents()) {
			ret.getResultDataContents().add(contentType);
		}
		return ret;
	}
}
