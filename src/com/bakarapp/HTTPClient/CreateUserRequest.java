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

import com.bakarapp.HTTPServer.CreateUserResponse;
import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.HTTPServer.ServerResponseBase;
import com.bakarapp.HelperClasses.ThisAppConfig;
import com.bakarapp.Users.UserAttributes;
import com.bakarapp.Util.BBDTracker;
import com.bakarapp.Util.Logger;

public class CreateUserRequest extends HttpRequest{
	private static String RESTAPI="createUser";
    public static final String URL = ServerConstants.SERVER_ADDRESS + ServerConstants.USER + "/"+RESTAPI+"/";
    private static final String TAG = "com.BhakBosdi.HttpClient.CreateUserRequest";
    
	HttpPost httpQuery;
	JSONObject jsonobj;	
	String uuid;
	HttpClient httpclient = new DefaultHttpClient();
	CreateUserResponse addUserResponse;
	String jsonStr;
	HttpResponseListener mListener;
	
	public CreateUserRequest(String uuid, String phone , String username,HttpResponseListener listener)
	{
		super(URL,RESTAPI);
		this.uuid=uuid;				
		mListener = listener;		
		jsonobj=new JSONObject();
		httpQuery =  new HttpPost(URL);
		
		try {
			jsonobj.put(ThisAppConfig.APPUUID, uuid);
			jsonobj.put(UserAttributes.NICK, username);
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
					
			addUserResponse =	new CreateUserResponse(response,jsonStr,RESTAPI);
			addUserResponse.setReqTimeStamp(this.reqTimeStamp);
			addUserResponse.setResponseListener(mListener);
			return addUserResponse;
		
	}
	
	

}

