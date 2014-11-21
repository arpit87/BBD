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
import com.bakarapp.HTTPClient.HttpClient;
import com.bakarapp.HTTPClient.HttpRequest;
import com.bakarapp.HTTPClient.HttpResponseListener;
import com.bakarapp.HTTPClient.SendBeepRequest;
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

public class CreateAndSendBeepResponse extends ServerResponseBase{

	String mToUser ="";
	String mFromUser="";
	
	private static final String TAG = "com.bakarapp.Server.CreateBeepResponse";
	public CreateAndSendBeepResponse(HttpResponse response,String jobjStr,String toUser,String fromUser,String api) {
		super(response,jobjStr,api);	
		mToUser = toUser;
		mFromUser = fromUser;
	}
	
	@Override
	public void process() {
		Logger.i(TAG,"processing CreateUserResponse response.status:"+this.getStatus());
		
		Logger.i(TAG,"got json "+jobj.toString());		
		try {
			body = jobj.getJSONObject("body");
			//ToastTracker.showToast("Beep created");
			int beep_ID = body.getInt(UserAttributes.BEEPID);		
			
			//send beep to server to add as sent to fren
			HttpRequest sendBeepReq= new SendBeepRequest(Integer.parseInt(mFromUser),Integer.parseInt(mToUser),beep_ID,null);
			HttpClient.getInstance().executeRequest(sendBeepReq);
			
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
