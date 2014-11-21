package com.bakarapp.HTTPServer;

import org.apache.http.HttpResponse;
import org.json.JSONException;

import android.content.Intent;
import android.util.Log;

import com.bakarapp.R;
import com.bakarapp.Activities.BhakBhosdiActivity;
import com.bakarapp.ChatClient.ChatMessage;
import com.bakarapp.ChatService.ChatService;
import com.bakarapp.ChatService.Message;
import com.bakarapp.HTTPClient.HttpResponseListener;
import com.bakarapp.HelperClasses.ActiveChat;
import com.bakarapp.HelperClasses.ChatHistory;
import com.bakarapp.HelperClasses.ProgressHandler;
import com.bakarapp.HelperClasses.ThisUserConfig;
import com.bakarapp.HelperClasses.ToastTracker;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Users.ThisUser;
import com.bakarapp.Users.UserAttributes;
import com.bakarapp.Util.Logger;
import com.bakarapp.Util.StringUtils;

public class SendBeepResponse extends ServerResponseBase{

		
	private static final String TAG = "com.bakarapp.Server.CreateBeepResponse";
	public SendBeepResponse(HttpResponse response,String jobjStr,String api) {
		super(response,jobjStr,api);		
	}
	
	@Override
	public void process() {
		Logger.i(TAG,"processing CreateUserResponse response.status:"+this.getStatus());
		
		Logger.i(TAG,"got json "+jobj.toString());		
		try {
			body = jobj.getJSONObject("body");
			//ToastTracker.showToast("Beep sent");
						
			HttpResponseListener listener = getResponseListener();
			if(listener!=null)
				listener.onComplete(body);
			
			logSuccess();
		} catch (JSONException e) {
			logServererror();
			if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Error returned by server on user add");
			ProgressHandler.dismissDialoge();
			ToastTracker.showToast("Unable to communicate to server,try again later");
			e.printStackTrace();
		}
		
	}
		
   }
