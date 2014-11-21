package com.bakarapp.HTTPClient;

import org.json.JSONObject;

public abstract class HttpResponseListener {
	public boolean hasBeenCancelled = false;
	public abstract void onComplete(Object json);
	public void onCancel()
	{
		hasBeenCancelled = true;
	}
}
