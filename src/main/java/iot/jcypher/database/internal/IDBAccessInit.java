package iot.jcypher.database.internal;

import iot.jcypher.database.IDBAccess;

import java.util.Properties;

public interface IDBAccessInit extends IDBAccess {

	public void initialize(Properties properties);
}
