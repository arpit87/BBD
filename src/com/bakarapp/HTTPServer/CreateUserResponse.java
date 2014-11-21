package com.bakarapp.HTTPServer;

import org.apache.http.HttpResponse;
import org.json.JSONException;

import android.content.Intent;
import android.util.Log;

import com.bakarapp.R;
import com.bakarapp.Activities.BhakBhosdiActivity;
import com.bakarapp.ChatClient.ChatMessage;
import com.bakarapp.ChatService.ChatService;
import com.bakarapp.ChatService.Message;
import com.bakarapp.HelperClasses.ActiveChat;
import com.bakarapp.HelperClasses.ChatHistory;
import com.bakarapp.HelperClasses.ProgressHandler;
import com.bakarapp.HelperClasses.ThisAppConfig;
import com.bakarapp.HelperClasses.ThisUserConfig;
import com.bakarapp.HelperClasses.ToastTracker;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Users.ThisUser;
import com.bakarapp.Users.UserAttributes;
import com.bakarapp.Util.Logger;
import com.bakarapp.Util.StringUtils;

public class CreateUserResponse extends ServerResponseBase{

		
	private static final String TAG = "com.bakarapp.Server.CreateUserResponse";
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
			//String nick = body.getString(UserAttributes.NICK);
			String chatuser = body.getString(UserAttributes.CHATUSER);
			String chatpass = body.getString(UserAttributes.CHATPASS);
			ThisUserConfig.getInstance().putString(ThisUserConfig.BBD_ID, bbd_id);
			ThisUserConfig.getInstance().putInt(ThisUserConfig.LEVEL, 3);
			//ThisUserConfig.getInstance().putString(ThisUserConfig.MYNICK, nick);
			ThisUserConfig.getInstance().putString(ThisUserConfig.CHATUSERID, chatuser);
			ThisUserConfig.getInstance().putString(ThisUserConfig.CHATPASSWORD, chatpass);
			
			//for dev profile save app _id
			if(bbd_id.equals("1001"))
			{
				String AppUUID = body.getString(ThisAppConfig.APPUUID);
				ThisAppConfig.getInstance().putString(ThisAppConfig.APPUUID, AppUUID);
			}
            // starts the gcm service once the userid is available

			ThisUser.getInstance().setUserID(bbd_id);	
			//ThisUser.getInstance().setPhoneNumber(phone);
			
			ToastTracker.showToast("Your BA Pin "+bbd_id);
			
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
      		String dev_first_msg_img = Platform.getInstance().getContext().getResources().getString(R.string.dev_first_msg_img);
        	int dev_bbd_id_hash = dev_bbd_id.hashCode();
			
			Message welcome_message = new Message("", ServerConstants.AppendServerIPToFBID(dev_bbd_id), dev_first_msg, StringUtils.gettodayDateInFormat("hh:mm")
					,Message.MSG_TYPE_CHAT, ChatMessage.RECEIVED,System.currentTimeMillis(),dev_nick,dev_first_msg_img);

			ChatHistory.addtoChatHistory(welcome_message);
			ActiveChat.addChat(dev_bbd_id, dev_nick, dev_first_msg,0);
			
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
