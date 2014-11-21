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

import com.bakarapp.HTTPServer.SendBeepResponse;
import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.HTTPServer.ServerResponseBase;
import com.bakarapp.Users.UserAttributes;
import com.bakarapp.Util.BBDTracker;
import com.bakarapp.Util.Logger;

public class SendBeepRequest extends HttpRequest{
	private static String RESTAPI="sendBeep";
    public static final String URL = ServerConstants.SERVER_ADDRESS + ServerConstants.BEEP + "/"+RESTAPI+"/";
    private static final String TAG = "com.BhakBosdi.HttpClient.CreateBeepRequest";
    
	HttpPost httpQuery;
	JSONObject jsonobj;
	HttpClient httpclient = new DefaultHttpClient();
	SendBeepResponse createBeepResponse;
	String jsonStr;
	HttpResponseListener mListener;
	
	public SendBeepRequest(int from,int to,int beepid,HttpResponseListener listener)
	{
		super(URL,RESTAPI);
		
		mListener = listener;		
		jsonobj= GetServerAuthenticatedJSON();
		httpQuery =  new HttpPost(URL);
				
		try {			
			jsonobj.put(UserAttributes.BEEPID, beepid);
			jsonobj.put(UserAttributes.FROM_ID, from);
			jsonobj.put(UserAttributes.TO_ID, to);
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
					
			createBeepResponse =	new SendBeepResponse(response,jsonStr,RESTAPI);
			createBeepResponse.setReqTimeStamp(this.reqTimeStamp);
			createBeepResponse.setResponseListener(mListener);
			return createBeepResponse;		
	}
	
	

}

