package com.BhakBhosdi.ChatClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.BhakBhosdi.R;
import com.BhakBhosdi.HelperClasses.ThisUserConfig;
import com.BhakBhosdi.Util.StringUtils;



public class ChatListViewAdapter extends BaseAdapter {

	//private static String TAG = "com.BhakBhosdi.ChatClient.SBChatListViewAdapter";
	List<ChatMessage> mListMessages = new ArrayList<ChatMessage>();
	HashMap<Long,ChatMessage> mHashMapSentNotDeliveredMsgs = new HashMap<Long,ChatMessage>();
	String participantFBURL = "";
	String selfBBDId = ThisUserConfig.getInstance().getString(ThisUserConfig.BBD_ID);
	String selfFirstName = ThisUserConfig.getInstance().getString(ThisUserConfig.CHATUSERID);	
	String selfImageURL = StringUtils.getFBPicURLFromFBID(selfBBDId);	
	int chatMsgStatus = ChatMessage.UNKNOWN ;
    private Activity activity;

    public ChatListViewAdapter(Activity activity) {
        this.activity = activity;
    }

    @Override
    public boolean isEnabled(int position) {
        return false;
    }

    /**
	 * Returns the number of messages contained in the messages list.
	 * @return The number of messages contained in the messages list.
	 */
	@Override
	public int getCount() {
	    return mListMessages.size();	    
	}
	
	public void setMessage(int i,ChatMessage msg) {
	     mListMessages.set(i, msg);
	}
	
	public void setParticipantFBURL(String fburl) {
		participantFBURL = fburl;
	}
	
	
	public void addMessage(ChatMessage msg) {
	     mListMessages.add(msg);
	     //save all msgs that user is sending in a hash map to update their status
	     if(msg.getStatus() == ChatMessage.SENDING)
	    	 mHashMapSentNotDeliveredMsgs.put(msg.getUniqueIdentifier(), msg);
	}
	
	public void updateMessageStatusWithUniqueID(long unique_ID,int status) {
		
	     ChatMessage msg = mHashMapSentNotDeliveredMsgs.get(unique_ID);
	     if(msg!=null)
	     {
	    	 msg.setStatus(status);
	    	 if(status == ChatMessage.DELIVERED)
	    		 mHashMapSentNotDeliveredMsgs.remove(unique_ID);
	     }
	}
	
	public void clearList()
	{
		mListMessages.clear();
	}
	
	public void addAllToList(List<ChatMessage> listMessages)
	{
		mListMessages.addAll(listMessages);	
		for (ChatMessage msg : listMessages)
		{
			if(msg.getStatus() == ChatMessage.SENDING)
				mHashMapSentNotDeliveredMsgs.put(msg.getUniqueIdentifier(), msg);
		}
	}

	/**
	 * Return an item from the messages list that is positioned at the position passed by parameter.
	 * @param position The position of the requested item.
	 * @return The item from the messages list at the requested position.
	 */
	@Override
	public Object getItem(int position) {
	    return mListMessages.get(position);
	}
	
	

	/**
	 * Return the id of an item from the messages list that is positioned at the position passed by parameter.
	 * @param position The position of the requested item.
	 * @return The id of an item from the messages list at the requested position.
	 */
	@Override
	public long getItemId(int position) {
	    return position;
	}

	/**
	 * Return the view of an item from the messages list.
	 * @param position The position of the requested item.
	 * @param convertView The old view to reuse if possible.
	 * @param parent The parent that this view will eventually be attached to.
	 * @return A View corresponding to the data at the specified position.
	 */

	public View getView(int position, View convertView, ViewGroup parent) {
	    View chatRowView;
	    TextView msgText ;
	    TextView msgStatus ;
	    ImageView imgView ;
	    String imageURL = "";
	    String statusText = "";
	    String time = "";
		LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);		
	    ChatMessage msg = mListMessages.get(position);	
	    if(msg.getInitiator().equalsIgnoreCase(selfBBDId))
	    {
	    	if(position == 0)
	    		chatRowView = inflater.inflate(R.layout.chat_msg_row_my_first, null);
	    	else	
	    		chatRowView = inflater.inflate(R.layout.chat_msg_row_my, null);
	    	imageURL = selfImageURL;	   
	    	// if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG,"self msg"+msg.getMessage());
	    }
	    else
	    {	
	    	if(position == 0)
	    		chatRowView = inflater.inflate(R.layout.chat_msg_row_other_first, null);
	    	else
	    	 	chatRowView = inflater.inflate(R.layout.chat_msg_row_other, null);	    	 	
	 	     imageURL = participantFBURL;		    	
	 	    // if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG,"othr person msg"+msg.getMessage());
	    }  
	  
	    	msgText = (TextView) chatRowView.findViewById(R.id.chatmessagetext);
	    	msgStatus = (TextView) chatRowView.findViewById(R.id.chatmessagestatusandtime);
	    	imgView = (ImageView) chatRowView.findViewById(R.id.chat_msg_pic);
	    	//SBImageLoader.getInstance().displayImageElseStub(imageURL, imgView, R.drawable.userpicicon);
		    msgText.setText(msg.getMessage());
		    
		    if (msg.getTimestamp() != null) {
				time = msg.getTimeStamp();				
			    }
		    
		    chatMsgStatus = msg.getStatus();		    
		    switch(chatMsgStatus)
		    {
		        case ChatMessage.SENDING_FAILED:
		    	statusText = "Sending failed";
		    	msgStatus.setTextColor(Color.RED);
		    	break;
		    	case ChatMessage.SENDING: //sending
		    	statusText = "Sending..";
		    	break;
		    	case ChatMessage.SENT:
		    	statusText = "Sent";		    	
		    	break;
		    	case ChatMessage.DELIVERED:
		    	statusText = "Delivered";		    	
			    break;	
		    	case ChatMessage.BLOCKED:
		    	statusText = "Blocked";
			    break;	
		    	case ChatMessage.RECEIVED:
			    statusText = "@"+time;
				break;
		    	case ChatMessage.OLD:
				statusText = "@"+time;
				break;
				default:
				statusText = "";
		    }
		    //registerForContextMenu(msgText);
		    msgStatus.setText(statusText);
		    
	    if (msg.isError()) {
		String err = "#some error occured!";
		msgText.setText(err);
		msgText.setTextColor(Color.RED);
		msgStatus.setError("");
	    }
	    return chatRowView;
	}
    }

