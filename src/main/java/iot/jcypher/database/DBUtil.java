package iot.jcypher.database;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

public class DBUtil {

	public static String getStacktrace(Throwable exception) {
		ByteArrayOutputStream bo = new ByteArrayOutputStream();
		PrintStream ps = new PrintStream(bo);
		exception.printStackTrace(ps);
		ps.flush();
		String ret;
		try {
			ret = bo.toString("UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
		ps.close();
		return ret;
	}
}
