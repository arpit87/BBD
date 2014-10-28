package com.BhakBhosdi.HTTPServer;

public class ServerConstants {
	
	//public static  String  SERVER_ADDRESS = "http://192.168.1.4/hopon/";
	//public static  String  SERVER_ADDRESS = "http://www.greenyatra.org/sb/";
	public static  String  SERVER_ADDRESS = "http://bhakbhosdi.com";
	public static String USER="/User";
	public static String BEEP="/Beep";
	public static String TRENDS="/Trends";	
	public static String CHATSERVERIP= "bhakbhosdi.com";
	public static String CHATADMINACKFROM = "hopin_server_ack";
	
	
	public static String AppendServerIPToFBID(String fbid)
	{
		return fbid+"@"+CHATSERVERIP;
	}


}
