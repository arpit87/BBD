package com.bakarapp.HTTPServer;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.bakarapp.HTTPClient.HttpResponseListener;
import com.bakarapp.HelperClasses.ToastTracker;
import com.bakarapp.Util.Logger;

public class GetBeepListResponse extends ServerResponseBase{
	
	private static final String TAG = "com.bakarapp.Server.GetBeepListResponse";
	public GetBeepListResponse(HttpResponse response,String jobjStr,String api) {
		super(response,jobjStr,api);
	}
	
	@Override
	public void process() {
		Logger.i(TAG,"processing getBeepListResponse");
		Logger.i(TAG,"server response:"+jobj.toString());
		try {
			body = jobj.getJSONObject("body");		
			JSONArray beepJson = body.getJSONArray("BeepList");			
			HttpResponseListener listener = getResponseListener();
			if(listener!=null)
				listener.onComplete(beepJson);	
			else
				Logger.i(TAG, "beep listener null");
			logSuccess();
		} catch (JSONException e) {
			logServererror();
			ToastTracker.showToast("Some error occured in getting beeps");			
		}
	}
	
		
   }
