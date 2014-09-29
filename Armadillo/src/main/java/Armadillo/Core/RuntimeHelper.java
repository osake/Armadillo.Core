package Armadillo.Core;

import java.io.UnsupportedEncodingException;
import java.lang.management.ManagementFactory;
import java.net.URLDecoder;

import Armadillo.Core.Io.FileHelper;

public class RuntimeHelper {

	private static String m_strProcessid;

	static {
		m_strProcessid = ManagementFactory.getRuntimeMXBean().getName();
	}

	public static String getProcessId() {
		return m_strProcessid;
	}

	public static String getCurrentJarName(){
		String path = RuntimeHelper.class.getProtectionDomain().getCodeSource().getLocation().getPath();
		try {
			String decodedPath = URLDecoder.decode(path, "UTF-8");
			return FileHelper.getName(decodedPath);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return "";
	}
}
