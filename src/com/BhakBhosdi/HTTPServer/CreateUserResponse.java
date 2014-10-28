package com.BhakBhosdi.HTTPServer;

import org.apache.http.HttpResponse;
import org.json.JSONException;

import android.content.Intent;
import android.util.Log;

import com.BhakBhosdi.R;
import com.BhakBhosdi.Activities.BhakBhosdiActivity;
import com.BhakBhosdi.ChatClient.ChatMessage;
import com.BhakBhosdi.ChatService.ChatService;
import com.BhakBhosdi.ChatService.Message;
import com.BhakBhosdi.HelperClasses.ActiveChat;
import com.BhakBhosdi.HelperClasses.ChatHistory;
import com.BhakBhosdi.HelperClasses.ProgressHandler;
import com.BhakBhosdi.HelperClasses.ThisUserConfig;
import com.BhakBhosdi.HelperClasses.ToastTracker;
import com.BhakBhosdi.Platform.Platform;
import com.BhakBhosdi.Users.ThisUser;
import com.BhakBhosdi.Users.UserAttributes;
import com.BhakBhosdi.Util.Logger;
import com.BhakBhosdi.Util.StringUtils;

public class CreateUserResponse extends ServerResponseBase{

		
	private static final String TAG = "com.BhakBhosdi.Server.CreateUserResponse";
	public CreateUserResponse(HttpResponse response,String jobjStr,String api) {
		super(response,jobjStr,api);		
	}
	
	@Override
	public void process() {
		Logger.i(TAG,"processing CreateUserResponse response.status:"+this.getStatus());
		
		Logger.i(TAG,"got json "+jobj.toString());		
		try {
			body = jobj.getJSONObject("body");
			String bbd_id = body.getString(UserAttributes.BBD_ID);
			//String phone = body.getString(UserAttributes.PHONE);
			String chatuser = body.getString(UserAttributes.CHATUSER);
			String chatpass = body.getString(UserAttributes.CHATPASS);
			ThisUserConfig.getInstance().putString(ThisUserConfig.BBD_ID, bbd_id);
			//ThisUserConfig.getInstance().putString(ThisUserConfig.PHONE, phone);
			ThisUserConfig.getInstance().putString(ThisUserConfig.CHATUSERID, chatuser);
			ThisUserConfig.getInstance().putString(ThisUserConfig.CHATPASSWORD, chatpass);
            // starts the gcm service once the userid is available

			ThisUser.getInstance().setUserID(bbd_id);	
			//ThisUser.getInstance().setPhoneNumber(phone);
			
			ToastTracker.showToast("Got bbd_id:"+bbd_id);
			
			//now we will start map activity
			final Intent showBBDActivity = new Intent(Platform.getInstance().getContext(), BhakBhosdiActivity.class);	
			showBBDActivity.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
			 Platform.getInstance().getHandler().post(new Runnable() {
	          public void run() { 
	              Platform.getInstance().getContext().startActivity(showBBDActivity);
	              ProgressHandler.dismissDialoge();
	 			  mListener.onComplete(null);
	          }
	        });
			 
			Intent loginToChatServer = new Intent();
			loginToChatServer.setAction(ChatService.BBDLOGIN_TO_CHAT);
			loginToChatServer.putExtra("username", chatuser);
			loginToChatServer.putExtra("password", chatpass);
			Platform.getInstance().getContext().sendBroadcast(loginToChatServer);
			
			String dev_bbd_id = Platform.getInstance().getContext().getResources().getString(R.string.dev_bbd_id);
      		String dev_nick = Platform.getInstance().getContext().getResources().getString(R.string.dev_nick);
      		String dev_first_msg = Platform.getInstance().getContext().getResources().getString(R.string.dev_first_msg);	        	  
        	int dev_bbd_id_hash = dev_bbd_id.hashCode();
			
			Message welcome_message = new Message("", ServerConstants.AppendServerIPToFBID(dev_bbd_id), dev_first_msg, StringUtils.gettodayDateInFormat("hh:mm")
					,Message.MSG_TYPE_CHAT, ChatMessage.RECEIVED,System.currentTimeMillis(),dev_nick);

			ChatHistory.addtoChatHistory(welcome_message);
			ActiveChat.addChat(dev_bbd_id, dev_nick, dev_first_msg);
			
			logSuccess();
		} catch (JSONException e) {
			logServererror();
			if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, "Error returned by server on user add");
			ProgressHandler.dismissDialoge();
			ToastTracker.showToast("Unable to communicate to server,try again later");
			e.printStackTrace();
		}
		
	}
		
   }
