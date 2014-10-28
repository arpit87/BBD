package com.BhakBhosdi.HelperClasses;

import android.content.Context;
import android.widget.Toast;

import com.BhakBhosdi.Platform.Platform;

public class ToastTracker {	
	
	static Context context = Platform.getInstance().getContext();
	public static void showToast(String message,int duration){
		final String thismsg = message;
		final int thisduration = duration;
		Platform.getInstance().getHandler().post((new Runnable(){
			public void run() {				
				Toast.makeText(context, thismsg, thisduration).show();				
			}}));
		
	}
	
	public static void showToast(String message){
		final String thismsg = message;		
		Platform.getInstance().getHandler().post((new Runnable(){
			public void run() {				
				Toast.makeText(context, thismsg, Toast.LENGTH_SHORT).show();				
			}}));
		
	}

}
