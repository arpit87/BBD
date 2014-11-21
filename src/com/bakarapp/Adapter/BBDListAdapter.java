package com.bakarapp.Adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bakarapp.R;
import com.bakarapp.ChatClient.ChatMessage;
import com.bakarapp.HelperClasses.ActiveChat;

public class BBDListAdapter  extends BaseAdapter{
	
	private LayoutInflater inflater= null;
	List<ActiveChat> mchatUsers;
	//private static String TAG = "com.bakarapp.ChatClient.SBChatListViewAdapter";
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

	
	public void addToChatList(ActiveChat chat) {
		
	    mchatUsers.add(chat);
	}
	
	public void clearList()
	{
		mchatUsers.clear();
	}
	
	public void updateToList(List<ActiveChat> chatUsers)
	{
		mchatUsers = chatUsers;
	}
	
	private int getImageRes(int position)
	{
		int res = R.drawable.animal1;
		switch(position%11+1)
		{
			case 1:
			res = R.drawable.animal1;
			break;
			case 2:
				res = R.drawable.animal2;
				break;
			case 3:
				res = R.drawable.animal3;
				break;
			case 4:
				res = R.drawable.animal4;
				break;
			case 5:
				res = R.drawable.animal5;
				break;
			case 6:
				res = R.drawable.animal6;
				break;
			case 7:
				res = R.drawable.animal7;
				break;
			case 8:
				res = R.drawable.animal8;
				break;
			case 9:
				res = R.drawable.animal9;
				break;
			case 10:
				res = R.drawable.animal10;
				break;
			case 11:
				res = R.drawable.animal11;
				break;
		}
		return res;
	}
			
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		
		View thisUserRow=convertView;
        if(thisUserRow==null)
		   thisUserRow = inflater.inflate(R.layout.chatlist_row, null);
        ImageView userImageView = (ImageView)thisUserRow.findViewById(R.id.chatlist_image);                
        TextView userName = (TextView)thisUserRow.findViewById(R.id.chatlist_name);
        TextView lastChatMsgView = (TextView)thisUserRow.findViewById(R.id.chatlist_lastchat);
        TextView baidtextView = (TextView)thisUserRow.findViewById(R.id.chatlist_baid);
        userImageView.setBackgroundResource(getImageRes(position));
        
        String baid = mchatUsers.get(position).getUserId();
        String name = mchatUsers.get(position).getName();
        String lastChat = mchatUsers.get(position).getLastMessage();
        int lastchatread = mchatUsers.get(position).getLastMessageRead();
        //String imageurl = StringUtils.getFBPicURLFromFBID(fbid);

        //SBImageLoader.getInstance().displayImageElseStub(imageurl, userImageView, R.drawable.userpicicon);
        userName.setText(name);
        baidtextView.setText(baid);
        lastChatMsgView.setText(lastChat);
        if(lastchatread!=1)
        	lastChatMsgView.setTypeface(null,Typeface.BOLD);
        
		return thisUserRow;
		
	}

}
