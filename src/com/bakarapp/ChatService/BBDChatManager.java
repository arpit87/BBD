package com.bakarapp.ChatService;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.util.StringUtils;

import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.bakarapp.ChatService.IChatAdapter;
import com.bakarapp.ChatService.IChatManager;
import com.bakarapp.ChatClient.IMessageListener;
import com.bakarapp.HTTPServer.ServerConstants;
import com.bakarapp.HelperClasses.ToastTracker;
import com.bakarapp.Platform.Platform;
import com.bakarapp.Util.Logger;

public class BBDChatManager extends IChatManager.Stub {
	
	private ChatManager mChatManager;
	private XMPPConnection mXMPPConnection;
	private static final String TAG = "com.bakarapp.ChatService.ChatManager";
    private final Map<String, ChatAdapter> mAllChats = new HashMap<String, ChatAdapter>();
    private final SBChatManagerAndInitialMsgListener mChatAndInitialMsgListener = new SBChatManagerAndInitialMsgListener();   
    private final RemoteCallbackList<IMessageListener> mRemoteListeners = new RemoteCallbackList<IMessageListener>();	
    private ChatService mService = null;
    private boolean bbdChatListOpen = false;
      
    

	public BBDChatManager(XMPPConnection xmppConnection, ChatService service) {
		this.mXMPPConnection = xmppConnection;
		this.mChatManager = ChatManager.getInstanceFor(xmppConnection);
		this.mService = service;
		xmppConnection.getRoster();				
		//this.mChatManager.addChatListener(mChatAndInitialMsgListener);
        addChatListener();

	}

    public XMPPConnection getXMPPConnection () {
        return mXMPPConnection;
    }

    private void resetChatManager() {
        mChatManager = ChatManager.getInstanceFor(mXMPPConnection);
    }
    
    private void addChatListener() {
        mChatManager.addChatListener(mChatAndInitialMsgListener);
    }

    public synchronized void resetOnConnection() {
        resetChatManager();
        addChatListener();
        for (Map.Entry<String, ChatAdapter> entry : mAllChats.entrySet()) {
            String key = entry.getKey() + "@" + ServerConstants.CHATSERVERIP;
            Chat chat = mChatManager.createChat(key, null);
            entry.getValue().resetChatOnConnection(chat);
        }
    }

	/**
     * Get an existing ChatAdapter or create it if necessary.
     * @param chat The real instance of smack chat
     * @return a chat adapter register in the manager
     */
    private ChatAdapter getChatAdapter(Chat chat) {
	String key = StringUtils.parseName(chat.getParticipant());
	if (mAllChats.containsKey(key)) {
	    return mAllChats.get(key);
	}
	ChatAdapter newChatAdapter = new ChatAdapter(chat,this);	
	mAllChats.put(key, newChatAdapter);
	return newChatAdapter;
    }
    
    @Override
    public void deleteChatNotification(IChatAdapter chat) {
	mService.deleteNotification(1);
    }
    
    @Override
    public void addMessageListener(IMessageListener listener) throws RemoteException {
	if (listener != null)
		mRemoteListeners.register(listener);
    }
	
 @Override
    public void removeMessageListener(IMessageListener listener) throws RemoteException {
	if (listener != null)
		mRemoteListeners.unregister(listener);
    }

	@Override
	public synchronized IChatAdapter createChat(String participant, IMessageListener listener) throws RemoteException {
			String key = participant+"@"+ServerConstants.CHATSERVERIP;
			ChatAdapter chatAdapter;
			if (mAllChats.containsKey(participant)) {
				chatAdapter = mAllChats.get(participant);
				chatAdapter.addMessageListener(listener);
			    return chatAdapter;
			}
			Chat c = mChatManager.createChat(key, null);
			// maybe a little probleme of thread synchronization
			// if so use an HashTable instead of a HashMap for mChats
			chatAdapter = getChatAdapter(c);
			chatAdapter.addMessageListener(listener);
			return chatAdapter;
		    }
	
