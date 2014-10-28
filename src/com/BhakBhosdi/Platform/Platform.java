package com.BhakBhosdi.Platform;


import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;

import com.BhakBhosdi.HTTPClient.HttpClient;
import com.BhakBhosdi.Users.ThisUser;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;



public class Platform {
	
	private final static String TAG = "com.BhakBhosdi.Platform.Platform";
	private static Platform instance = null;
	private Context context;	
	private Handler handler;
	private boolean ENABLE_LOGGING = true;
	
		
	private Platform() {
	}
	
	public static Platform getInstance()
	{
		if(instance == null)
			instance = new Platform();
		return instance;
	}
	
	public boolean isLoggingEnabled() {
		return ENABLE_LOGGING;
	}	

	public Context getContext(){
		return context;
	}	
	
	public Handler getHandler(){
		return handler;
	}
	
	public void initialize(Context context) {
		this.context= context;			
		HttpClient.getInstance();
		handler = new Handler();		
		EasyTracker tracker = EasyTracker.getInstance(context);
		GoogleAnalytics.getInstance(context).setDefaultTracker(tracker);
		ENABLE_LOGGING = (0 != (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE));
		ThisUser.getInstance();
	}
	
	public int getThisAppVersion()
	{
	 try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
	}	
	
	public String getThisAppVersionName()
	{
	 try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
	}
	
	public void startChatService(){
	     
        Intent i = new Intent("com.BhakBhosdi.ChatService.ChatService");                  
        if (Platform.getInstance().isLoggingEnabled()) Log.d( TAG, "Service starting" );
        context.startService(i);
       
       }
            
	
	 public void stopChatService() {		
	          Intent i = new Intent("com.BhakBhosdi.ChatService.ChatService");
	          context.stopService(i);       
	          
	          if (Platform.getInstance().isLoggingEnabled()) Log.d( TAG, "Service stopped" );	         
	             
    }
}

