package com.bakarapp.ChatClient;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.widget.Toast;


public class ChatBroadcastReceiver extends BroadcastReceiver{

	    /** Broadcast intent type. */
	    public static String SBCHAT_CONNECTION_CLOSED = "SBConnectionClosed";
	    public static String SBLOGIN_TO_CHAT = "SBLoginToChatServer";

	   
	    @Override
	    public void onReceive(final Context context, final Intent intent) {
	    	String intentAction = intent.getAction();
		if (intentAction.equals(SBCHAT_CONNECTION_CLOSED)) {
		    CharSequence message = intent.getCharSequenceExtra("message");
		    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
		    if (context instanceof Activity) {
			Activity act = (Activity) context;
			act.finish();
			// The service will be unbinded in the destroy of the activity.
		    }
		} else if (intentAction.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
		    if (intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false)) {
			Toast.makeText(context, "No network",
			    Toast.LENGTH_SHORT).show();
			//context.stopService(new Intent(context, SBChatService.class));
		    }
		}
		/*else if(intentAction.equals(ServerConstants.NEARBY_USER_UPDATED))
		{
			//this is done to handle if chat comes in from a user who is not yet visible to this user
			//then getnearbyuser called which fires intent after updation and then we proceed in chat
			//updation is in new list and not in current list so we need to call update currenttonew
			thisChatWindow.unregisterReceiver(this);			
			NearbyUser thisNearbyUser= CurrentNearbyUsers.getInstance().getNearbyUserWithFBID(thisChatWindow.getParticipantFBID());
			if(thisNearbyUser == null)
			{
				//this can happen if user get a chat after long time n other user has moved out till then
				Toast.makeText(context, "Sorry the user changed his request,chat will close",
					    Toast.LENGTH_SHORT).show();
				thisChatWindow.finish();
			}
			else
				thisChatWindow.getParticipantInfoFromFBID(thisChatWindow.getParticipantFBID());			
			ProgressHandler.dismissDialoge();
		}*/
	    }
	}



