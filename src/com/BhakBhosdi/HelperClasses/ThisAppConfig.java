package com.BhakBhosdi.HelperClasses;

public class ThisAppConfig extends ConfigBase{
	
	private static ThisAppConfig instance = null;

	public static final String APPUUID = "uuid";	
	
	//app settings

    public static final String PROPERTY_REG_ID = "registration_id";
    public static final String PROPERTY_APP_VERSION = "appVersion";
    public static final String PROPERTY_ON_SERVER_EXPIRATION_TIME ="onServerExpirationTimeMs";   
    public static final String DEVICEID = "device_id";
    public static final String REFERRER_STRING = "referrer_string";
    public static final String NOTIFICATION_SETTINGS = "not_setting";
    public static final String SOUND_SETTINGS = "sound_setting";
	
	
	
	private ThisAppConfig(){super(Constants.APP_CONF_FILE);}
	
	public static ThisAppConfig getInstance()
	{
		if(instance == null)
			instance = new ThisAppConfig();
		return instance;
		
	}
}
