package com.bakarapp.HelperClasses;


public class ThisUserConfig extends ConfigBase{
	

	private static ThisUserConfig instance = null;
	
	
	public static final String BBD_ID = "bbdid";
	public static final String MYNICK = "mynick";
	public static final String PHONE = "phone";
	public static final String CHATUSERID = "chatuser";
	public static final String CHATPASSWORD = "chatpass";
	public static final String LEVEL = "level";
	
		
	private ThisUserConfig(){super(Constants.USER_CONF_FILE);}
	
	public static ThisUserConfig getInstance()
	{
		if(instance == null)
			instance = new ThisUserConfig();
		return instance;
		
	}
}
