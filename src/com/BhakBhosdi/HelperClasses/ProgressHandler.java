package com.BhakBhosdi.HelperClasses;


import java.util.concurrent.atomic.AtomicBoolean;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.widget.ProgressBar;

import com.BhakBhosdi.HTTPClient.HttpResponseListener;
import com.BhakBhosdi.Platform.Platform;
import com.BhakBhosdi.Util.BBDTracker;



/**
 * 
 * @author arpit87
 *This class is thread safe,call from anywhere to show and dismiss dialog
 */



public class ProgressHandler {
	ProgressBar progressBar = null;	
	private static ProgressDialog progressDialog = null;
	private static AtomicBoolean isshowing = new AtomicBoolean(false);
	private static Activity underlying_activity = null;
	private static String title = "";
	private static String message = "";
	private static HttpResponseListener mListener = null;
	private static Runnable cancelableRunnable = new Runnable() {
		
		@Override
		public void run() {
			if(progressDialog!=null && isshowing.get())
			{
				progressDialog.setTitle("Slow connection?");
				progressDialog.setMessage("Hold on for some time or press back to cancel");				
				progressDialog.setCancelable(true);		
				BBDTracker.sendEvent("ProgressHandler","ShowDialog","progresshandler:show:cancelable:"+underlying_activity.getClass().toString(),1L);
			}			
		}
	};
	
	private static Runnable startRunnable = new Runnable() {
		
		@Override
		public void run() {
			progressDialog = ProgressDialog.show(underlying_activity, title, message, true);
			progressDialog.setOnCancelListener(new OnCancelListener() {				
				@Override
				public void onCancel(DialogInterface dialog) {
					isshowing.set(false);	
					if(mListener!=null)
						mListener.onCancel();
					BBDTracker.sendEvent("ProgressHandler","ButtonClick","progresshandler:click:cancel:"+underlying_activity.getClass().toString(),1L);
				}		
		});
	}};	
	
	
	public static void showInfiniteProgressDialoge(final Activity underlying_activity,final String title,final String message,HttpResponseListener listener)
	{
		mListener = listener;
		if(!isshowing.getAndSet(true)) 
		{
			ProgressHandler.underlying_activity = underlying_activity;
			ProgressHandler.title = title;
			ProgressHandler.message = message;
			Platform.getInstance().getHandler().post(startRunnable);	
			BBDTracker.sendEvent("ProgressHandler","ShowDialog","progresshandler:show:newdialog:"+underlying_activity.getClass().toString(),1L);
			//set cancelable after 10 sec of delay
			Platform.getInstance().getHandler().postDelayed(cancelableRunnable,10000);
		}
		else
		{
			if(progressDialog!=null)
			{
				Platform.getInstance().getHandler().post((new Runnable(){
					public void run() {
						progressDialog.setTitle(title);
						progressDialog.setMessage(message);
						BBDTracker.sendEvent("ProgressHandler","ShowDialog","progresshandler:show:updateolddialog:"+underlying_activity.getClass().toString(),1L);
				}}));
			}
		}
	}
	
	public static void dismissDialoge()
	{
		if(isshowing.getAndSet(false))
		{
			mListener = null;
			Platform.getInstance().getHandler().removeCallbacks(cancelableRunnable);
			Platform.getInstance().getHandler().post((new Runnable(){
			public void run() {
				if(progressDialog.isShowing())
					progressDialog.dismiss();						
			}}));
		}
	}	
	

}
