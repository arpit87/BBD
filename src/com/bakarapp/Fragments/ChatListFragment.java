package com.bakarapp.Fragments;

import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;

import com.bakarapp.R;
import com.bakarapp.Activities.BhakBhosdiActivity;
import com.bakarapp.Adapter.BBDListAdapter;
import com.bakarapp.ChatClient.ChatMessage;
import com.bakarapp.ChatClient.IMessageListener;
import com.bakarapp.ChatService.IChatManager;
import com.bakarapp.ChatService.Message;
import com.bakarapp.HTTPClient.AddFriendRequest;
import com.bakarapp.HTTPClient.HttpClient;
import com.bakarapp.HTTPClient.HttpResponseListener;
import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.HelperClasses.ActiveChat;
import com.bakarapp.HelperClasses.ChatHistory;
import com.bakarapp.HelperClasses.ProgressHandler;
import com.bakarapp.HelperClasses.ToastTracker;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Users.UserAttributes;
import com.bakarapp.Util.BBDTracker;
import com.bakarapp.Util.Logger;
import com.bakarapp.Util.StringUtils;

public class ChatListFragment extends ListFragment {
	
	private static final String TAG = "com.bakarapp.Fragments.ChatListFragment";
	private ViewGroup mListViewContainer;
	private ListView mChatList;
	 private IChatManager mChatManager;
	 private IMessageListener mMessageListener = new SBOnChatMessageListener();
	BBDListAdapter mAdapter;
	private FriendAddedListener addFriendtListener = null;
	
	@Override
	public void onCreate(Bundle savedState) {
        super.onCreate(null);
		//update listview
        if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"on create list view");
        List<ActiveChat> chatUserlist = ActiveChat.getActiveChats();
        
        //should be bound to service here..
      	mChatManager = ((BhakBhosdiActivity)getActivity()).getChatManager();    
		 
        
        LayoutInflater inflater = ((LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE));
        mListViewContainer = (ViewGroup) inflater.inflate(R.layout.frag_mainscreen_chatlistview, null);
        mChatList = (ListView)mListViewContainer.findViewById(android.R.id.list);
        //set this before setadapter
        		
