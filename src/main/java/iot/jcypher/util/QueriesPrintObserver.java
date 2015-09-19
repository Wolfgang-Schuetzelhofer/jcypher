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

package iot.jcypher.util;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

public class QueriesPrintObserver {
	private static MultiOutputStream multiOutputStream = new MultiOutputStream();
	public static final PrintStream printStream = new PrintStream(multiOutputStream);
	
	private static Map<String, ContentToObserve> enabledQueries;
	
	public static ContentToObserve contentToObserve(QueryToObserve queryTitle) {
		if (enabledQueries != null)
			return enabledQueries.get(queryTitle.getTitle());
		return null;
	}
	
	public static void addToEnabledQueries(QueryToObserve queryTitle, ContentToObserve cto) {
		if (enabledQueries == null)
			enabledQueries = new HashMap<String, ContentToObserve>();
		enabledQueries.put(queryTitle.getTitle(), cto);
	}
	
	public static void removeFromEnabledQueries(QueryToObserve queryTitle) {
		if (enabledQueries != null)
			enabledQueries.remove(queryTitle.getTitle());
	}
	
	public static void removeAllEnabledQueries() {
		enabledQueries = null;
	}
	
	public static void addOutputStream(OutputStream out) {
		multiOutputStream.addDelegate(out);
	}
	
	public static void removeOutputStream(OutputStream out) {
		multiOutputStream.removeDelegate(out);
	}
	
	public static void removeAllOutputStreams() {
		multiOutputStream.removeAllDelegates();
	}
	
	/************************************/
	public enum ContentToObserve {
		CYPHER, JSON, CYPHER_JSON
	}
	
	/************************************/
	public enum QueryToObserve {
		DOMAIN_INFO("DOMAIN INFO"),
		DOM_QUERY("DOM QUERY"),
		COUNT_QUERY("COUNT QUERY");
		
		private String title;

		private QueryToObserve(String title) {
			this.title = title;
		}

		public String getTitle() {
			return title;
		}
		
	}
}
