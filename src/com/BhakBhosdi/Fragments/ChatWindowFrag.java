package com.BhakBhosdi.Fragments;

import java.util.ArrayList;
import java.util.List;

import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.BhakBhosdi.R;
import com.BhakBhosdi.Activities.BhakBhosdiActivity;
import com.BhakBhosdi.ChatClient.ChatListViewAdapter;
import com.BhakBhosdi.ChatClient.ChatMessage;
import com.BhakBhosdi.ChatClient.IMessageListener;
import com.BhakBhosdi.ChatService.IChatAdapter;
import com.BhakBhosdi.ChatService.IChatManager;
import com.BhakBhosdi.ChatService.Message;
import com.BhakBhosdi.HTTPServer.ServerConstants;
import com.BhakBhosdi.HelperClasses.ActiveChat;
import com.BhakBhosdi.HelperClasses.BlockedUser;
import com.BhakBhosdi.Platform.Platform;
import com.BhakBhosdi.Util.BBDTracker;
import com.BhakBhosdi.Util.Logger;
import com.BhakBhosdi.Util.StringUtils;


public class ChatWindowFrag extends Fragment{
	
	private static String TAG = "com.BhakBhosdi.ChatClient.ChatWindow";
	public static String PARTICIPANT = "participant";
	public static String PARTICIPANT_NAME = "name";	
	
	  
    private ListView mMessagesListView;
    private Button chooseBeepButton; 
    
    private Menu mMenu;
    private IChatAdapter chatAdapter;
    private IChatManager mChatManager;   
    private IMessageListener mMessageListener = new SBOnChatMessageListener();   
    
    private String mParticipantBBDID = "";
    private String mParticipantName = "";       
    private String mParticipantImageURL = ""; 
    Handler mHandler = new Handler();
    private ChatListViewAdapter mMessagesListAdapter = null;
    private boolean mBinded = false;
    private String mThiUserChatUserName = "";
    private String mThisUserChatPassword = "";
    private String mThisUserChatFullName =  "";
	private NotificationManager notificationManager;
    private boolean mFBLoggedIn = false;
    private ChooseBeepFragment chooseBeepFrag;    
       
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);	   
        mMessagesListAdapter = new ChatListViewAdapter(getActivity());
        notificationManager =  (NotificationManager) getActivity().getSystemService(getActivity().NOTIFICATION_SERVICE); 
 }
    
    
		    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
	    	
		super.onCreate(savedInstanceState);		
		
		View chatFrag = inflater.inflate(R.layout.chatwindow,null);
	        	  
	    mMessagesListView = (ListView) chatFrag.findViewById(R.id.chat_messages);
	    mMessagesListView.setAdapter(mMessagesListAdapter);
		chooseBeepButton = (Button) chatFrag.findViewById(R.id.choosebeep_button);		
		chooseBeepButton.setOnClickListener(new OnClickListener() {
		    @Override
		    public void onClick(View v) {
		    	BBDTracker.sendEvent("ChatWindow","ButtonClick","chatwindow:click:send",1L);
		    	if(BlockedUser.isUserBlocked(mParticipantBBDID))
		    		buildUnblockAlertMessageToUnblock(mParticipantBBDID);
		    	else
		    	  chooseBeepFrag(true);
		    }
		});	
		
        //notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);   
        return chatFrag;
}
	    
	    