        View footer = inflater.inflate(R.layout.chatlist_footer, null, false);
        ImageButton addFrenButton = (ImageButton)footer.findViewById(R.id.chatlist_lastrow_addfriend);
		mChatList.addFooterView(footer);
					
       
		mAdapter = new BBDListAdapter(getActivity(), chatUserlist);			
		setListAdapter(mAdapter);
		if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"chatlist users:"+chatUserlist.toString());
       
        
        addFrenButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				//ToastTracker.showToast("footer");
				buildEnterBBDPINAlertDialog();				
			}
		});
	}
    
	 @Override
	    public void onResume() {
	    	super.onResume();
	    	try {
				mChatManager.setOpen(true);
				mChatManager.addMessageListener(mMessageListener);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	 
	 @Override
	    public void onPause() {
	    	super.onPause();
	    	try {
				mChatManager.setOpen(false);
				mChatManager.removeMessageListener(mMessageListener);
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView( inflater, container, null );
		if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"oncreateview chatlistview");
		ViewGroup parent = (ViewGroup) mListViewContainer.getParent();
		if(parent!=null)
			parent.removeView(mListViewContainer);	
		mAdapter.updateToList(ActiveChat.getActiveChats());
		mAdapter.notifyDataSetChanged();
		return mListViewContainer;
	}
	
	@Override
    public void onListItemClick(ListView l, View v, int position, long id) {
		BBDTracker.sendEvent("MyChat","ListClick","mychats:click:listitem",1L);
		ActiveChat clickedUser = (ActiveChat)mAdapter.getItem(position);
		String bbdid = clickedUser.getUserId();
		String name = clickedUser.getName();
		((ChatContainerFrag)getParentFragment()).showIndividualChatView(bbdid, name);
    }	
	
	private void buildEnterBBDPINAlertDialog()
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

		alert.setTitle("Add Friend");
		LayoutInflater inflater = ((LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE));
		ViewGroup input = (ViewGroup) inflater.inflate(R.layout.enter_bap_dialog, null);			
		final EditText ba_pin = (EditText)input.findViewById(R.id.enter_bap_edittext); 
		alert.setView(input);

		alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		public void onClick(DialogInterface dialog, int whichButton) {
		  String value = ba_pin.getText().toString();
		  if(StringUtils.isBlank(value))
			  ToastTracker.showToast("Please enter a BA pin");
		  else
		  {
			  ProgressHandler.showInfiniteProgressDialoge(getActivity(), "Adding friend", "bas 1 sec dost", getListener());
			  dialog.cancel();
			  AddFriendRequest addFrnReq = new AddFriendRequest(value, getListener());
		  	  HttpClient.getInstance().executeRequest(addFrnReq);
		  }
		 
		  }
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		  public void onClick(DialogInterface dialog, int whichButton) {
		    dialog.cancel();
		  }
		});

		alert.show();
	}
	
	HttpResponseListener getListener()
	{
		if(addFriendtListener == null)
			addFriendtListener = new FriendAddedListener();
		return addFriendtListener;
	}
	
	class FriendAddedListener extends HttpResponseListener
	{

		@Override
		public void onComplete(Object Obj) {
			Logger.i(TAG, "add friend complete");			
			//BeepList.getInstance().updateBeepList((JSONArray)beepListJsonArrayObj);
			JSONObject jsonObj = (JSONObject)Obj;
			if(!hasBeenCancelled)
			{
				String friend_nick = "";
				String friend_bbdid = "";
				try {
					friend_nick = jsonObj.getString(UserAttributes.FRIEND_NICK);
					friend_bbdid = jsonObj.getString(UserAttributes.FRIEND_BBD_ID);
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}					
				
				String friend_first_msg = Platform.getInstance().getContext().getResources().getString(R.string.friend_first_msg);	        	  
				String friend_first_msg_img = Platform.getInstance().getContext().getResources().getString(R.string.friend_first_msg_img);
	        	int dev_bbd_id_hash = friend_bbdid.hashCode();
				
				Message welcome_message = new Message("", ServerConstants.AppendServerIPToFBID(friend_bbdid), friend_first_msg, StringUtils.gettodayDateInFormat("hh:mm")
						,Message.MSG_TYPE_CHAT, ChatMessage.RECEIVED,System.currentTimeMillis(),friend_nick,friend_first_msg_img);

				ChatHistory.addtoChatHistory(welcome_message);
				ActiveChat.addChat(friend_bbdid, friend_nick, friend_first_msg,0);
				mAdapter.addToChatList(new ActiveChat(friend_bbdid, friend_nick, friend_first_msg,1));
				mAdapter.notifyDataSetChanged();
				ProgressHandler.dismissDialoge();	
			}
		}
		
	}
	
	//this is callback method executed on client when ChatService receives a message	
	private class SBOnChatMessageListener extends IMessageListener.Stub {
		//this method appends to current chat, we open new chat only on notification tap or user taps on list
		//i.e. we open new chat window only on intent
		@Override
		public void processMessage(final Message msg)
				throws RemoteException {

	        Platform.getInstance().getHandler().post(new Runnable() {

	            @Override
	            public void run() {
	                Logger.i(TAG, "Msg of type :" + msg.getType() + " received in chatlist");
	                //ToastTracker.showToast("got callback in chatlist");
	                if (msg.getType() == Message.MSG_TYPE_ACKFOR_DELIVERED) {
	                    //here we should receive acks of only open chats
	                    //non open chats ack update msgs in list of theie respective chatAdapter and user when next opens them
	                    //he fetches all the msgs which have been updated in adapter.
	                   // mMessagesListAdapter.updateMessageStatusWithUniqueID(msg.getUniqueMsgIdentifier(), ChatMessage.DELIVERED);
	                } else if (msg.getType() == Message.MSG_TYPE_ACKFOR_BLOCKED) {
	                    //here we should receive acks of only open chats
	                    //non open chats ack update msgs in list of theie respective chatAdapter and user when next opens them
	                    //he fetches all the msgs which have been updated in adapter.
	                    //mMessagesListAdapter.updateMessageStatusWithUniqueID(msg.getUniqueMsgIdentifier(), ChatMessage.BLOCKED);
	                } else if (msg.getType() == Message.MSG_TYPE_ACKFOR_SENT) {
	                    //here we should receive acks of only open chats
	                    //non open chats ack update msgs in list of theie respective chatAdapter and user when next opens them
	                    //he fetches all the msgs which have been updated in adapter.
	                   // mMessagesListAdapter.updateMessageStatusWithUniqueID(msg.getUniqueMsgIdentifier(), ChatMessage.SENT);
	                }  else if (msg.getType() == Message.MSG_TYPE_CHAT) {
	                    //here we can get one type of chat msg                    
	                    //1) incoming msg from other user
	                	//ChatListFragment.this.isVisible()
	                	ActiveChat.addChat(msg.getInitiator(), msg.getSubject(), msg.getBody(), 0);
	                    mAdapter.updateToList(ActiveChat.getActiveChats());
	                    mAdapter.notifyDataSetChanged();
	                }

	                Logger.i(TAG, "Notifying adapter.");
	               
	            }
	        });


	    }

					
}
}


