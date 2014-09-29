package Armadillo.Core;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class NetworkHelper {
	
	public static String getHostName(){
		String localhostname = "";
		try {
			localhostname = java.net.InetAddress.getLocalHost().getHostName();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}	
		return localhostname;
	}

	public static String GetIpAddr(String strServerName) {
		
		try {
			InetAddress address = InetAddress.getByName(strServerName);
			return address.getHostAddress();
			
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		return "";
	}
}