@Override
public void onResume() {
	super.onResume();
	//set participant before binding	
	Bundle bundle = this.getArguments();
	mParticipantBBDID = bundle.getString(PARTICIPANT,"");	
    if(StringUtils.isBlank(mParticipantBBDID))
	  return;
    notificationManager.cancel(mParticipantBBDID.hashCode());
	mParticipantName = bundle.getString(PARTICIPANT_NAME,"");

	//mContactNameTextView.setText(mParticipantName);
	mParticipantImageURL = StringUtils.getFBPicURLFromFBID(mParticipantBBDID);
	mMessagesListAdapter.clearList();
	mMessagesListAdapter.setParticipantFBURL(mParticipantImageURL);	
	//mContactNameTextView.setText(mReceiver);
	//getParticipantInfoFromFBID(mParticipantFBID);
	
	try {			
			changeOrStartNewCurrentChat();
	} catch (RemoteException e1) {
		// TODO Auto-generated catch block
		e1.printStackTrace();
	}
	
}

    @Override
    public void onPause() {
	super.onPause();
	
	    if (chatAdapter != null) {
	    	try {
				chatAdapter.setOpen(false);
				Logger.i(TAG, "closed chat of:"+chatAdapter.getParticipant());
				List<Message> chatMessages = chatAdapter.getMessages();
                if (chatMessages != null && !chatMessages.isEmpty()) {
				    Message lastmMessage = chatMessages.get(chatMessages.size()-1);
				    ActiveChat.addChat(mParticipantBBDID, mParticipantName, lastmMessage.getBody());
                }
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	  
	    }    
	   
    }
    
    @Override
    public void onDestroy() {
    	
    	super.onDestroy();
    	chatAdapter = null;
    	mChatManager = null;    	
    }   
    
       
    
    @Override
    public void onStart(){
        super.onStart();
        BBDTracker.sendView("ChatWindow");
        BBDTracker.sendEvent("ChatWindow","ScreenOpen","chatwindow:open",1L);
        //EasyTracker.getInstance().activityStart(this);
    }
    
        
    public void chooseBeepFrag(boolean show)
    {        
        BBDTracker.sendView("ChooseBeepView");
        BBDTracker.sendEvent("ChooseBeepView","ScreenOpen","choosebeep:open",1L);    
        FragmentManager fm= getChildFragmentManager();
        if(fm!=null)
        {
	        FragmentTransaction ft = fm.beginTransaction();
	       if(chooseBeepFrag==null)
	        {
	        	chooseBeepFrag = new ChooseBeepFragment();
	        	ft.add(R.id.choosebeep_container, chooseBeepFrag); 
	        	chooseBeepButton.setVisibility(View.GONE);
	        }
	        else if(show)
	        {
	        	ft.show(chooseBeepFrag);
	        	chooseBeepButton.setVisibility(View.GONE);
	        }
	        else
	        {
	        	ft.hide(chooseBeepFrag);	
	        	chooseBeepButton.setVisibility(View.VISIBLE);
	        }
	        ft.commit();
        }                 
        
    }
   
    
	    public String getParticipantFBID() {
			return mParticipantBBDID;
		}	    
	    
		public void sendMessage(String inputContent) {			
		ChatMessage lastMessage = null;				
		 if(!"".equals(inputContent))
		{
			Message newMessage = new Message(mParticipantBBDID);
			newMessage.setBody(inputContent);
			newMessage.setFrom(mThiUserChatUserName+"@"+ServerConstants.CHATSERVERIP);			
			newMessage.setTo(mParticipantBBDID+"@"+ServerConstants.CHATSERVERIP);
			newMessage.setSubject(mThisUserChatFullName);			
			newMessage.setUniqueMsgIdentifier(System.currentTimeMillis());	
			newMessage.setTimeStamp(StringUtils.gettodayDateInFormat("hh:mm"));
			newMessage.setStatus(ChatMessage.SENDING);							
			mMessagesListAdapter.addMessage(new ChatMessage(mThiUserChatUserName, mParticipantBBDID,inputContent, false, StringUtils.gettodayDateInFormat("hh:mm"),
					                                          ChatMessage.SENDING,newMessage.getUniqueMsgIdentifier()));
			mMessagesListAdapter.notifyDataSetChanged();
			
		  //send msg to xmpp
			 try {
				if (chatAdapter == null) {										
					chatAdapter = mChatManager.createChat(mParticipantBBDID, mMessageListener);					
				}
				else
				{
					chatAdapter.setOpen(true);
					Logger.i(TAG, "open chat is of:"+chatAdapter.getParticipant());
					chatAdapter.sendMessage(newMessage);
				}
				
			    } catch (RemoteException e) {
			    	sendingFailed(lastMessage);
				if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, e.getMessage());
			    }
		   
		}			   
		  
		}
	    
	    private void sendingFailed(ChatMessage lastMessage)
	    {
	    	lastMessage = (ChatMessage) mMessagesListAdapter.getItem(mMessagesListAdapter.getCount() - 1);
	    	lastMessage.setStatus(ChatMessage.SENDING_FAILED); 
	    	mMessagesListAdapter.setMessage(mMessagesListAdapter.getCount() - 1, lastMessage);
	    	mMessagesListAdapter.notifyDataSetChanged();
	    }
	  	
	    //in already open chatWindow this function switches chats
	    private void changeOrStartNewCurrentChat() throws RemoteException {
	    
	    	if(mChatManager == null)
			{
				mChatManager = ((BhakBhosdiActivity)getActivity()).getChatManager();    
			} 
			Logger.d(TAG, "Chat manager got");				
			chatAdapter = mChatManager.getChat(mParticipantBBDID);
	    	if (chatAdapter != null) {
	    		chatAdapter.setOpen(true);
	    		Logger.i(TAG, "open chat is of:"+chatAdapter.getParticipant());
	    		chatAdapter.addMessageListener(mMessageListener);
	    		fetchPastMsgsIfAny();
	    	}
			
			
	        }
	    
	    /**
	     * Get all messages from the current chat and refresh the activity with them.
	     * @throws RemoteException If a Binder remote-invocation error occurred.
	     */
	private void fetchPastMsgsIfAny() throws RemoteException {
		mMessagesListAdapter.clearList();		
		if (chatAdapter != null) {
			List<Message> chatMessages = chatAdapter.getMessages();
		Logger.i(TAG, "tryin to fetch past msgs");
		if (chatMessages.size() > 0) {
			List<ChatMessage> msgList = convertMessagesList(chatMessages);
			mMessagesListAdapter.addAllToList(msgList);
			Logger.d(TAG,"list adapter size in fetch past"	+ mMessagesListAdapter.getCount());
			mMessagesListAdapter.notifyDataSetChanged();
			mMessagesListView.setSelection(mMessagesListView.getCount() - 1);
		}
		}
	}
	    

	    /**
	     * Convert a list of Message coming from the service to a list of MessageText that can be displayed in UI.
	     * @param chatMessages the list of Message
	     * @return a list of message that can be displayed.
	     */
	    private List<ChatMessage> convertMessagesList(List<Message> chatMessages) {
		List<ChatMessage> result = new ArrayList<ChatMessage>(chatMessages.size());		
		ChatMessage lastMessage = null;		
		for (Message m : chatMessages) {
		    		    
		    if (m.getType() == Message.MSG_TYPE_CHAT) {	
			
			if (m.getBody() != null) {
			    if (lastMessage == null ) {
					lastMessage = new ChatMessage(m.getInitiator(), m.getReceiver(), m.getBody(), false, m.getTimestamp(),m.getStatus(),m.getUniqueMsgIdentifier());
					if(m.getStatus() == ChatMessage.DELIVERED || m.getStatus()==ChatMessage.RECEIVED)
		    			lastMessage.setStatus(ChatMessage.OLD);
					result.add(lastMessage);
			    } 
			    else if(m.getInitiator().equals(lastMessage.getInitiator()) && 
			    		lastMessage.getStatus() == ChatMessage.OLD &&
			    		(m.getStatus() == ChatMessage.DELIVERED || m.getStatus()==ChatMessage.RECEIVED))
		    	{			    	
	    			lastMessage.setMessage(lastMessage.getMessage().concat("\n" + m.getBody()));	    			
		    		lastMessage.setTimestamp(m.getTimestamp());
		    	}
		    	else
		    	{			    		
		    		lastMessage = new ChatMessage(m.getInitiator(), m.getReceiver(), m.getBody(), false, m.getTimestamp(),m.getStatus(),m.getUniqueMsgIdentifier());
		    		if(m.getStatus() == ChatMessage.DELIVERED || m.getStatus()==ChatMessage.RECEIVED)
		    			lastMessage.setStatus(ChatMessage.OLD);
		    		result.add(lastMessage);
		    	}
			    }			
		    }
		    
		}
		return result;
	    }
	        

	    
		private void buildUnblockAlertMessageToUnblock(final String fbid) {
	        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	        builder.setMessage("Do you really want to unblock "+ mParticipantName + "?")
	                .setCancelable(false)
	                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
	                    public void onClick(final DialogInterface dialog, final int id) {
	                        BlockedUser.deleteFromList(fbid);
	                        sendMessage("");
	                    }
	                })
	                .setNegativeButton("No", new DialogInterface.OnClickListener() {
	                    public void onClick(final DialogInterface dialog, final int id) {
	                        dialog.cancel();
	                    }
	                });
	        final AlertDialog alert = builder.create();
	        alert.show();
	    }

	    
 	    
 
