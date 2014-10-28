package com.BhakBhosdi.HelperClasses;

import org.json.JSONException;
import org.json.JSONObject;

import com.BhakBhosdi.Users.UserAttributes;

public class Beep {
	String beep_str = "";
	int level = -1;
	int creator_bbdid = -1;
	
	JSONObject allInfo = null;
	
	
	public Beep(JSONObject jsonObject) {
			
		
        allInfo = jsonObject;
        try {
        	beep_str = allInfo.getString(UserAttributes.BEEP_STR);
        } catch (JSONException e) {
        	
        }
        
        try {
        	level = allInfo.getInt(UserAttributes.BEEP_LEVEL);
        } catch (JSONException e) {
        	
        }
        
        try {
        	creator_bbdid = allInfo.getInt(UserAttributes.BEEP_CREATOR);
        } catch (JSONException e) {
        	
        }  
        
               
	}
	
	public String getBeepStr() {
		return beep_str;
	}


	public int getBeepLevel() {
		return level;
	}
	
	public int getBeepCreator() {
		return creator_bbdid;
	}
	
}


