package com.BhakBhosdi.HelperClasses;

import android.app.NotificationManager;
import android.content.Context;
import android.media.RingtoneManager;
import android.net.Uri;

import com.BhakBhosdi.ChatClient.ChatMessage;
import com.BhakBhosdi.ChatService.Message;
import com.BhakBhosdi.HTTPServer.ServerConstants;
import com.BhakBhosdi.Platform.Platform;
import com.BhakBhosdi.Util.BBDTracker;
import com.BhakBhosdi.Util.Logger;
import com.BhakBhosdi.Util.StringUtils;

/****
 * 
 * @author arpit87
 * handler code for various scenaiors on chat click here
 * like not logged in to server,not fb login yet etc
 * to start chat from anywhere call this class
 */
public class CommunicationHelper {
	
	private static String TAG = "com.BhakBhosdi.ActivityHandler.ChatHandler";
	static CommunicationHelper instance = new CommunicationHelper();
	Context context = Platform.getInstance().getContext(); 
	//private FacebookConnector fbconnect;
			
	public static CommunicationHelper getInstance()
	{
		return instance;
	}
	
	
	public void onSendBeepClickWithUser(int beep_id , int beep_to )
	{
		//chat username and id are set only after successful addition to chat server
		//if these missing =?not yet added on chat server		
		BBDTracker.sendEvent("SendBeep","ButtonClick","click:sendbeep",1L);		
	
	}
	
		
	
	public void sendChatNotification(int id,String fb_id,String participant_name,String chatMessage) {

	   	 NotificationManager notificationManager = (NotificationManager) context.getSystemService(context.NOTIFICATION_SERVICE);
			// Intent chatIntent = new Intent(context,ChatWindow.class);
			// 	chatIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
			//   chatIntent.putExtra(ChatWindow.PARTICIPANT, fb_id);
			//   chatIntent.putExtra(ChatWindow.PARTICIPANT_NAME, participant_name);		 
			  	
			 Logger.i(TAG, "Sending notification") ;
			 //PendingIntent pintent = PendingIntent.getActivity(context, id, chatIntent, PendingIntent.FLAG_ONE_SHOT);
			 Uri sound_uri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			 
			// Notification notif = new Notification(R.drawable.launchernew,"New message from "+participant_name,System.currentTimeMillis());
			// notif.flags |= Notification.FLAG_AUTO_CANCEL;
			// notif.setLatestEventInfo(context, participant_name, chatMessage, pintent);
					
			 Message welcome_message = new Message("", ServerConstants.AppendServerIPToFBID(fb_id), chatMessage, StringUtils.gettodayDateInFormat("hh:mm")
					 								,Message.MSG_TYPE_CHAT, ChatMessage.RECEIVED,System.currentTimeMillis(),participant_name);
			 
			 ChatHistory.addtoChatHistory(welcome_message);
			 ActiveChat.addChat(fb_id, participant_name, chatMessage);
			 
			/*	notif.ledARGB = 0xff0000ff; // Blue color
				notif.ledOnMS = 1000;
				notif.ledOffMS = 1000;
				notif.defaults |= Notification.DEFAULT_LIGHTS;	
				notif.sound = sound_uri;
	     
				notificationManager.notify(id, notif);
				Logger.i(TAG, "notification sent") ;			*/
				
			    }

}


