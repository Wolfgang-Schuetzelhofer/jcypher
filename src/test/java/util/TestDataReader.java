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

package util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.HashMap;
import java.util.Map;

public class TestDataReader {
	
	public static final String TEST_IGNORE_LINE = "_IGNORE_";
	private static final String TEST_START = "----";
	private static final String COMMENT = "#";
	private static final String TEST_END = "--------------------";
	
	private static final int STATE_START = 0;
	private static final int STATE_IN_DATA = 1;
	
	private Map<String, String> testData;
	
	public TestDataReader(String testFile) {
		InputStream inStream = this.getClass().getResourceAsStream(testFile);
		InputStreamReader in = new InputStreamReader(inStream);
		load(new LineNumberReader(in));
	}
	
	private void load(LineNumberReader lineNumberReader) {
		this.testData = new HashMap<String, String>();
		String[] td;
		try {
			while((td = readNextTestData(lineNumberReader)) != null) {
				this.testData.put(td[0], td[1]);
			}
		} catch (Throwable e) {
			throw new RuntimeException(e);
		} finally {
			try {
				lineNumberReader.close();
			} catch (IOException e) {
			}
		}
	}
	
	private String[] readNextTestData(LineNumberReader lineNumberReader) throws IOException {
		String[] ret = new String[2];
		StringBuilder testDat = null;
		String line;
		int state = STATE_START;
		while ((line = lineNumberReader.readLine()) != null) {
			if (line.trim().startsWith(COMMENT))
				continue;
			if (state == STATE_START) {
				if (line.startsWith(TEST_START)) {
					ret[0] = line.substring(TEST_START.length()).trim();
					if (ret[0].indexOf(':') == ret[0].length() - 1)
						ret[0] = ret[0].substring(0, ret[0].length() - 1);
					state = STATE_IN_DATA;
				}
			} else if (state == STATE_IN_DATA) {
				if (line.startsWith(TEST_END)) {
					if (testDat != null)
						ret[1] = testDat.toString();
					return ret;
				}
				if (testDat == null)
					testDat = new StringBuilder();
				else
					testDat.append("\n");
				testDat.append(line);
			}
		}
		return null;
	}

	public String getTestData(String testId) {
		return this.testData.get(testId);
	}
	
	public static String trimComments(String toTrim) {
		try {
			String line;
			StringBuilder sb = null;
			ByteArrayInputStream inStream = new ByteArrayInputStream(toTrim.getBytes());
			InputStreamReader in = new InputStreamReader(inStream);
			LineNumberReader lineNumberReader = new LineNumberReader(in);
			while ((line = lineNumberReader.readLine()) != null) {
				if (line.trim().startsWith(COMMENT))
					continue;
				if (sb == null)
					sb = new StringBuilder();
				else
					sb.append("\n");
				sb.append(line);
			}
			return sb.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
