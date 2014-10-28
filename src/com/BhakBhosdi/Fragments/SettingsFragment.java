package com.BhakBhosdi.Fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.BhakBhosdi.R;
import com.BhakBhosdi.HelperClasses.ThisAppConfig;

public class SettingsFragment extends Fragment{
	
	 //private static final String TAG = "in.co.hopin.Fragments.SelfAboutMeFrag";
	 TextView notificationSettingsValue;
	 TextView soundSettingsValue;
	 ViewGroup notificationViewGroup;
	 ViewGroup soundViewGroup;
	 ViewGroup blockedUserViewGroup;
	 
	 
	 @Override
	    public void onCreate(Bundle savedInstanceState){
	        super.onCreate(savedInstanceState);	      
	 }
	 	 
	 @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		ViewGroup settingsView = (ViewGroup) inflater.inflate(R.layout.settings_layout, null);
	    
		notificationViewGroup = (ViewGroup) settingsView.findViewById(R.id.settings_notification_layout);
        soundViewGroup = (ViewGroup) settingsView.findViewById(R.id.settings_sounds_layout);
        soundViewGroup = (ViewGroup) settingsView.findViewById(R.id.settings_blockedusers_layout);
		
        notificationSettingsValue = (TextView) settingsView.findViewById(R.id.settings_notification_value);
        soundSettingsValue = (TextView) settingsView.findViewById(R.id.settings_sounds_value);
               
        
        if(ThisAppConfig.getInstance().getInt(ThisAppConfig.NOTIFICATION_SETTINGS)==0)
        	notificationSettingsValue.setText("Off");
        
        if(ThisAppConfig.getInstance().getInt(ThisAppConfig.SOUND_SETTINGS)==0)
        	soundSettingsValue.setText("Off");
        
        notificationViewGroup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(ThisAppConfig.getInstance().getInt(ThisAppConfig.NOTIFICATION_SETTINGS)==0)
				{
					ThisAppConfig.getInstance().putInt(ThisAppConfig.NOTIFICATION_SETTINGS, 1);
		        	notificationSettingsValue.setText("On");
				} else if(ThisAppConfig.getInstance().getInt(ThisAppConfig.NOTIFICATION_SETTINGS)==1)
				{
					ThisAppConfig.getInstance().putInt(ThisAppConfig.NOTIFICATION_SETTINGS, 0);
		        	notificationSettingsValue.setText("Off");
				} 
				
			}
		});
        
        soundViewGroup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				if(ThisAppConfig.getInstance().getInt(ThisAppConfig.SOUND_SETTINGS)==0)
				{
					ThisAppConfig.getInstance().putInt(ThisAppConfig.SOUND_SETTINGS, 1);
		        	notificationSettingsValue.setText("On");
				} else if(ThisAppConfig.getInstance().getInt(ThisAppConfig.SOUND_SETTINGS)==1)
				{
					ThisAppConfig.getInstance().putInt(ThisAppConfig.SOUND_SETTINGS, 0);
		        	notificationSettingsValue.setText("Off");
				} 
				
			}
		});
        
        return settingsView;
		}
		
}
