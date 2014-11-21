package com.bakarapp.HelperClasses;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.util.InetAddressUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Util.Logger;

public class SBConnectivity {
    final static Context context = Platform.getInstance().getContext();
    private static String TAG = "com.bakarapp.HelperClasses.SBConnectivity";
    public static boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Logger.d("SBConnectivity", "ConnectivityManager:"+cm.toString());
        NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null && ni.isConnected();
    }
    
    public static boolean isWifi() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		Logger.d("SBConnectivity", "ConnectivityManager:"+cm.toString());
        NetworkInfo ni = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        return ni != null && ni.isConnected();
    }

    public static boolean isConnected() {
        try {
        	if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "chking data available by http timeout");
            HttpGet request = new HttpGet(ServerConstants.SERVER_ADDRESS);
            HttpParams httpParameters = new BasicHttpParams();
            int timeoutConnection = 10000; //timeout of 10 seconds
            HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);
            int timeoutSocket = 10000; //socket timeout of 10 seconds
            HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
            DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
            request.addHeader("Content-Type", "application/json");
            HttpResponse response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();

            if (entity != null) {
            	if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "yes data available by http timeout");
                return true;
            }
            //Log.d("com.bakarapp.HelperClasses.SBConnectivity", "No response");
            if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "no data available by http timeout");
            return false;
        } catch (Exception e) {
            //Log.e("com.bakarapp.HelperClasses.SBConnectivity", e.getMessage());
            return false;
        }

    }
    
    public static String getNetworkType()
   	{
    	ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    	NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
    	if(activeNetwork!=null)
    	switch (activeNetwork.getType()) {
    	  case (ConnectivityManager.TYPE_WIFI):
    		  return "Wifi";
    	  case (ConnectivityManager.TYPE_MOBILE):
    		  return "Mobile";
    	  default:
    		  return "";
    	}
    	return "";
   	}
    
    public static String getNetworkSubType()
	{
    	TelephonyManager tm = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = tm.getNetworkType();		
		switch (networkType)
		{
		case 7:
		    return "1xRTT";		          
		case 4:
			return"CDMA";		         
		case 2:
			return "EDGE";		      
		case 14:
			return "eHRPD";		         
		case 5:
			return "EVDO rev. 0";		      
		case 6:
			return "EVDO rev. A";		   
		case 12:
			return "EVDO rev. B";		      
		case 1:
			return "GPRS";		          
		case 8:
			return "HSDPA";		        
		case 10:
			return "HSPA";		            
		case 15:
			return "HSPA+";		             
		case 9:
			return "HSUPA";		              
		case 11:
			return "iDen" ;		    
		case 13:
			return "LTE";		    
		case 3:
			return "UMTS";		              
		default:
			return "";
		   
		}
		
	}    
   
    public static String getipAddress() { 
    	Logger.i(TAG,"getting ip address");
    	String ipaddress = "";
        try {
            for (Enumeration en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = (NetworkInterface) en.nextElement();
                for (Enumeration enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = (InetAddress) enumIpAddr.nextElement();                   
                    if (!inetAddress.isLoopbackAddress() && InetAddressUtils.isIPv4Address(ipaddress = inetAddress.getHostAddress())) {                     	
                        Logger.i(TAG,"ip address:"+ipaddress.toString());  
                        return ipaddress.toString();
                    }
                }
            }
        } catch (SocketException ex) {
            Logger.e(TAG,"Socket exception in GetIP Address of Utilities:"+ex.toString());
        }
        return ipaddress; 
}  
      
    
}
