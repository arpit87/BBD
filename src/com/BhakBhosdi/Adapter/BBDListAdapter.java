package com.BhakBhosdi.Adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.BhakBhosdi.R;
import com.BhakBhosdi.ChatClient.ChatMessage;
import com.BhakBhosdi.HelperClasses.ActiveChat;

public class BBDListAdapter  extends BaseAdapter{
	
	private LayoutInflater inflater= null;
	List<ActiveChat> mchatUsers;
	//private static String TAG = "com.BhakBhosdi.ChatClient.SBChatListViewAdapter";
	List<ChatMessage> mListMessages = new ArrayList<ChatMessage>();
	HashMap<Long,ChatMessage> mHashMapSentNotDeliveredMsgs = new HashMap<Long,ChatMessage>();

	public BBDListAdapter(Activity activity,List<ActiveChat> chatUserlist)
	{
		inflater= (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mchatUsers = chatUserlist;
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mchatUsers.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return mchatUsers.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
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
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View thisUserRow=convertView;
        if(thisUserRow==null)
		   thisUserRow = inflater.inflate(R.layout.chatlist_row, null);
       // ImageView userImageView = (ImageView)thisUserRow.findViewById(R.id.chatlist_image);                
        TextView userName = (TextView)thisUserRow.findViewById(R.id.chatlist_name);
        TextView lastChatMsgView = (TextView)thisUserRow.findViewById(R.id.chatlist_lastchat);
        
        String fbid = mchatUsers.get(position).getUserId();
        String name = mchatUsers.get(position).getName();
        String lastChat = mchatUsers.get(position).getLastMessage();
       // String imageurl = StringUtils.getFBPicURLFromFBID(fbid);

        //SBImageLoader.getInstance().displayImageElseStub(imageurl, userImageView, R.drawable.userpicicon);
        userName.setText(name);
        lastChatMsgView.setText(lastChat);
        
		return thisUserRow;
		
	}

}
