package com.BhakBhosdi.HTTPClient;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ResponseHandler;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import com.BhakBhosdi.HTTPServer.ServerResponseBase;
import com.BhakBhosdi.HelperClasses.ThisAppConfig;
import com.BhakBhosdi.HelperClasses.ThisUserConfig;

public abstract class HttpRequest {
	
	
	public enum QueryMethod {
		Get,
		Post,
		Put,
		Delete
	}
	
	QueryMethod queryMethod = null;
	String URLStr = "";
	String API = "";
	HttpResponse response = null;
	long reqTimeStamp = System.currentTimeMillis();
	// Create a response handler
    ResponseHandler<String> responseHandler = new BasicResponseHandler();
    	
	public abstract ServerResponseBase execute();	
	
	public HttpRequest(String URL,String RESTAPI)
	{
		URLStr = URL;
		API = RESTAPI;
	}
	
	public String GetQueryURL()
	{
		return URLStr;
	}
	
	public String GetAPI()
	{
		return API;
	}
	
	//do not add this to initial add user request
	public JSONObject GetServerAuthenticatedJSON()
	{
		JSONObject jObj = new JSONObject();
		try {
			jObj.put(ThisAppConfig.APPUUID, ThisAppConfig.getInstance().getString(ThisAppConfig.APPUUID));
			jObj.put(ThisUserConfig.BBD_ID, ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID));
		} catch (JSONException e) {			
			e.printStackTrace();
		}		
		return jObj;
	}
	
	public List<NameValuePair> GetServerAuthenticatedParams()
	{
		List<NameValuePair> params = new ArrayList<NameValuePair>();
		params.add( new BasicNameValuePair( ThisAppConfig.APPUUID, ThisAppConfig.getInstance().getString(ThisAppConfig.APPUUID)) );
		params.add( new BasicNameValuePair( ThisUserConfig.BBD_ID , ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID) ) );		
		return params;
	}
}
