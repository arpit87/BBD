package com.BhakBhosdi.Util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.BhakBhosdi.HelperClasses.Event;
import com.BhakBhosdi.HelperClasses.SBConnectivity;
import com.BhakBhosdi.HelperClasses.ThisAppConfig;
import com.BhakBhosdi.HelperClasses.ThisUserConfig;
import com.BhakBhosdi.Platform.Platform;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.GoogleAnalytics;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class BBDTracker {
	
	private static final String TAG = "com.BhakBhosdi.Util.BBDTracker";
	//common info
	public static String USERID = "uid";
	public static String APPUUID = "app_id";
	public static String APPVERSIONCODE = "app_vcode";
	public static String APPVERSIONNAME = "app_vname";
	public static String DEVICEID = "did";
	public static String IPADDRESS = "ip";	
	public static String MANUFACTURER = "manufacturer";
	public static String MODEL = "model";
	public static String FBID = "fbid";
	public static String ANDROIDVERSION = "sdk_ver";
	public static String PLATFORM = "platform";
	
	
	//inst info
	public static String MOBILECOUNTRYCODE = "mcc";  //MobileCountryCode
	public static String MOBILENETWORKCODE="mnc";
	public static String CURRLATITUDE="curr_lat";
	public static String CURRLONGITUDE="curr_longi";
	public static String TIMESTAMP="ts";		
	public static String NETWORKOPERATOR = "net_op";
	public static String NETWORKTYPE = "net_type";
	public static String NETWORKSUBTYPE = "net_subtype";
	public static String LOGINSTATE = "login";
	
	//arguments
	public static String GRPSIZE = "grp_size";
	public static String OTHERUSERID = "other_uid";
	public static String OTHERUSERFBID = "other_fbid";
	public static String APIRESPONSETIME = "res_time";
	public static String NUMMATCHES = "num_match";
	public static String FBUSERNAME = "fb_username";
	
	//default tracker
	static Tracker defaultTracker = GoogleAnalytics.getInstance(Platform.getInstance().getContext()).getDefaultTracker();
	
	
	public static void sendEvent(String category, String action, String label, Long value, Map<String,Object> args)
	{	
		//to GA
		sendEvent(category, action, label, value);
		//send to our logger
		args.put("event", label);
		String jsonString = createInstantaneousInfoJSON(args).toString();
		Event.addEvent(jsonString);
	}
	
	public static void sendEvent(String category, String action, String label, Long value)
	{
		Logger.i(TAG, "get event:"+label);		
		Map<String,Object> args = new HashMap<String, Object>();
		args.put("event", label);
		String jsonString = createInstantaneousInfoJSON(args).toString();
		Event.addEvent(jsonString);		
		defaultTracker.send(MapBuilder.createEvent(category,action,label,value).build());
	}
	
	public static void sendView(String viewString)
	{
		HashMap<String, String> hitParameters = new HashMap<String, String>();
		hitParameters.put(Fields.SCREEN_NAME, viewString);
		defaultTracker.send(hitParameters);
	}
	
	public static JSONObject createCommonInfoJSON()
	{
		JSONObject json = new JSONObject();
		//common info val
		  String USERIDVAL = ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID);
		  String APPUUIDVAL = ThisAppConfig.getInstance().getString(ThisAppConfig.APPUUID);
		  int APPVERSIONCODEVAL = Platform.getInstance().getThisAppVersion();
		  String APPVERSIONNAMEVAL = Platform.getInstance().getThisAppVersionName();
		  String DEVICEIDVAL = ThisAppConfig.getInstance().getString(ThisAppConfig.DEVICEID);	
		  String manufacturer = Build.MANUFACTURER;
		  String model = Build.MODEL;
		  int sdk = Build.VERSION.SDK_INT;
		try {
			json.put(PLATFORM, "Android");
			json.put(USERID, USERIDVAL);			
			json.put(APPUUID, APPUUIDVAL);
			json.put(APPVERSIONCODE, APPVERSIONCODEVAL);
			json.put(APPVERSIONNAME, APPVERSIONNAMEVAL);
			json.put(DEVICEID, DEVICEIDVAL);
			json.put(MANUFACTURER, manufacturer);
			json.put(MODEL, model);
			json.put(ANDROIDVERSION, sdk);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return json;
		
	}
	
	private static JSONObject createInstantaneousInfoJSON(Map<String,Object> args)
	{
		JSONObject json = new JSONObject();
		String IPADDRESSESVAL = SBConnectivity.getipAddress();		 
		TelephonyManager telephonyManager = (TelephonyManager)Platform.getInstance().getContext().getSystemService(Context.TELEPHONY_SERVICE);
		long unixTime = System.currentTimeMillis();		
		String networkOperator = telephonyManager.getNetworkOperator();
		String mcc = "";
	    String mnc = "";
		if(networkOperator!= null && networkOperator.length() > 3)
		{
	     mcc = networkOperator.substring(0, 3);
	     mnc = networkOperator.substring(3);
		}
	    double lat = 0.0;
	    double longi = 0.0;	   
	    String login_state = "none";
	    String fbid = "";
	    String operatorName = telephonyManager.getNetworkOperatorName();
	    String networkType = SBConnectivity.getNetworkType();
	    String networkSubType = SBConnectivity.getNetworkSubType();
	      
	   
	    try {
			json.put(MOBILECOUNTRYCODE, mcc);			
			json.put(MOBILENETWORKCODE, mnc);
			json.put(CURRLATITUDE, lat);
			json.put(CURRLONGITUDE, longi);
			json.put(TIMESTAMP, unixTime);
			json.put(IPADDRESS, IPADDRESSESVAL);	
			json.put(LOGINSTATE, login_state);
			json.put(FBID, fbid);
			json.put(NETWORKOPERATOR, operatorName.trim());
			json.put(NETWORKTYPE, networkType);
			json.put(NETWORKSUBTYPE, networkSubType);	
			Iterator entries = args.entrySet().iterator();
				while (entries.hasNext())
				{
					Map.Entry entry = (Map.Entry) entries.next();			    
				    json.put((String)entry.getKey(), entry.getValue());
				}
			}
			 catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    
		return json;
		
	}
	
	/*private  static String mergeJSONObjects(JSONObject[] objs)
	{
		JSONObject merged = new JSONObject();		
		for (JSONObject obj : objs) {
		    Iterator it = obj.keys();
		    while (it.hasNext()) {
		        String key = (String)it.next();
		        try {
					merged.put(key, obj.get(key));
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		    }
		}
		return merged.toString();
	}*/

}
