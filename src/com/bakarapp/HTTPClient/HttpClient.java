package com.bakarapp.HTTPClient;


import android.os.AsyncTask;
import android.util.Log;

import com.bakarapp.HTTPServer.ServerResponseBase;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Util.BBDTracker;
import com.bakarapp.Util.Logger;

//Singleton class
//this class will have a thread pool to its disposal
//??all threads will be async???
//??if we want a synced job then we can customize the request ???
public class HttpClient {
	
	private static final String TAG = "com.bakarapp.HttpClient.SBHttpClient";
	private static HttpClient uniqueClient;
	private HttpClient(){};
	private ServerResponseBase response = null;
	public static HttpClient getInstance()
	{		
		if(uniqueClient == null)
			uniqueClient = new HttpClient();
		return uniqueClient; 		
	}
	
	public void executeRequest(HttpRequest... request)
	{
		//currently allowing max 3 sync requests
		int count = request.length;
		if (count<=3)
			new NewAsyncTask().execute(request);
		else {
			if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Max 3 http request per thread allowed");
        }
	}	
	
	private class NewAsyncTask extends AsyncTask <HttpRequest, Void, ServerResponseBase>
	{		
		protected ServerResponseBase doInBackground(HttpRequest... request) {
			 int count = request.length;	         
	         for(int i=0;i<count;i++)
	         {
	        	 Logger.d(TAG, "sending req:"+request[i].GetQueryURL());
	        	 response =  request[i].execute();
	        	 if(response==null )	 
	        	 {
	        		 BBDTracker.sendEvent("HttpRequest",request[i].GetAPI(),"httprequest:"+request[i].GetAPI()+":execute:responsenull",1L);
	        		 return null;
	        	 }
	         }
	         return response;
		}
		
		protected void onPostExecute(ServerResponseBase response) {
			if(response!=null)
				response.process();
		}

				
	}

}
