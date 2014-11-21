package com.bakarapp.Users;

/***
 * 
 * @author arpit87
 * use this class to define any json attributes
 * that we get in response from server
 *
 */

public class UserAttributes {
	
	
	//get beep
	public static String BEEP_STR = "beep_str";
	public static String BEEP_CREATOR = "created_by";
	public static String BEEP_IMG = "beep_img";
	public static String BEEP_LEVEL ="beeplevel";
	public static String BBD_ID ="bbdid";
	public static String NUM_BEEPS = "numbeeps";
	public static String REBEEPS = "rebeeps";
	public static String LIKES = "likes";
	public static String BEEPID = "beepid";
	public static String DATE_CREATED = "date_created";
	
	//add user
	public static String NICK ="name";
	public static String PHONE = "phone";
	public static String CHATUSER = "chatuser";
	public static String CHATPASS = "chatpass";
	
	//add friend
	public static String FRIEND_BBD_ID = "friendbbdid";
	public static String FRIEND_NICK = "friendnick";
	
	//send  beep
	public static String FROM_ID = "bbdid";
	public static String TO_ID="friendbbdid";
	
	//get trends
	public static final String TREND_TYPE = "trend_type";
	public static final String TOP_NUM="top_num";
	public static final String NEW_BEEP_NUM="new_num";
	public static final String TOTAL_BEEP_NUM="total_num";
			
	
			
}