//this is callback method executed on client when ChatService receives a message	
private class SBOnChatMessageListener extends IMessageListener.Stub {
	//this method appends to current chat, we open new chat only on notification tap or user taps on list
	//i.e. we open new chat window only on intent
	@Override
	public void processMessage(final IChatAdapter chatAdapter, final Message msg)
			throws RemoteException {

        mHandler.post(new Runnable() {

            @Override
            public void run() {
                Logger.i(TAG, "Msg of type :" + msg.getType() + " received.");

                if (msg.getType() == Message.MSG_TYPE_ACKFOR_DELIVERED) {
                    //here we should receive acks of only open chats
                    //non open chats ack update msgs in list of theie respective chatAdapter and user when next opens them
                    //he fetches all the msgs which have been updated in adapter.
                    mMessagesListAdapter.updateMessageStatusWithUniqueID(msg.getUniqueMsgIdentifier(), ChatMessage.DELIVERED);
                } else if (msg.getType() == Message.MSG_TYPE_ACKFOR_BLOCKED) {
                    //here we should receive acks of only open chats
                    //non open chats ack update msgs in list of theie respective chatAdapter and user when next opens them
                    //he fetches all the msgs which have been updated in adapter.
                    mMessagesListAdapter.updateMessageStatusWithUniqueID(msg.getUniqueMsgIdentifier(), ChatMessage.BLOCKED);
                } else if (msg.getType() == Message.MSG_TYPE_ACKFOR_SENT) {
                    //here we should receive acks of only open chats
                    //non open chats ack update msgs in list of theie respective chatAdapter and user when next opens them
                    //he fetches all the msgs which have been updated in adapter.
                    mMessagesListAdapter.updateMessageStatusWithUniqueID(msg.getUniqueMsgIdentifier(), ChatMessage.SENT);
                }  else if (msg.getType() == Message.MSG_TYPE_CHAT) {
                    //here we can get one type of chat msg                    
                    //1) incoming msg from other user
                     if (msg.getBody() != null) {
                            //incomiing added in chatadapter
                            //ActiveChat.addChat(mParticipantFBID, mThisUserChatFullName, msg.getBody());
                            ChatMessage lastMessage = null;

                            if (mMessagesListAdapter.getCount() != 0)
                                lastMessage = (ChatMessage) mMessagesListAdapter.getItem(mMessagesListAdapter.getCount() - 1);

                            if (lastMessage != null && !lastMessage.getInitiator().equals(mThiUserChatUserName)) {
                                lastMessage.setMessage(lastMessage.getMessage().concat("\n" + msg.getBody()));
                                lastMessage.setTimestamp(msg.getTimestamp());
                                mMessagesListAdapter.setMessage(mMessagesListAdapter.getCount() - 1, lastMessage);

                            } else if (msg.getBody() != null) {
                                mMessagesListAdapter.addMessage(new ChatMessage(msg.getInitiator(), msg.getReceiver(), msg.getBody(), false, msg.getTimestamp(), msg.getStatus(), msg.getUniqueMsgIdentifier()));
                            }

                        }
                    
                }

                Logger.i(TAG, "Notifying adapter.");
                mMessagesListAdapter.notifyDataSetChanged();
            }
        });


    }
}



	    
}
