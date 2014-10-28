package com.BhakBhosdi.Platform;

import static org.acra.ReportField.ANDROID_VERSION;
import static org.acra.ReportField.APP_VERSION_CODE;
import static org.acra.ReportField.APP_VERSION_NAME;
import static org.acra.ReportField.AVAILABLE_MEM_SIZE;
import static org.acra.ReportField.BRAND;
import static org.acra.ReportField.CUSTOM_DATA;
import static org.acra.ReportField.DISPLAY;
import static org.acra.ReportField.LOGCAT;
import static org.acra.ReportField.PACKAGE_NAME;
import static org.acra.ReportField.PHONE_MODEL;
import static org.acra.ReportField.STACK_TRACE;
import static org.acra.ReportField.TOTAL_MEM_SIZE;
import static org.acra.ReportField.USER_APP_START_DATE;
import static org.acra.ReportField.USER_CRASH_DATE;

import org.acra.ACRA;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.BhakBhosdi.Util.Logger;

@ReportsCrashes(formKey = "dDZQYXlhUldnM192YWhpdUhmTm1MLUE6MQ" ,
customReportContent = {APP_VERSION_NAME,
		APP_VERSION_CODE,  PACKAGE_NAME,PHONE_MODEL,BRAND, ANDROID_VERSION,
		TOTAL_MEM_SIZE, AVAILABLE_MEM_SIZE ,CUSTOM_DATA, STACK_TRACE,
		 DISPLAY,USER_APP_START_DATE , USER_CRASH_DATE,LOGCAT },
logcatArguments = { "-t", "100", "-v", "long", "StrangerBuddy:I", "*:D", "*:S" },
mode = ReportingInteractionMode.TOAST,
forceCloseDialogAfterToast = false, // optional, default false
resToastText = com.BhakBhosdi.R.string.crash_toast_text
) 
public class BhakBhosdi extends Application{	
	
	private static final String TAG = "com.BhakBhosdi.Platform.BhakBhosdi";
		
	@Override
	public void onCreate()
	{		
		super.onCreate();
		ACRA.init(this);
		Logger.i(TAG,"App start");
		getApplicationContext();		
		Platform.getInstance().initialize(this);
		Platform.getInstance().startChatService();
        //Platform.getInstance().startUploadEventService();
        //if (!StringUtils.isEmpty(ThisUserConfig.getInstance().getString(ThisUserConfig.USERID))) {
        //	Platform.getInstance().startGCMService();
        //}
        //else userid has not been set yet, service will be started after add user response is received.

		//we check on userid which we wipe out on fb logout. User may login as another user
		//for which we will provide different userid		
		Logger.i(TAG,"Platform initialized");
		
	}

}
