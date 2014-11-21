package com.bakarapp.Fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.bakarapp.R;
import com.bakarapp.HelperClasses.ThisAppConfig;
import com.bakarapp.HelperClasses.ThisUserConfig;
import com.bakarapp.HelperClasses.ToastTracker;
import com.bakarapp.Util.StringUtils;

public class SettingsFragment extends Fragment{
	
	 //private static final String TAG = "in.co.hopin.Fragments.SelfAboutMeFrag";
	 TextView notificationSettingsValue;
	 TextView soundSettingsValue;
	 TextView levelValue;
	 TextView bbaPin;
	 ViewGroup notificationViewGroup;
	 ViewGroup soundViewGroup;
	 //ViewGroup blockedUserViewGroup;
	 ViewGroup levelViewGroup;
	 ImageButton shareBBA ;
	 String BBAPin = ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID);
	// Strings to Show In Dialog with Radio Buttons
	final CharSequence[] levelitems = {" Kid "," Teen "," GrownUp ","Bindass","Dabang"};
	 
	 
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
       // blockedUserViewGroup = (ViewGroup) settingsView.findViewById(R.id.settings_blockedusers_layout);
        levelViewGroup = (ViewGroup) settingsView.findViewById(R.id.settings_level_layout);
        shareBBA = (ImageButton) settingsView.findViewById(R.id.settings_sharetbbapin);
		
        notificationSettingsValue = (TextView) settingsView.findViewById(R.id.settings_notification_value);
        soundSettingsValue = (TextView) settingsView.findViewById(R.id.settings_sounds_value);
        levelValue = (TextView) settingsView.findViewById(R.id.settings_level_value);
        bbaPin = (TextView) settingsView.findViewById(R.id.settings_bba_pin);
        
        
        bbaPin.setText(StringUtils.getSpannedText("BBA Pin", BBAPin));
        
        levelValue.setText(levelitems[ThisUserConfig.getInstance().getInt(ThisUserConfig.LEVEL)]);
        
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
		        	ToastTracker.showToast("Notifications On");
				} else if(ThisAppConfig.getInstance().getInt(ThisAppConfig.NOTIFICATION_SETTINGS)==1)
				{
					ToastTracker.showToast("Notifications Off");
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
		        	ToastTracker.showToast("Sound On");
				} else if(ThisAppConfig.getInstance().getInt(ThisAppConfig.SOUND_SETTINGS)==1)
				{
					ThisAppConfig.getInstance().putInt(ThisAppConfig.SOUND_SETTINGS, 0);
		        	notificationSettingsValue.setText("Off");
		        	ToastTracker.showToast("Sound Off");
				} 
				
			}
		});
        
        levelViewGroup.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showLevelDialog();
				
			}
		});
        
        shareBBA.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showShareBBADialog();				
			}
		});
        
        return settingsView;
		}
	 
	 
	 private void showShareBBADialog()
	 {
		 Intent sendIntent = new Intent();
		 sendIntent.setAction(Intent.ACTION_SEND);
		 
		 String bbaInvite = "Add me on BakarBakarApp , my BBA pin is "+BBAPin;
		 sendIntent.putExtra(Intent.EXTRA_TEXT, bbaInvite);
		 sendIntent.setType("text/plain");
		 startActivity(Intent.createChooser(sendIntent, "Share BBA Pin"));
	 }
	 
	private void showLevelDialog(){ 
		
		
	 AlertDialog levelDialog = null;

	            
    // Creating and Building the Dialog 
    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    builder.setTitle("Set The Bakar Level");
    builder.setSingleChoiceItems(levelitems, -1, new DialogInterface.OnClickListener() {
    public void onClick(DialogInterface dialog, int item) {
       
    	
        switch(item)
        {
            case 0:
                    ToastTracker.showToast("Grow up , this app not for kids");
                     break;
            case 1:
                    ThisUserConfig.getInstance().putInt(ThisUserConfig.LEVEL, 2);
                    
                    break;
            case 2:
            	ThisUserConfig.getInstance().putInt(ThisUserConfig.LEVEL, 3);
                    break;
            case 3:
            	ThisUserConfig.getInstance().putInt(ThisUserConfig.LEVEL, 4);           
                    break;
            case 4:
            	ThisUserConfig.getInstance().putInt(ThisUserConfig.LEVEL, 5);            
               break;
          
            
        }
        if(item!=0)
    		levelValue.setText(levelitems[item]);
        dialog.cancel();   
        }
    });
    levelDialog = builder.create();
    levelDialog.show();
	}
		
}
