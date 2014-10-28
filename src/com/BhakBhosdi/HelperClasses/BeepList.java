package com.BhakBhosdi.HelperClasses;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BeepList {
 
static BeepList instance = null;
List <Beep> beepList ;

private BeepList()
{}

public static BeepList getInstance()
{ 
	if (instance == null)
	 instance = new BeepList();
	return instance;
	
}

public List<Beep> getBeepList()
{
	return beepList;
}

public void updateBeepList(JSONArray beeps){
	ArrayList<Beep> beepList = new ArrayList<Beep>();
	try {			
					
		//JSONArray beeps = beepListJSON.getJSONArray("BeepList");
		
		for(int i=0;i<beeps.length();i++)
		{
			JSONObject thisBeep=beeps.getJSONObject(i);
			//if (Platform.getInstance().isLoggingEnabled()) Log.d("json",thisOtherUser.toString());
			Beep b = new Beep(thisBeep);
			beepList.add((b));				
		}
		
	} catch (JSONException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	this.beepList = beepList;
	
}

}
