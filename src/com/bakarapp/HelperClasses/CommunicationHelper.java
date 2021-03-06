package com.bakarapp.HelperClasses;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.LayoutInflater;
import android.widget.EditText;

import com.bakarapp.ChatClient.ChatMessage;
import com.bakarapp.ChatService.Message;
import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Util.BBDTracker;
import com.bakarapp.Util.Logger;
import com.bakarapp.Util.StringUtils;

/****
 * 
 * @author arpit87
 * handler code for various scenaiors on chat click here
 * like not logged in to server,not fb login yet etc
 * to start chat from anywhere call this class
 */
public class CommunicationHelper {
	
	private static String TAG = "com.bakarapp.ActivityHandler.ChatHandler";
	static CommunicationHelper instance = new CommunicationHelper();
	Context context = Platform.getInstance().getContext(); 
	//private FacebookConnector fbconnect;
			
	public static CommunicationHelper getInstance()
	{
		return instance;
	}
	
	public void addFriend(int bbd_id)
	{	

		// get prompts.xml view
		LayoutInflater layoutInflater = LayoutInflater.from(context);
		//View promptView = layoutInflater.inflate(R.layout.input_dialog, null);
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
		EditText bbdid_to_add = new EditText(context);
		alertDialogBuilder.setView(bbdid_to_add);
		// setup a dialog window
		alertDialogBuilder
				.setCancelable(true)
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						
						
					}
				})
				.setNegativeButton("Cancel",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});
	
		// create an alert dialog
		AlertDialog alert = alertDialogBuilder.create();
		alert.show();

		
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
					
			// Message welcome_message = new Message("", ServerConstants.AppendServerIPToFBID(fb_id), chatMessage, StringUtils.gettodayDateInFormat("hh:mm")
			//		 								,Message.MSG_TYPE_CHAT, ChatMessage.RECEIVED,System.currentTimeMillis(),participant_name);
			 
			// ChatHistory.addtoChatHistory(welcome_message);
			// ActiveChat.addChat(fb_id, participant_name, chatMessage);
			 
			/*	notif.ledARGB = 0xff0000ff; // Blue color
				notif.ledOnMS = 1000;
				notif.ledOffMS = 1000;
				notif.defaults |= Notification.DEFAULT_LIGHTS;	
				notif.sound = sound_uri;
	     
				notificationManager.notify(id, notif);
				Logger.i(TAG, "notification sent") ;			*/
				
			    }

}


