package com.bakarapp.HTTPClient;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import com.bakarapp.HTTPServer.GetBeepListResponse;
import com.bakarapp.HTTPServer.GetMyBeepListResponse;
import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.HTTPServer.ServerResponseBase;
import com.bakarapp.Users.UserAttributes;
import com.bakarapp.Util.BBDTracker;
import com.bakarapp.Util.Logger;

public class GetMyBeepListRequest extends HttpRequest{

	private String TAG = "com.bakarapp.HttpClient.getMyBeepListRequest";
    private static String RESTAPI="getMyBeepList";
    public static final String URL = ServerConstants.SERVER_ADDRESS + ServerConstants.BEEP + "/"+RESTAPI+"/";

	HttpGet httpQueryGetBeepList;	
	JSONObject jsonobjBeepList;
	HttpClient httpclient = new DefaultHttpClient();
	GetMyBeepListResponse getBeepResponse;
	String jsonStr;
	HttpResponseListener mListener;
	public GetMyBeepListRequest(int numbeeps, HttpResponseListener listener)
	{		
		super(URL,RESTAPI);
		queryMethod = QueryMethod.Get;		
		mListener = listener;
		
		List<NameValuePair> params = GetServerAuthenticatedParams();
		
		params.add( new BasicNameValuePair( UserAttributes.NUM_BEEPS, Integer.toString(numbeeps) ) );	
		
	
		URI uri = null;
		try {
			uri = new URI( URL + "?" + URLEncodedUtils.format( params, "utf-8" ));
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Logger.d(TAG,"getting beeps:"+uri.toString());
		//prepare getnearby request		
	    httpQueryGetBeepList = new HttpGet(uri);
						
		}
	
	public ServerResponseBase execute() {
		BBDTracker.sendEvent("HttpRequest",RESTAPI,"httprequest:"+RESTAPI+":execute",1L);
			try {
				response=httpclient.execute(httpQueryGetBeepList);
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
			
			getBeepResponse =	new GetMyBeepListResponse(response,jsonStr,RESTAPI);
			getBeepResponse.setReqTimeStamp(this.reqTimeStamp);
			getBeepResponse.setResponseListener(mListener);
			return getBeepResponse;
		
	}
	
	

}
