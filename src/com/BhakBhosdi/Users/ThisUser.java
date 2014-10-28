package com.BhakBhosdi.Users;

import java.util.LinkedList;

import android.util.Log;

import com.BhakBhosdi.HelperClasses.ThisUserConfig;
import com.BhakBhosdi.Platform.Platform;
import com.BhakBhosdi.Util.StringUtils;
/***
 * This class has latest data to set all current req data of this user
 * any activity to be updated like list map picks from this location
 * @author arpit87
 *
 */
public class ThisUser {
	
		
	private static final String TAG = "in.co.hopin.Users.ThisUserNew";
	private static ThisUser instance = null;
	private int beepCutoff = 0;
	//private LinkedList<HistoryAdapter.HistoryItem> historyItemList = new LinkedList<HistoryAdapter.HistoryItem>();
	private String bbdID;
	private String phoneNumber;
	private int level;
	private int offset;
	
	private ThisUser()
	{
		bbdID = ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID);
		level = ThisUserConfig.getInstance().getInt(ThisUserConfig.LEVEL);
	}
				
    public void reset(){
    	beepCutoff = 0;        
       }

	public void setUserID(String userID) {
		if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"set user id");
		this.bbdID = userID;
	}
	
	public String getUserID() {
		if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"get user id"+this.bbdID);
		return this.bbdID;
	}
	public int getBeepCutoff() {
		return beepCutoff;
	}

	public void setBeepCutoff(int beepCutoff) {
		this.beepCutoff = beepCutoff;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public static ThisUser getInstance() {
		if(instance == null)
			instance = new ThisUser();
		 return instance;
	}
	
	public static void clearAllData()
    {
    	instance = new ThisUser();
    }	

}
