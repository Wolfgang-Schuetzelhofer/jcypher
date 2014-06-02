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

package iot.jcypher.database;

import java.lang.reflect.Method;
import java.util.Properties;

public class DBAccessFactory {

	@SuppressWarnings("unchecked")
	public static IDBAccess createRemoteDBAccess(Properties properties) {
		IDBAccess dbAccess;
		try {
			Class<? extends IDBAccess> dbAccessClass =
					(Class<? extends IDBAccess>) Class.forName("iot.jcypher.database.remote.RemoteDBAccess");
			Method init = dbAccessClass.getDeclaredMethod("initialize", new Class[] {Properties.class});
			dbAccess = dbAccessClass.newInstance();
			init.invoke(dbAccess, new Object[] {properties});
		} catch (Throwable e) {
			throw new RuntimeException(e);
		}
		return dbAccess;
	}
	
	public static IDBAccess createEmbeddedDBAccess(Properties properties) {
		return null;
	}
}
