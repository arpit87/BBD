package com.bakarapp.HTTPClient;

import java.io.UnsupportedEncodingException;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.json.JSONException;
import org.json.JSONObject;

import com.bakarapp.HTTPServer.CreateBeepResponse;
import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.HTTPServer.ServerResponseBase;
import com.bakarapp.Users.UserAttributes;
import com.bakarapp.Util.BBDTracker;
import com.bakarapp.Util.Logger;

public class CreateBeepRequest extends HttpRequest{
	private static String RESTAPI="createBeep";
    public static final String URL = ServerConstants.SERVER_ADDRESS + ServerConstants.BEEP + "/"+RESTAPI+"/";
    private static final String TAG = "com.BhakBosdi.HttpClient.CreateBeepRequest";
    
	HttpPost httpQuery;
	JSONObject jsonobj;
	HttpClient httpclient = new DefaultHttpClient();
	CreateBeepResponse createBeepResponse;
	String jsonStr;
	HttpResponseListener mListener;
	
	public CreateBeepRequest(String beep_str,int beep_level,HttpResponseListener listener)
	{
		super(URL,RESTAPI);
		
		mListener = listener;		
		jsonobj= GetServerAuthenticatedJSON();
		httpQuery =  new HttpPost(URL);
				
		try {			
			jsonobj.put(UserAttributes.BEEP_STR, beep_str);
			jsonobj.put(UserAttributes.BEEP_LEVEL, beep_level);
			//jsonobj.put(UserAttributes.PHONE, phone);
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
	
		
		StringEntity postEntityUser = null;
		try {
			postEntityUser = new StringEntity(jsonobj.toString());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		postEntityUser.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
		Logger.i(TAG, "calling server:"+jsonobj.toString());	
		httpQuery.setEntity(postEntityUser);
	}
	
	public ServerResponseBase execute() {
			BBDTracker.sendEvent("HttpRequest",RESTAPI,"httprequest:"+RESTAPI+":execute",1L);
			try {
				response=httpclient.execute(httpQuery);
			} catch (Exception e) {
				BBDTracker.sendEvent("HttpRequest",RESTAPI,"httprequest:"+RESTAPI+":execute:executeexception",1L);
			}
			try {
				if(response==null)
					return null;
				jsonStr = responseHandler.handleResponse(response);
			} catch (Exception e) {
				BBDTracker.sendEvent("HttpRequest",RESTAPI,"httprequest:"+RESTAPI+":execute:responseexception",1L);
			}   
					
			createBeepResponse =	new CreateBeepResponse(response,jsonStr,RESTAPI);
			createBeepResponse.setReqTimeStamp(this.reqTimeStamp);
			createBeepResponse.setResponseListener(mListener);
			return createBeepResponse;		
	}
	
	

}

