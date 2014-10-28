package com.BhakBhosdi.HTTPServer;

import org.apache.http.HttpResponse;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.BhakBhosdi.HTTPClient.HttpResponseListener;
import com.BhakBhosdi.HelperClasses.ToastTracker;
import com.BhakBhosdi.Util.Logger;

public class GetBeepListResponse extends ServerResponseBase{
	
	private static final String TAG = "com.BhakBhosdi.Server.GetBeepListResponse";
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
