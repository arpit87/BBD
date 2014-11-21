package com.bakarapp.HelperClasses;

import java.util.Random;

import org.json.JSONException;
import org.json.JSONObject;

import com.bakarapp.Users.UserAttributes;

public class Beep {
	String beep_str = "";
	int level = -1;
	int creator_bbdid = -1;
	String beep_img = null;
	int rebeeps = 0 ;
	int likes = 0;
	int beep_id =1;
	String date_created = "";
	
	JSONObject allInfo = null;
	
	public Beep(){}
	
	public Beep(JSONObject jsonObject) {
			
		
        allInfo = jsonObject;
        try {
        	beep_str = allInfo.getString(UserAttributes.BEEP_STR);
        } catch (JSONException e) {
        	
        }
        
        try {
        	date_created = allInfo.getString(UserAttributes.DATE_CREATED);
        } catch (JSONException e) {
        	
        }
        
        try {
        	level = allInfo.getInt(UserAttributes.BEEP_LEVEL);
        } catch (JSONException e) {
        	
        }
        
        try {
        	rebeeps = allInfo.getInt(UserAttributes.REBEEPS);
        } catch (JSONException e) {
        	
        }
        
        try {
        	beep_id = allInfo.getInt(UserAttributes.BEEPID);
        } catch (JSONException e) {
        	
        }
        
        try {
        	likes = allInfo.getInt(UserAttributes.LIKES);
        } catch (JSONException e) {
        	
        }
        
        try {
        	creator_bbdid = allInfo.getInt(UserAttributes.BEEP_CREATOR);
        } catch (JSONException e) {
        	
        }  
        
        try {
        	beep_img = allInfo.getString(UserAttributes.BEEP_IMG);
        } catch (JSONException e) {        	
        } 
        
               
	}
	
	public String getBeepStr() {
		return beep_str;
	}
	
	public String getBeepImg() {
		if(beep_img == null)
		{
			Random rand = new Random();
	    	int  n = rand.nextInt(53) + 1;	    	
	    	beep_img = "trollface" + Integer.toString(n) +".png";
		}
		return beep_img;
	}


	public int getBeepLevel() {
		return level;
	}
	
	public int getBeepCreator() {
		return creator_bbdid;
	}

	public void setBeep_str(String beep_str) {
		this.beep_str = beep_str;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public void setCreator_bbdid(int creator_bbdid) {
		this.creator_bbdid = creator_bbdid;
	}

	public void setBeep_img(String beep_img) {
		this.beep_img = beep_img;
	}

	public int getRebeeps() {
		return rebeeps;
	}

	public void setRebeeps(int rebeeps) {
		this.rebeeps = rebeeps;
	}

	public int getLikes() {
		return likes;
	}

	public void setLikes(int likes) {
		this.likes = likes;
	}

	
	public void setBeep_id(int beep_id) {
		this.beep_id = beep_id;
	}

	public int getBeep_id() {
		// TODO Auto-generated method stub
		return beep_id;
	}


}


