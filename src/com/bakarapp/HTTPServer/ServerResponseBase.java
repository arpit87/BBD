package com.bakarapp.HTTPServer;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.json.JSONException;
import org.json.JSONObject;

import com.bakarapp.HTTPClient.HttpResponseListener;
import com.bakarapp.Util.BBDTracker;
import com.bakarapp.Util.Logger;

public abstract class ServerResponseBase {
	
	public enum ResponseStatus{
		HttpStatus200, // OK
		HttpStatus201, // CREATED
		HttpStatus202, // ACCEPTED
		HttpStatus302, // found
		HttpStatus401, // authentication error
		HttpStatus403, // access denied
		HttpStatus404, // not found
		HttpStatus422, // validation error
		Unknown, // unknown error
	}	
	
	JSONObject jobj;
	JSONObject body;
	JSONObject error;
	String RESTAPI = "";
	protected ResponseStatus status = ResponseStatus.Unknown;
	long reqTimeStamp =0L;
	long responseTimeStamp =System.currentTimeMillis();
	HttpResponseListener mListener;
	
	public ServerResponseBase(HttpResponse response,String jobjStr,String RESTAPI) {
		
		if(response!=null)
		switch(response.getStatusLine().getStatusCode()){
		case 200:
			status = ResponseStatus.HttpStatus200;
		break;
		case 201:
			status = ResponseStatus.HttpStatus201;
		break;
		case 202:
			status = ResponseStatus.HttpStatus202;
		break;
		case 302:
			status = ResponseStatus.HttpStatus302;	
		break;
		case 401:
			status = ResponseStatus.HttpStatus401;
		break;
		case 403:
			status = ResponseStatus.HttpStatus403;
			break;
		case 404:
			status = ResponseStatus.HttpStatus404;
			break;
		case 422:
			status = ResponseStatus.HttpStatus422;
			break;
	}
		
		try {
			if(jobjStr != null)
			{
				Logger.d("Server response string:", jobjStr);
				jobj= new JSONObject(jobjStr);
			}
			else
				jobj= new JSONObject("{\"header\":[]}");
		} catch (JSONException e) {
			try {
				//build dummy json if server doesnt return json string
				jobj= new JSONObject("{\"header\":[]}");
			} catch (JSONException e1) {				
			}
			
		}
		this.RESTAPI = RESTAPI;
	}

	public ResponseStatus getStatus()
	{
		return status;
	}
	
	public String getRESTAPI()
	{
		return RESTAPI;
	}
	
	public long getReqTimeStamp() {
		return reqTimeStamp;
	}

	public void setReqTimeStamp(long reqTimeStamp) {
		this.reqTimeStamp = reqTimeStamp;
	}

	public abstract void process();
	

	public HttpResponseListener getResponseListener() {
		return mListener;
	}

	public void setResponseListener(HttpResponseListener mListener) {
		this.mListener = mListener;
	}

	
	private long getResponseTimeMilli()
	{
		return responseTimeStamp - reqTimeStamp;
	}
	
	protected void logSuccess()
	{
		Map<String, Object> trackArgMap = new HashMap<String,Object>();
	    trackArgMap.put(BBDTracker.APIRESPONSETIME, getResponseTimeMilli());
		BBDTracker.sendEvent("ServerResponse",getRESTAPI(),"ServerResponse:"+getRESTAPI()+":success",1L,trackArgMap);
	}
	
	protected void logSuccessWithArg(String label,String value)
	{
		Map<String, Object> trackArgMap = new HashMap<String,Object>();
	    trackArgMap.put(BBDTracker.APIRESPONSETIME, getResponseTimeMilli());
	    trackArgMap.put(label, value);
		BBDTracker.sendEvent("ServerResponse",getRESTAPI(),"ServerResponse:"+getRESTAPI()+":success",1L,trackArgMap);
	}
	
	protected void logServererror()
	{
		Map<String, Object> trackArgMap = new HashMap<String,Object>();
	    trackArgMap.put(BBDTracker.APIRESPONSETIME, getResponseTimeMilli());
		BBDTracker.sendEvent("ServerResponse",getRESTAPI(),"ServerResponse:"+getRESTAPI()+":servererror",1L,trackArgMap);
	}

}