	@Override
    public synchronized ChatAdapter getChat(String participant) {
		String key = participant;
		if (mAllChats.containsKey(key)) {
			Logger.i(TAG,"Chat returned for:"+key);
		    return mAllChats.get(key);
		}
		else
		{	
			Chat c = mChatManager.createChat(key+"@"+ServerConstants.CHATSERVERIP, null);
			Logger.i(TAG,"Chat created for:"+key);
			// maybe a little probleme of thread synchronization
			// if so use an HashTable instead of a HashMap for mChats
			return getChatAdapter(c);
		}
    }   
	
	public void notifyAllPendingQueue()
	{
		if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"notifying all pending queue");
		Collection<ChatAdapter> c = mAllChats.values();		
		Iterator<ChatAdapter> it = c.iterator();
		while(it.hasNext())
		{			
			ChatAdapter ca = (ChatAdapter) it.next();
			ca.notifyMsgQueue();
			if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"notified a queue");
		}
			
		
	}
	
	public int numChats()
	{
		return mAllChats.size();
	}
	
	public void notifyChat(Message msg) { 	   			
			if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG, "Sending notification") ;
			if(bbdChatListOpen)
				callListeners(msg);
			else
				mService.sendNotification(msg.getInitiator().hashCode(),msg.getInitiator(),msg.getSubject(),msg.getBody(),msg.getImageName());
	   
	}
	
		
	private class SBChatManagerAndInitialMsgListener implements ChatManagerListener {

		/****
		 * this is initial remote msg listener registered to a newly remote created chat.It is called back only 
		 * till this window not opened by user and it keeps showing incoming msgs as notifications.
		 * as soon the user taps on notification and this chat opens we change msg listener to one with 
		 * client so that further call backs are handled by SBonChatMsgListener
		 */
		/*@Override
		public void processMessage(IChatAdapter chat,
				in.co.hopin.ChatService.Message msg) throws RemoteException {
			try {
				String body = msg.getBody();
				if (!chat.isOpen() && body != null) {
				    if (chat instanceof ChatAdapter) {
					mAllChats.put(chat.getParticipant(), (ChatAdapter) chat);
				    }
				    //will put it as notification
				    notifyChat(chat, body);
				}
			    } catch (RemoteException e) {
				if (Platform.getInstance().isLoggingEnabled()) Log.e(TAG, e.getMessage());
			    }
			
		}*/
		

		@Override
		public void chatCreated(Chat chat, boolean locally) {
			/// no call backs required on remote chat creation as we showing notification
			//till chat window opened by user. As soon he opens we will register msglistener
			//which will then take care of further msgs
			 ChatAdapter newchatAdapter;
			 String key = StringUtils.parseName(chat.getParticipant());
			 if(!com.bakarapp.Util.StringUtils.isBlank(key)) {
				if (mAllChats.containsKey(key)) {
					newchatAdapter= mAllChats.get(key);
					if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"returning old adapter for:"+key);
				}
				else
				{
					if (Platform.getInstance().isLoggingEnabled()) Log.i(TAG,"chat adapter not fond so creating new for:"+key);
					newchatAdapter = new ChatAdapter(chat,BBDChatManager.this);	
					mAllChats.put(key,newchatAdapter);
				}
             }
             else {
                    //newchatAdapter.addMessageListener(mChatAndInitialMsgListener);
			        if (Platform.getInstance().isLoggingEnabled()) Log.d(TAG, "Insane smack " + chat.toString() + " created locally " + locally + " with blank key?: " + key);
             }
		
		}	
		}


	@Override
	public void setOpen(boolean value) throws RemoteException {
		// TODO Auto-generated method stub
		bbdChatListOpen = value;
	}

	@Override
	public boolean isOpen() throws RemoteException {
		// TODO Auto-generated method stub
		return bbdChatListOpen;
	}
	
	private void callListeners(Message msg) {
		//ToastTracker.showToast("sending callback to chatlist");
		int n = mRemoteListeners.beginBroadcast();
		for (int i = 0; i < n; i++) {
			IMessageListener listener =  mRemoteListeners.getBroadcastItem(i);
			try {
				if (listener != null)
					listener.processMessage( msg);
			} catch (RemoteException e) {
				if (Platform.getInstance().isLoggingEnabled()) Log.w(TAG, "Error while diffusing message to listener", e);
			}
		}
		mRemoteListeners.finishBroadcast();
	}	
	
}
